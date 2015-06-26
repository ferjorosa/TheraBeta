package controllers.api

import java.util.concurrent.TimeUnit

import actors._
import akka.actor.Props
import akka.pattern._
import akka.util.Timeout
import play.Logger
import play.api.Play.current
import play.api.libs.concurrent.{Akka, Promise}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by Fer on 26/06/2015.
 */
object WebSocketAPI extends Controller{

  val testActor = Akka.system.actorOf(Props[TestActor])

  def socket = WebSocket.acceptWithActor[String, String] { request => out =>
    MyWebSocketActor.props(out)
  }

  // sends the time every second, ignores any input
  def wsTime = WebSocket.async[String] {
    request => Future {
      Logger.info(s"wsTime, client connected.")

      val outEnumerator: Enumerator[String] = Enumerator.repeatM(Promise.timeout(s"${new java.util.Date()}", 1000))
      val inIteratee: Iteratee[String, Unit] = Iteratee.ignore[String]

      (inIteratee, outEnumerator)
    }
  }

  def test_in(deviceID: Int,message :String) = Action {
    testActor ! newMessage(deviceID,message)
    Ok("Mensaje recibido")
  }

  def test_out(deviceID: Int) = WebSocket.async[String] {
    request => {
      Logger.info(s"test_out, client connected.")
      implicit val timeout = Timeout(3,TimeUnit.SECONDS)
      for {
        outEnumerator <- (testActor ? StartSocket(deviceID)) map {
          enumerator => enumerator.asInstanceOf[Enumerator[String]]
        }
        inIteratee = Iteratee.ignore[String] map {
          _ => testActor ! SocketClosed(deviceID)
        }
      } yield (inIteratee, outEnumerator)
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