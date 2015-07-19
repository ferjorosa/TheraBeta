package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.NormalUser
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import scala.concurrent.Future

object Application extends AuthConfigImpl with AuthElement {

  def index = Action { implicit request =>
    Ok(views.html.index(request.acceptLanguages.head))
  }

  def error(code: Int) = Action{ implicit request =>
    Ok(views.html.Errors.Error(code))
  }

  def dashboard = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>

    Future(Ok(views.html.dashboard(request.acceptLanguages.head)))
  }
  //TODO: Useless
  def prueba(lang:String) = Action{implicit request =>
    Ok(views.html.Prueba.texto())
  }
}