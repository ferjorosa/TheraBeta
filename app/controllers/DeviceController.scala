package controllers

import java.util.UUID

import models.Device
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

object DeviceController extends Controller {

  //Form-mapping used in the registration of a new user's device
  val deviceRegisterForm: Form[Device] = Form(mapping(
    "DeviceID"-> ignored(UUID.randomUUID()),
    "OwnerID" -> ignored("default"),
    "Identifier"-> text(minLength = 2, maxLength = 32),
    "Activated"-> ignored(false),
    "Subscriptions" -> ignored(Set.empty[UUID])
  )(Device.apply)(Device.unapply))

  def register = Action{
    Ok(views.html.Device.register(deviceRegisterForm))
  }

  def addDevice = Action{

    implicit request =>
      deviceRegisterForm.bindFromRequest.fold(

        formWithErrors =>  BadRequest(views.html.Device.register(formWithErrors)),

        device =>{
              Device.save(device)
              Redirect("/device/register")
        })



  }
  //TODO async
  /*Here we will check if the user has >0 devices and load them*/
  def manageDevices = Action{
    Ok(views.html.Device.manageDevices())
  }
  //TODO flatMap or map?
  def listAll = Action.async{
    val f = Device.getAllDevices
    f.map(devices => Ok(views.html.Device.listDevices(devices.toList)))
    //Teoricamente estoy pasando todo su contenido, no iterando
    //Ej: f onSuccess { case posts => for (post <- posts) println(post) }
  }

}