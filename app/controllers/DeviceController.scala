package controllers

import java.util.UUID

import jp.t2v.lab.play2.auth.AuthElement
import models.{Device, NormalUser}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

object DeviceController extends AuthConfigImpl with AuthElement {

  //Form-mapping used in the registration of a new user's device
  val deviceRegisterForm: Form[Device] = Form(mapping(
    "DeviceID"-> ignored(UUID.randomUUID()),  //static UUID, its defined at the creation of the class
    "OwnerID" -> ignored("default"),
    "Identifier"-> text(minLength = 2, maxLength = 32),
    "Activated"-> ignored(false)
  )(Device.apply)(Device.unapply))

  /**
   * Redirects to the registration page after authorization
   * @return
   */
  def register = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "register device"

    Future{Ok(views.html.Device.registerDevice(deviceRegisterForm))}
  }

  /**
   * Register a new device
   * @return
   */
  def addDevice = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "add device"

    deviceRegisterForm.bindFromRequest.fold(
      formWithErrors =>  Future{BadRequest(views.html.Device.registerDevice(formWithErrors))},

      device =>{
          Device.getDeviceByIdentifier(user.username,device.Identifier) flatMap{
            case Some(device)=> Future(Redirect("/devices/register").flashing("failure"->Messages("registerDevice.deviceAlreadyExists")))
            case None =>{
              val newDevice = Device(UUID.randomUUID(),user.username,device.Identifier,device.Activated)
              Device.save(user.username,newDevice) map{res=>
                if(res)
                  Redirect("/devices").flashing("success"->Messages("registerDevice.successful"))
                else
                  Redirect("/error/500")
              }
            }
          }

        })
  }

  /**
   * Redirects to the device management page
   * @return
   */
  def manageDevices = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "my devices"
    Device.getDevicesByAccountId(user.username)map(
      devices => Ok(views.html.Device.manageDevices(devices.toList)))
  }

  /**
   * Redirects to the device's details page
   * @param identifer
   * @return
   */
  def detailDevice(identifer :String) = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "device detail"

    Device.getDeviceByIdentifier(user.username,identifer)map{
      case Some(device) => Ok(views.html.Device.deviceDetail(device))
      case None => Redirect("/error/404")
    }
  }


  /**
   * Deletes a device by its identifier
   * @param identifier
   * @return
   */
  //TODO: Too much compressed, look addDevice(needs err500)
  def deleteDevice(identifier: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "delete device"

    Device.deleteDevice(user.username,identifier) map{res =>
      if (res == true)
        Redirect("/devices").flashing("success" -> Messages("deviceDetail.successfulDelete"))
      else
        Redirect("/error/404")
    }
  }

  /**
   * Activates a device by its identifier
   * @param identifier
   * @return
   */
  //TODO: Too much compressed, look addDevice(needs err500)
  def activateDevice(identifier: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "activate device"

    Device.activateDevice(user.username,identifier)map { res =>
      if (res == true)
        Redirect("/devices/"+identifier).flashing("success" -> Messages("deviceDetail.successfulActivation"))
      else
        Redirect("/error/404")
    }
  }

  /**
   * Deactivates a device by its identifier
   * @param identifier
   * @return
   */
  //TODO: Too much compressed, It needs a way to distinguish between "the device doesnt exist" (404) and "couldn't delete everything" (500)
  def deactivateDevice(identifier: String) = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "deactivate device"

    Device.deactivateDevice(user.username,identifier)map { res =>
      if (res == true)
        Redirect("/devices/"+identifier).flashing("success" -> Messages("deviceDetail.successfulDeactivation"))
      else
        Redirect("/error/404")
    }
  }
}