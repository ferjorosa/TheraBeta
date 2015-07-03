package models

import java.util.UUID
import java.util.concurrent.TimeUnit

import actors.newMessageWS
import akka.util.Timeout
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, Writes}
import services.Messages

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future => ScalaFuture}

/**
 * Created by Fer on 11/03/2015.
 */
case class Message(
                    deviceID: UUID,
                    eventTime:DateTime,
                    content: Map[String, String]){

  def formattedEventTime: String ={
    val dtf = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
    dtf.print(eventTime)
  }
}

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

  val webSocketsActor = Akka.system.actorSelection("akka://application/user/webSocketsActor")

  //TODO: Too much computation??
  //TODO: Should change Boolean for a self-made Result
  //TODO: Look into this method, lots of things to change (the device is returned in the controller to check if its active, for example)
  def save(message: Message): ScalaFuture[Boolean] ={
    Messages.insertNewMessage(message)

    for {
      device <- Device.getDeviceById(message.deviceID)
      if(device isDefined)
      networks <- Network.getNetworks(device.get.AccountID)
      network <- networks
      if network.activated
      followerIDs <- Follower.getAllFollowersIDs(device.get.AccountID, network.name,message.deviceID)
      followerID <- followerIDs
    }propagateMessage(followerID,message)
    //TODO: map the result insertions, if all good => true; else => false
    ScalaFuture.successful(true)

    /*Device.getDeviceById(message.deviceID) flatMap{
      case Some(device:Device) =>

        val messagePropagation = for{
          networks <- Network.getNetworks(device.AccountID)
          network <- networks
          if network.activated
          followerIDs <- Follower.getAllFollowersIDs(device.AccountID, network.name,message.deviceID)
          followerID <- followerIDs
        }yield Messages.insertNewMessage(Message(followerID,message.eventTime,message.content))

        messagePropagation.flatMap{insertionSeq =>
          for(insert <- insertionSeq)
            if(insert.wasApplied)
              ScalaFuture.successful(true)
            else
              ScalaFuture.successful(false)
        }

      case None => ScalaFuture.successful(false)
    }
    */
  }
  //TODO: Dirty code (Option [Device])
  private def propagateMessage(followerID:UUID,message:Message):Unit = {

    Messages.insertNewMessage(Message(followerID,message.eventTime,message.content))
    //Notify the webSocketsActor that a new message has arrived
    implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))
    Logger.info("Akka: About to send a new message to webSocketsActor")
    webSocketsActor ! newMessageWS(followerID,message.content.toString())


    /*Akka.system.actorSelection("user/" + "webSocketsActor").resolveOne().onComplete {
      case Success(actorRef) => {
        Logger.info("Akka: ready to send a new message to webSocketsActor")
        actorRef ! newMessageWS(device.get.DeviceID,message.content.toString())
      }
      case Failure(ex) => Logger.warn("Akka: user/" + "webSocketsActor" + " does not exist")*/

  }

  def insertMessage(message: Message): ScalaFuture[Boolean] ={
    Messages.insertNewMessage(message).map(res=> res.wasApplied())
  }

  def getMessages(deviceID:UUID):ScalaFuture[Seq[Message]] ={
    Messages.getMessagesByDevice(deviceID)
  }

  def getMessagesByRequest(request:MessagesRequest):ScalaFuture[Seq[Message]] ={
    Messages.getMessagesByRequest(request)
  }

  def deleteMessages(deviceID:UUID):ScalaFuture[Boolean]={
    Messages.deleteAllMessagesByDevice(deviceID).map(res=> res.wasApplied())
  }
}
