package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.{Network, NormalUser}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

object NetworkController extends AuthConfigImpl with AuthElement {

  val networkRegisterForm:Form[Network]= Form(mapping(
    "accountID"-> ignored("default"),
    "name" -> text(minLength = 2, maxLength = 32),
    "activated" -> ignored(false)//When a new network is created is deactivated by default
  )(Network.apply)(Network.unapply))

  /**
   * Redirects to the network management page
   * @return
   */
  def manageNetworks = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "my networks"
    Network.getNetworks(user.username)map(networks =>
      Ok(views.html.Network.manageNetworks(networks.toList)))
  }

  /**
   * Redirects to the network registration page
   * @return
   */
  def register = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "new network"
    Future(Ok(views.html.Network.registerNetwork(networkRegisterForm)))
  }

  /**
   * Registers a new network
   * @return
   */
  def addNetwork = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "add network"

    networkRegisterForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.Network.registerNetwork(formWithErrors))),
      network =>
        Network.getNetwork(user.username,network.name) flatMap{
          case Some(networkRetrieved)=> Future(Redirect("/devices/register").flashing("failure"->Messages("registerNetwork.networkAlreadyExists")))
          case None =>{
            val newNetwork = Network(user.username,network.name,network.activated)
            Network.save(user.username,newNetwork) map{res=>
              if(res)
                Redirect("/networks").flashing("success"->Messages("registerNetwork.successful"))
              else
                Redirect("/error/500")
            }
          }
        })
  }

  /**
   * Redirects to the network's detail page
   * @param networkName
   * @return
   */
  def detailNetwork(networkName: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "network detail"

    Network.getNetwork(user.username,networkName) map{
      case Some(network)=>
        Ok(views.html.Network.networkDetail(network))

      case None => Redirect("/error/404")
    }
  }

  /**
   * Deletes a network by its name
   * @param networkName
   * @return
   */
  //TODO: Too much compressed, It needs a way to distinguish between "the network doesnt exist" (404) and "couldn't delete everything" (500)
  def deleteNetwork(networkName: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "delete network"

    Network.deleteNetwork(user.username,networkName)map { res =>
      if (res == true)
        Redirect("/networks").flashing("success" -> Messages("networkDetail.successfulDelete"))
      else
        Redirect("/error/404")
    }
  }

  /**
   * Activates a network by its name
   * @param networkName
   * @return
   */
  //TODO: Too much compressed, It needs a way to distinguish between "the network doesnt exist" (404) and "couldn't delete everything" (500)
  def activateNetwork(networkName: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "activate network"

    Network.activateNetwork(user.username,networkName)map { res =>
      if (res == true)
        Redirect("/networks/"+networkName).flashing("success" -> Messages("networkDetail.successfulActivation"))
      else
        Redirect("/error/404")
    }
  }

  /**
   * Deactivates a network by its name
   * @param networkName
   * @return
   */
  //TODO: Too much compressed, It needs a way to distinguish between "the network doesnt exist" (404) and "couldn't delete everything" (500)
  def deactivateNetwork(networkName: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "deactivate network"

    Network.deactivateNetwork(user.username,networkName)map { res =>
      if (res == true)
        Redirect("/networks/"+networkName).flashing("success" -> Messages("networkDetail.successfulDeactivation"))
      else
        Redirect("/error/404")
    }
  }

}