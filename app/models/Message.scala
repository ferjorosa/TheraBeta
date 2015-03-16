package models

import java.util.UUID

import com.websudos.phantom.Implicits.ResultSet
import org.joda.time.DateTime
import services.Messages
import scala.concurrent.{Future => ScalaFuture}

/**
 * Created by Fer on 11/03/2015.
 */
case class Message(
                    DeviceID: UUID,
                    EventTime:DateTime,
                    Content: Map[String, String])

object Message{

  def save(message:Message): ScalaFuture[ResultSet] ={
    Messages.insertNewMessage(message)
  }

  def getMessages(deviceID:UUID):ScalaFuture[Seq[Message]] ={
    Messages.getMessagesByDevice(deviceID)
  }
}
