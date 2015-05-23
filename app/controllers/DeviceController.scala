package controllers

import java.util.UUID

import controllers.PruebaController._
import models.{NormalUser, Device}
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

  def addDevice = Action{ implicit request =>
    deviceRegisterForm.bindFromRequest.fold(
        formWithErrors =>  BadRequest(views.html.Device.register(formWithErrors)),

        device =>{
              Device.save(device)
              Redirect("/devices/register")
        })
  }

  def manageDevices = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "my devices"
    val f = Device.getDevicesByAccountId(user.username)
    f.map(devices => Ok(views.html.Device.manageDevices(devices.toList)))
  }

  def detailDevice(identifer :String) = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "device detail"
    val f = Device.getDeviceByIdentifier(user.username,identifer)
    f.map(deviceRetrieved => deviceRetrieved match{
      case Some(device) => Ok(views.html.Device.deviceDetail(device))
      case None => Ok("Error 404")
    })
  }


  //TODO flatMap or map?
  def listAll = Action.async{
    val f = Device.getAllDevices
    f.map(devices => Ok(views.html.Device.listDevices(devices.toList)))
    //Teoricamente estoy pasando todo su contenido, no iterando
    //Ej: f onSuccess { case posts => for (post <- posts) println(post) }
  }

}