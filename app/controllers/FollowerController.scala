package controllers

import java.util.UUID

import jp.t2v.lab.play2.auth.AuthElement
import models.{Device, Follower, Network, NormalUser}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Created by Fer on 04/06/2015.
 */
object FollowerController extends AuthConfigImpl with AuthElement {

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

  def register(networkName: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "new follower"
    val f = Device.getDevicesByAccountId(user.username)
    f.map(devices => Ok(views.html.Network.registerFollower(followerRegisterForm, networkName, devices.toList)))
  }

  def addFollower(networkName: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "add follower"

    followerRegisterForm.bindFromRequest.fold(
      formWithErrors => {
        val f = Device.getDevicesByAccountId(user.username)
        f.map(devices => BadRequest(views.html.Network.registerFollower(followerRegisterForm, networkName, devices.toList)))
      },
      follower => {
        val result = for {
          deviceX <- Device.getDeviceById(follower.deviceX)
          deviceY <- Device.getDeviceById(follower.deviceY)
        } yield (deviceX, deviceY) //If defined it returns 2 successful Option[Device], if not 2 failed Option[Device]

        result.map {
          devices => {
            if (devices._1.isDefined && devices._2.isDefined) {
              Follower.insertNewFollower(
                Follower(
                  user.username,
                  networkName,
                  devices._1.get.DeviceID,
                  devices._2.get.DeviceID,
                  devices._1.get.Identifier,
                  devices._2.get.Identifier))

              Redirect("/networks/" + networkName)
            } else
              BadRequest("/error/404")
          }
        }

      } //end-follower
    ) //end-form-binder
  } //end-add-follower

  /*TODO: This method should be looked once again
    Check: if the network exists, if those devices exist...etc
  */
  def deleteFollower(networkName: String, deviceX: String, deviceY: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "delete follower"

    val deviceX_UUID = UUID.fromString(deviceX)
    val deviceY_UUID = UUID.fromString(deviceY)
    val follower = Follower(user.username, networkName, deviceX_UUID, deviceY_UUID, "ignored", "ignored")

    Follower.deleteFollower(follower).map { res =>
      if (res.wasApplied())
        Redirect("/networks/" + networkName).flashing("success" -> "Follower removed correctly")
      else
        Redirect("/error/500")
    }
  }

  def listFollowers(networkName: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "delete follower"

    Network.getNetwork(user.username, networkName) flatMap {
      case Some(network) =>
        Follower.getAllFollowers(user.username, network.name).map {
          followers => Ok(views.html.Network.manageFollowers(followers.toList, network.name))
        }
      case None => Future.successful(Redirect("/error/404"))
    }
  }
}
