package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.{Follower, Network, NormalUser}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

object NetworkController extends AuthConfigImpl with AuthElement {

  val networkRegisterForm:Form[Network]= Form(mapping(
    "accountID"-> ignored("default"),
    "name" -> text(minLength = 2, maxLength = 32),
    "activated" -> ignored(true)//TODO: It should be false adn then be activated by the user...etc, test purposes...
  )(Network.apply)(Network.unapply))

  def manageNetworks = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "my networks"
    val f = Network.getNetworks(user.username)
    f.map(networks => Ok(views.html.Network.manageNetworks(networks.toList)))
  }

  def register = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "new network"
    Future.successful(Ok(views.html.Network.registerNetwork(networkRegisterForm)))
  }

  def addNetwork = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "add network"

    networkRegisterForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.Network.registerNetwork(formWithErrors))),
      network =>{
        val newNetwork = Network(user.username,network.name,network.activated)
        Network.insertNewNetwork(newNetwork)
        Future.successful(Redirect("/networks"))
      }
    )
  }

  def detailNetwork(networkName: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "network detail"
    val f = Follower.getAllFollowers(user.username,networkName)

    f.map(followers => Ok(views.html.Network.networkDetail(followers.toList,networkName)))
  }

}