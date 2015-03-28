package controllers.api

import java.util.UUID

import models.Message
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{BodyParsers, Action, Controller}
import services.Messages

/**
 * Created by Fer on 27/03/2015.
 */
object MessageControllerAPI extends Controller{

  def getMessagesByDeviceID(deviceID:String) = Action.async{

    val result = Messages.getMessagesByDevice(UUID.fromString(deviceID))
    result.map(messages => Ok(Json.toJson(messages)))
    //If no device matches the UUID an IllegalArgumentException will be thrown...
    //TODO: How to address errors producing Err 500 on bad request
  }

  def saveMessage(deviceID:String) = Action(BodyParsers.parse.json){ request =>
    val placeResult = request.body.validate[Message]
    placeResult.fold(
      errors => {
        BadRequest(Json.obj("status" ->"ERROR", "message" -> JsError.toFlatJson(errors)))
      },
      message => {
        Message.save(message)
        Ok(Json.obj("status" ->"OK", "message date" -> message.EventTime ))
      }
    )
  }

}
