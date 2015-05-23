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
                    deviceID: UUID,
                    eventTime:DateTime,
                    content: Map[String, String])

object Message{
  //JSON API Output Converter
  implicit val messageWrites: Writes[Message] = (
    (JsPath \ "deviceID").write[UUID] and
    (JsPath \ "eventTime").write[DateTime] and
    (JsPath \ "content").write[Map[String,String]]
  )(unlift(Message.unapply))

  //JSON API Input Converter
  implicit  val messageReads: Reads[Message] = (
    (JsPath \ "deviceID").read[UUID] and
    (JsPath \ "eventTime").read[DateTime] and
    (JsPath \ "content").read[Map[String,String]]
  )(Message.apply _)

  def save(message:Message): ScalaFuture[ResultSet] ={
    Messages.insertNewMessage(message)
  }

  def getMessages(deviceID:UUID):ScalaFuture[Seq[Message]] ={
    Messages.getMessagesByDevice(deviceID)
  }

  def getMessagesByRequest(request:MessagesRequest):ScalaFuture[Seq[Message]] ={
    Messages.getMessagesByRequest(request)
  }
}
