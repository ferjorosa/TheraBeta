package services

import java.util.UUID

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraConnector
import models.Message
import scala.concurrent.{Future => ScalaFuture}
import org.joda.time.DateTime

/**
 * Created by Fer on 11/03/2015.
 */
sealed class Messages extends CassandraTable[Messages, Message]{
  object DeviceID extends  UUIDColumn(this) with PartitionKey[UUID]
  object EventTime extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object Content extends MapColumn[Messages,Message,String,String](this)

  def fromRow(row: Row): Message = {
    Message(
    DeviceID(row),
    EventTime(row),
    Content(row)
    )
  }
}

object Messages extends Messages with PhantomCassandraConnector{

  override def tableName = "messages"

  //Insert a new Message
  def insertNewMessage(message:Message): ScalaFuture[ResultSet] = {
    insert.value(_.DeviceID,message.DeviceID)
      .value(_.EventTime,message.EventTime)
      .value(_.Content,message.Content)
      .future()
  }
  //Get All Messages by DeviceID
  def getMessagesByDevice(deviceID:UUID):ScalaFuture[Seq[Message]] = {
    select.where(_.DeviceID eqs deviceID).fetch()
  }
}