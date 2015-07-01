package controllers

import play.api.mvc._

object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Thera ya esta lista. Por fin. Madre mia, de dios."))
  }

  def error(code: Int) = Action{ implicit request =>
    Ok(views.html.Errors.Error(code))
  }
  //TODO: Useless
  def dashboard = Action{
    Ok(views.html.dashboard())
  }
  //TODO: Useless
  def prueba(lang:String) = Action{implicit request =>
    Ok(views.html.Prueba.texto())
  }
}