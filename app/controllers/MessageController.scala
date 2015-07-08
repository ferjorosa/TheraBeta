package controllers

import java.util.UUID

import jp.t2v.lab.play2.auth.AuthElement
import models.{Device, Message, NormalUser}
import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

object MessageController extends AuthConfigImpl with AuthElement{
  //TODO: Not the best approach but it works
  def listAllMessages(deviceName: String) = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "list messages"

    val result = for{
      device <- Device.getDeviceByIdentifier(user.username,deviceName)
      if(device isDefined)
      messages <- Message.getMessages(device.get.DeviceID)//TODO: Why the need of get?
    } yield messages

    //TODO: the for throws NoSuchElementException when the device doesnt Exist (isDefined?)
    result.map(messages => Ok(views.html.Message.showMessages(deviceName,messages.toList)))

  }

  def deleteAll(deviceName:String) = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "delete all messages"

    Device.getDeviceByIdentifier(user.username,deviceName).flatMap{
      case Some(device) => Message.deleteMessages(device.DeviceID).map(res=>
        if(res == true)
          Redirect("/messages/"+device.Identifier).flashing("Success"->"Messages removed correctly")
        else
          BadRequest("/error/500")
      )
      case None => Future.successful(BadRequest("/error/404"))
    }

  }

  def insertMockMessage(deviceID: String) = AsyncStack(AuthorityKey -> NormalUser){implicit request =>
    val user = loggedIn
    val title = "insert mock message"

    val message = Message(UUID.fromString(deviceID), DateTime.now(),Map("test" -> "websockets", "Time" -> DateTime.now().toString()))
    Message.save(message)
    Future.successful(Ok("mock messageinserted"))

  }

}
