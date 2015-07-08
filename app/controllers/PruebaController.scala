package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.NormalUser

object PruebaController extends AuthConfigImpl with AuthElement{
  //Non-Asynchronous
  def main = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "message main"
    Ok(views.html.Prueba.texto())
  }

}