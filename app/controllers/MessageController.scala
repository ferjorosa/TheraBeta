package controllers

import java.util.UUID

import jp.t2v.lab.play2.auth.AuthElement
import models.{Device, Message, NormalUser}
import org.joda.time.DateTime
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

object MessageController extends AuthConfigImpl with AuthElement{

  /**
   * Lists all the device's messages
   * @param deviceName
   * @return
   */
  def listAllMessages(deviceName: String) = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "list messages"

    Device.getDeviceByIdentifier(user.username,deviceName) flatMap{
      case Some(device: Device)=> Message.getMessages(device.DeviceID) map(messages =>
        Ok(views.html.Message.showMessages(deviceName,messages.toList)))
      case None => Future(Redirect("/error/404"))
    }

  }

  /**
   * Deletes all messages associated to a device
   * @param deviceName
   * @return
   */
  def deleteAll(deviceName:String) = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "delete all messages"

    Device.getDeviceByIdentifier(user.username,deviceName).flatMap{
      case Some(device) => Message.deleteMessages(device.DeviceID).map(res=>
        if(res == true)
          Redirect("/messages/"+device.Identifier).flashing("success"->Messages("showMessages.successfulDeleteAll"))
        else
          Redirect("/error/500")
      )
      case None => Future(Redirect("/error/404"))
    }

  }

  /**
   * Only for test purposes. Inserts a mock message.
   * @param deviceID
   * @return
   */
  def insertMockMessage(deviceID: String) = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "insert mock message"

    val message = Message(UUID.fromString(deviceID), DateTime.now(),Map("test" -> "websockets", "Time" -> DateTime.now().toString))
    Message.save(message)
    Future(Ok("mock message inserted"))

  }

}
