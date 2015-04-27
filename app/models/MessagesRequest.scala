package models

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, JsPath, Writes}

/**
 * Created by Fer on 11/04/2015.
 *
 * Used  in the JSON Message API for query purposes
 * NOTE: I had to maintain the DeviceID because it didn't let me have only a DateTime attribute for the JSON converter
 */

case class MessagesRequest(
                    DeviceID: UUID,
                    EventTime:DateTime)

object MessagesRequest {
  //JSON API Output Converter
  implicit val messageWrites: Writes[MessagesRequest] = (
    (JsPath \ "DeviceID").write[UUID] and
    (JsPath \ "EventTime").write[DateTime]
    )(unlift(MessagesRequest.unapply))

  //JSON API Input Converter
  implicit val messageReads: Reads[MessagesRequest] = (
    (JsPath \ "DeviceID").read[UUID] and
    (JsPath \ "EventTime").read[DateTime]
    )(MessagesRequest.apply _)

}



