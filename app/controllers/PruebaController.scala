package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.{Device, NormalUser}
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by Fer on 26/02/2015.
 */
object PruebaController extends AuthConfigImpl with AuthElement{
  //Non-Asynchronous
  def main = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "message main"
    Ok(views.html.Prueba.texto())
  }
  //Asynchronous
  def devicesTest = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "message main"
    val f= Device.getDevicesByAccountId(user.username)
    f.map(devices => Ok(views.html.Device.listDevices(devices.toList)))
  }
}