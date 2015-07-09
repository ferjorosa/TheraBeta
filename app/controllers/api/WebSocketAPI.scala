package controllers.api

import java.util.UUID
import java.util.concurrent.TimeUnit

import actors._
import akka.pattern._
import akka.util.Timeout
import models.Device
import play.Logger
import play.api.Play.current
import play.api.libs.concurrent.{Akka, Promise}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object WebSocketAPI extends Controller{

  val testActor = Akka.system.actorSelection("akka://application/user/testActor")
  val webSocketsActor = Akka.system.actorSelection("akka://application/user/webSocketsActor")

  /**
   * Test method - echo method
   * @return
   */
  def socket = WebSocket.acceptWithActor[String, String] { request => out =>
    MyWebSocketActor.props(out)
  }

  /**
   * Test method - sends the time every second, ignores any input
   * @return
   */
  def wsTime = WebSocket.async[String] {
    request => Future {
      Logger.info(s"wsTime, client connected.")

      val outEnumerator: Enumerator[String] = Enumerator.repeatM(Promise.timeout(s"${new java.util.Date()}", 1000))
      val inIteratee: Iteratee[String, Unit] = Iteratee.ignore[String]

      (inIteratee, outEnumerator)
    }
  }

  /**
   * Just a mock method to send new messages to the actor to see how it reacts
   * @param deviceID
   * @param message
   * @return
   */
  def test_in(deviceID: Int,message :String) = Action {
    testActor ! newMessage(deviceID,message)
    Ok("Mensaje recibido")
  }

  /**
   * Just a webSocket test connection
   * @param deviceID
   * @return
   */
  def test_out(deviceID: Int) = WebSocket.async[String] {
    request => {
      Logger.info(s"test_out, client connected.")
      implicit val timeout = Timeout(3,TimeUnit.SECONDS)
      //Generates one enumerator
      for {
            outEnumerator <- (testActor ? StartSocket(deviceID)) map {enumerator =>
            enumerator.asInstanceOf[Enumerator[String]]
        }
      //Generates one iteratee that ignores all the messages received (they should be Strings)
      //map acts a trigger when the sockets is closed
        inIteratee = Iteratee.ignore[String] map {
          _ => testActor ! SocketClosed(deviceID)
        }
      } yield (inIteratee, outEnumerator)
    }
  }

  /**
   * The real webSocket connection. Associates a websocket with a UUID (DeviceID)
   * @param stringDeviceID
   * @return
   */
  def connectDevice(stringDeviceID: String) = WebSocket.tryAccept[String]{ request =>
    implicit val timeout = Timeout(3,TimeUnit.SECONDS)
    val deviceID:UUID = UUID.fromString(stringDeviceID)

    Logger.info(s"Websocket - client"+ deviceID +"trying to connect.")

    //First we check if the device exists
    Device.getDeviceById(deviceID).flatMap{
      case Some(device) =>
        for {
          outEnumerator <- (webSocketsActor ? StartWebSocket(deviceID)) map {enumerator =>
            enumerator.asInstanceOf[Enumerator[String]]
        }
        //Generates one iteratee that ignores all the received messages (they should be Strings)
        //map acts as a trigger for the socket is closed
        inIteratee = Iteratee.ignore[String] map {
          _ => webSocketsActor ! WebSocketClosed(deviceID)
        }
      } yield Right(inIteratee, outEnumerator)

      case None => Future{Left(Unauthorized)}
    }
  }
}


/*
(testActor ? StartSocket(deviceID)) map {
  enumerator =>

    // create a Iteratee which ignore the input and
    // and send a SocketClosed message to the actor when
    // connection is closed from the client
    Right((Iteratee.ignore[String] map {
      _ =>
        testActor ! SocketClosed(deviceID)
    }, enumerator.asInstanceOf[Enumerator[String]]))

def test(deviceID: Int) = WebSocket.async[String] {

      Logger.info(s"test_out, client connected.")

      val futureEnumerator: Future[Enumerator[String]] = (testActor ? StartSocket(deviceID)) map{
        enumerator => enumerator.asInstanceOf[Enumerator[String]]
      }
      val inIteratee: Iteratee[String, Unit] = Iteratee.ignore[String] map{
        _ => testActor ! SocketClosed(deviceID)
      }
      val outEnumerator = futureEnumerator map {
        enumerator => enumerator
      }
      (inIteratee, outEnumerator)

}
}*/