package controllers

import play.api.mvc._

object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Thera ya esta lista. Por fin. Madre mia, de dios."))
  }

  def dashboard = Action{
    Ok(views.html.dashboard())
  }

  def prueba(lang:String) = Action{implicit request =>
    Ok(views.html.Prueba.texto())
  }
}