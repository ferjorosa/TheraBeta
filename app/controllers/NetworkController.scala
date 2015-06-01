package controllers

import play.api.mvc._

object NetworkController extends Controller {

  def currentNetwork = Action{
    Ok(views.html.Network.network())
  }

}