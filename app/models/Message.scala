package models

import java.util.UUID

import org.joda.time.DateTime

/**
 * Created by Fer on 11/03/2015.
 */
case class Message(
                    DeviceID: UUID,
                    EventTime:DateTime,
                    Content: Map[String, String])

object Message{
  def save(messsage:Message): Unit ={

  }
}
