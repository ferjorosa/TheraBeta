package services

import java.util.UUID

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraConnector
import models.{MessagesRequest, Message}
import scala.concurrent.{Future => ScalaFuture}
import org.joda.time.DateTime

/**
 * Data Mapper class for the Message model class. Maps the Message attributes to the associated Cassandra table.
 */
sealed class Messages extends CassandraTable[Messages, Message]{
  object DeviceID extends  UUIDColumn(this) with PartitionKey[UUID]
  object EventTime extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object Content extends MapColumn[Messages,Message,String,String](this)

  /**
   * Mapping function
   * @param row
   * @return
   */
  def fromRow(row: Row): Message = {
    Message(
    DeviceID(row),
    EventTime(row),
    Content(row)
    )
  }
}

/**
 * Companion object (Singleton class) containing the DataBase methods
 */
object Messages extends Messages with PhantomCassandraConnector{

  override def tableName = "messages"

  /**
   * Insert a new Message
   * @param message
   * @return
   */
  def insertNewMessage(message:Message): ScalaFuture[ResultSet] = {
    insert.value(_.DeviceID,message.deviceID)
      .value(_.EventTime,message.eventTime)
      .value(_.Content,message.content)
      .future()
  }

  /**
   * Get All Messages by DeviceID
   * @param deviceID
   * @return
   */
  def getMessagesByDevice(deviceID:UUID):ScalaFuture[Seq[Message]] = {
    select.where(_.DeviceID eqs deviceID).fetch()
  }

  /**
   * Get All Messages inserted after a requested date for a specific deviceID
   * @param request
   * @return
   */
  def getMessagesByRequest(request:MessagesRequest):ScalaFuture[Seq[Message]] = {
    select.where(_.DeviceID eqs request.DeviceID).and(_.EventTime gt request.EventTime).fetch()
  }

  /**
   * Delete All Messages by DeviceID
   * @param deviceID
   * @return
   */
  def deleteAllMessagesByDevice(deviceID: UUID):ScalaFuture[ResultSet]={
    delete.where(_.DeviceID eqs deviceID).future()
  }
}