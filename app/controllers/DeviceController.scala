package controllers

import java.util.UUID

import jp.t2v.lab.play2.auth.AuthElement
import models.{Device, NormalUser}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import scala.concurrent.Future

object DeviceController extends AuthConfigImpl with AuthElement {

  //Form-mapping used in the registration of a new user's device
  val deviceRegisterForm: Form[Device] = Form(mapping(
    "DeviceID"-> ignored(UUID.randomUUID()),  //static UUID, its defined at the creation of the class
    "OwnerID" -> ignored("default"),
    "Identifier"-> text(minLength = 2, maxLength = 32),
    "Activated"-> ignored(false),
    "Subscriptions" -> ignored(Set.empty[UUID])
  )(Device.apply)(Device.unapply))

  def register = Action{
    Ok(views.html.Device.registerDevice(deviceRegisterForm))
  }
  //TODO onComplete...
  def addDevice = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "add device"

    deviceRegisterForm.bindFromRequest.fold(
        formWithErrors =>  Future.successful(BadRequest(views.html.Device.registerDevice(formWithErrors))),

        device =>{
              val newDevice = Device(UUID.randomUUID(),user.username,device.Identifier,device.Activated,device.Subscriptions)
              Device.save(newDevice)
          Future.successful(Redirect("/devices"))
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
      case None => Ok("Error 404") //TODO: Proper 404 Error
    })
  }

  //TODO flatMap or map?
  //TODO Delete
  def listAll = Action.async{
    val f = Device.getAllDevices
    f.map(devices => Ok(views.html.Device.listDevices(devices.toList)))
    //Teoricamente estoy pasando todo su contenido, no iterando
    //Ej: f onSuccess { case posts => for (post <- posts) println(post) }
  }

}