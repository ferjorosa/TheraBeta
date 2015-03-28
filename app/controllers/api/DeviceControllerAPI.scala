package controllers.api

import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.mvc._


object DeviceControllerAPI extends Controller{

  def getAllDevices = Action{

    val d = Map("1" -> "gg","2" -> "gf")//Example using a Map[String, String], Map[String,Any] wont work with this

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
        ),
        Json.arr(d) //TODO check other possibilities
      )
    )


    Ok(json)
  }
}
