package models

import java.util.UUID

import com.websudos.phantom.Implicits.ResultSet
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, JsPath, Writes}
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
  //JSON API Output Converter
  implicit val messageWrites: Writes[Message] = (
    (JsPath \ "DeviceID").write[UUID] and
    (JsPath \ "EventTime").write[DateTime] and
    (JsPath \ "Content").write[Map[String,String]]
  )(unlift(Message.unapply))

  //JSON API Input Converter
  implicit  val messageReads: Reads[Message] = (
    (JsPath \ "DeviceID").read[UUID] and
    (JsPath \ "EventTime").read[DateTime] and
    (JsPath \ "Content").read[Map[String,String]]
  )(Message.apply _)

  def save(message:Message): ScalaFuture[ResultSet] ={
    Messages.insertNewMessage(message)
  }

  def getMessages(deviceID:UUID):ScalaFuture[Seq[Message]] ={
    Messages.getMessagesByDevice(deviceID)
  }
}
