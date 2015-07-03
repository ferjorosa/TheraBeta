package actors

import akka.actor.Actor
import play.Logger
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.{Concurrent, Enumerator}

/**
 * Created by Fer on 26/06/2015.
 */
/*
//AkkaDoc: Props is a configuration object using in creating an Actor; it is immutable, so it is thread-safe and fully shareable.
object TestActor {
  def props() = Props(new TestActor())
}
*/
class TestActor extends Actor{

  // this map relate every device with hisDeviceChannel
  var webSockets = Map[Int, TestChannel]()

  def receive = {
    case StartSocket(deviceID)=>
      Logger.info("start new socket for "+ deviceID)
      val testChannel: TestChannel = webSockets.getOrElse(deviceID, {
        val broadcast: (Enumerator[String], Channel[String]) = Concurrent.broadcast[String]
        TestChannel(deviceID, broadcast._1, broadcast._2)
      })
      webSockets += (deviceID -> testChannel)
      sender ! testChannel.enumerator

    //Falta caso en el que no exista dicho deviceID
    case newMessage(deviceID,msg) =>
      webSockets.get(deviceID).get.channel push msg

    case SocketClosed(deviceID)=>
      Logger.info("stop current socket for "+ deviceID)
      removeTestChannel(deviceID)
  }

  private def removeTestChannel(deviceID: Int) = webSockets -= deviceID
}

case class TestChannel(deviceID: Int,enumerator: Enumerator[String], channel: Channel[String])

sealed trait SocketMessage
//needs an id to identify him
case class StartSocket(deviceID: Int) extends SocketMessage

case class SocketClosed(deviceID: Int)extends SocketMessage

case class newMessage(deviceID: Int,msg:String) extends SocketMessage
