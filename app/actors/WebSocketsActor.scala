package actors

import java.util.UUID

import akka.actor.Actor
import play.Logger
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.{Concurrent, Enumerator}
/**
 * Created by Fer on 26/06/2015.
 */
//Super easy actor-system implementation, i would not scale well in real life (only one actor for thousands of connected messages)...
class WebSocketsActor extends Actor{
  // this map relates every device with his DeviceChannel
  // var...
  var webSockets = Map[UUID, DeviceChannel]()

  def receive = {

    case StartWebSocket(deviceID)=>
      Logger.info("WebSocketActor - start new socket for "+ deviceID)
      val deviceChannel: DeviceChannel = webSockets.getOrElse(deviceID, {
      val broadcast: (Enumerator[String], Channel[String]) = Concurrent.broadcast[String]
      DeviceChannel(deviceID, broadcast._1, broadcast._2)
      })
      webSockets += (deviceID -> deviceChannel)
      sender ! deviceChannel.enumerator

    //TODO:Falta caso en el que no exista dicho deviceID
    //This will be the most used method. Every time a device receives a message this method should be called...
    case newMessageWS(deviceID,msg) =>
      webSockets.get(deviceID) match {
        case Some(element) => {
          Logger.info("webSocketsActor: new message received from "+deviceID+" sending it to open connection")
          element.channel push msg
        }
        case None => Logger.warn("webSocketsActor: "+deviceID+" doesn't have an open connection")
      }

    case WebSocketClosed(deviceID)=>
      Logger.info("stop current socket for "+ deviceID)
      removeDeviceChannel(deviceID)
  }

  private def removeDeviceChannel(deviceID: UUID) = webSockets -= deviceID
}

case class DeviceChannel(deviceID: UUID,enumerator: Enumerator[String], channel: Channel[String])

sealed trait WebSocketMessage

case class StartWebSocket(deviceID: UUID) extends WebSocketMessage

case class WebSocketClosed(deviceID: UUID)extends WebSocketMessage

case class newMessageWS(deviceID: UUID,msg:String) extends WebSocketMessage