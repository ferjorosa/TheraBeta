package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.{Device, Follower, NormalUser}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

/**
 * Created by Fer on 04/06/2015.
 */
object FollowerController extends AuthConfigImpl with AuthElement{

  val followerRegisterForm:Form[Follower]= Form(mapping(
    "accountID" -> ignored("default"),
    "networkID" -> ignored("default"),
    "deviceX" -> text(minLength = 2, maxLength = 32),
    "deviceY" -> text(minLength = 2, maxLength = 32)
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
      formWithErrors =>{
        val f = Device.getDevicesByAccountId(user.username)
        f.map(devices => BadRequest(views.html.Network.registerFollower(followerRegisterForm, networkName, devices.toList)))
      },
      follower =>{
        val newFollower = Follower(user.username,networkName,follower.deviceX,follower.deviceY)
        Follower.insertNewFollower(newFollower)
        Future.successful(Redirect("/networks/"+networkName))
      }
    )
  }
}
