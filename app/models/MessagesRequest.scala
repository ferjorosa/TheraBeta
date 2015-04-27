package models

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, JsPath, Writes}

/**
 * Created by Fer on 11/04/2015.
 *
 * Used  in the Message API for query purposes
 */

case class MessageDate(
                    DeviceID: UUID,
                    EventTime:DateTime)

object MessageDate {
  //JSON API Output Converter
  implicit val messageWrites: Writes[MessageDate] = (
    (JsPath \ "DeviceID").write[UUID] and
    (JsPath \ "EventTime").write[DateTime]
    )(unlift(MessageDate.unapply))

  //JSON API Input Converter
  implicit val messageReads: Reads[MessageDate] = (
    (JsPath \ "DeviceID").read[UUID] and
    (JsPath \ "EventTime").read[DateTime]
    )(MessageDate.apply _)

}



