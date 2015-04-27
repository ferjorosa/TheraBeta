package UnitTests.Persistence

import java.util.UUID

import Utility.CustomSpec
import models.Message
import org.joda.time.DateTime
import org.scalatest.time.SpanSugar._
import services.Messages

import scala.concurrent.Await
import scala.util.{Failure, Success}

/**
 * Created by Fer on 11/03/2015.
 */
class MessagesSpec extends CustomSpec{
  val testUUID = UUID.randomUUID()

  "The Messages Persistence Layer" should "be able to insert new Messages" in{
    //We create a bunch of mock messages
    for(x <- 1 to 4){
      //Synchronous (only for unit testing))
      Await.ready(Messages.insertNewMessage(
        Message(testUUID,DateTime.now(),Map.empty[String,String])//TODO inserta las DateTimes con "Hora estandar romance"
      ),5 seconds)
    }

    val NotEmptyMap = Map("temperature" -> "30", "Unit of measurement" -> "Celsius")
    Await.ready(Messages.insertNewMessage(
      Message(testUUID,DateTime.now(),NotEmptyMap)//TODO inserta las DateTimes con "Hora estandar romance"
    ),5 seconds)

  }
  it should "be able to retrieve all Messages by DeviceID" in {
    Messages.getMessagesByDevice(testUUID) onComplete{
      case Success(devices) => assertResult(5)(devices.size)
      case Failure(t) => fail("There was an error retrieving/inserting Messages")
    }
  }
  //TODO: Pruebas con MessageRequest, para ello modificar las DateTimes introducidas para que no sean Now()
}
