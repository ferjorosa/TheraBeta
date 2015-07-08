package controllers

import java.util.UUID

import jp.t2v.lab.play2.auth.AuthElement
import models.{Device, Follower, Network, NormalUser}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future


object FollowerController extends AuthConfigImpl with AuthElement {
  /**
   * Form-mapper used in the registration of a new follower
   */
  val followerRegisterForm: Form[Follower] = Form(mapping(
    "accountID" -> ignored("default"),
    "networkID" -> ignored("default"),
    "deviceX" -> text(maxLength = 36) //UUID's max length
      .transform(
        (deviceX: String) => UUID.fromString(deviceX),
        (deviceX: UUID) => deviceX.toString),
    "deviceY" -> text(maxLength = 36) //UUID's max length
      .transform(
        (deviceY: String) => UUID.fromString(deviceY),
        (deviceY: UUID) => deviceY.toString),
    "deviceX_Name" -> ignored("default"),
    "deviceY_Name" -> ignored("default")
  )(Follower.apply)(Follower.unapply))

  /**
   * Redirects to the registration page after authorization
   * @param networkName
   * @return
   */
  def register(networkName: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "new follower"

    Device.getDevicesByAccountId(user.username)map(devices =>
      if(devices.isEmpty)
        Redirect("/networks/"+networkName).flashing("failure" -> Messages("registerFollower.noDevices"))
      else
        Ok(views.html.Network.registerFollower(followerRegisterForm, networkName, devices.toList))
    )
  }

  /**
   * Registers a new follower
   * @param networkName
   * @return
   */
  def addFollower(networkName: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "add follower"

    followerRegisterForm.bindFromRequest.fold(
      formWithErrors => Device.getDevicesByAccountId(user.username) map(devices =>
        BadRequest(views.html.Network.registerFollower(formWithErrors, networkName, devices.toList))),

      follower => {
        val result = for {
          deviceX <- Device.getDeviceById(follower.deviceX)
          deviceY <- Device.getDeviceById(follower.deviceY)
        } yield (deviceX, deviceY) //If defined it returns 2 successful Option[Device], if not 2 failed Option[Device]

        result.flatMap {
          devices => {
            if (devices._1.isDefined && devices._2.isDefined) {

              if (devices._1 equals devices._2)
                Future(Redirect("/networks/" + networkName + "/register").flashing("failure" -> Messages("registerFollower.failure")))

              else{ //This else is necessary because if it doesn't exist it will not return the redirection
                Follower.save(
                  Follower(user.username, networkName, devices._1.get.DeviceID, devices._2.get.DeviceID, devices._1.get.Identifier, devices._2.get.Identifier)
                ) map (res =>
                  if (res)
                    Redirect("/networks/" + networkName + "/followers").flashing("success" -> Messages("registerFollower.successful"))
                  else
                    Redirect("/error/500"))
              }
            } else
              Future(Redirect("/error/404"))
          }
        }

      } //end-follower
    ) //end-form-binder
  } //end-add-follower

  /**
   * Deletes a follower
   * @param networkName
   * @param deviceX
   * @param deviceY
   * @return
   */
  //TODO: Too much compressed, It needs a way to distinguish between "the device doesnt exist" (404) and "couldn't delete everything" (500)
  def deleteFollower(networkName: String, deviceX: String, deviceY: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "delete follower"

    val deviceX_UUID = UUID.fromString(deviceX)
    val deviceY_UUID = UUID.fromString(deviceY)
    val follower = Follower(user.username, networkName, deviceX_UUID, deviceY_UUID, "ignored", "ignored")

    Follower.deleteFollower(follower).map { res =>
      if (res)
        Redirect("/networks/" + networkName + "/followers").flashing("success" -> Messages("manageFollowers.successfulDelete"))
      else
        Redirect("/error/404")
    }
  }

  /**
   * Lists all followers of a network
   * @param networkName
   * @return
   */
  def listFollowers(networkName: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "list followers"

    Network.getNetwork(user.username, networkName) flatMap {
      case Some(network) =>
        Follower.getAllFollowers(user.username, network.name).map {
          followers => Ok(views.html.Network.manageFollowers(followers.toList, network.name))
        }
      case None => Future(Redirect("/error/404"))
    }
  }
}
