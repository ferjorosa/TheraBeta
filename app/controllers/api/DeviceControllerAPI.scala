package controllers.api

import play.api.libs.json.{JsValue, JsNull, Json}
import play.api.mvc._



object DeviceControllerAPI extends Controller{

  def getAllDevices = Action{
    val json: JsValue = Json.obj(
      "name" -> "Watership Down",
      "location" -> Json.obj("lat" -> 51.235685, "long" -> -1.309197),
      "residents" -> Json.arr(
        Json.obj(
          "name" -> "Fiver",
          "age" -> 4,
          "role" -> JsNull
        ),
        Json.obj(
          "name" -> "Bigwig",
          "age" -> 6,
          "role" -> "Owsla"
        )
      )
    )
    Ok(json)
  }
}
