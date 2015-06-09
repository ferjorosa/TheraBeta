package controllers

import java.util.UUID

import jp.t2v.lab.play2.auth.AuthElement
import models.{Device, Message, NormalUser}
import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Created by Fer on 05/06/2015.
 */
object MessageController extends AuthConfigImpl with AuthElement{
  //TODO: Not the best approach but it works
  def listAllMessages(deviceName: String) = AsyncStack(AuthorityKey -> NormalUser){implicit request =>{
    val user = loggedIn
    val title = "list messages"

    val result = for{
      device <- Device.getDeviceByIdentifier(user.username,deviceName)
      if(device isDefined)
      messages <- Message.getMessages(device.get.DeviceID)//TODO: Why the need of get?
    } yield messages.toList

    //TODO: the for throws NoSuchElementException when the device doesnt Exist (isDefined?)
    result.map(messages => Ok(views.html.Message.showMessages(deviceName,messages.toList)))
    }
  }

  def insertMockMessage(deviceID: String) = AsyncStack(AuthorityKey -> NormalUser){implicit request =>{
    val user = loggedIn
    val title = "insert mock message"

    val message = Message(UUID.fromString(deviceID), DateTime.now(),Map("test" -> "true", "Wow" -> "GG WP"))
    Message.save(message)
    Future.successful(Ok("mock messageinserted"))
    }
  }

}
