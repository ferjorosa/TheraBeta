package services

import java.util.UUID

import com.datastax.driver.core.{ResultSet, Row}
import com.websudos.phantom.Implicits._
import com.websudos.phantom.iteratee.Iteratee
import com.websudos.phantom.testing.PhantomCassandraConnector
import models.Device

import scala.concurrent.{Future => ScalaFuture}

/**
 * Data Mapper class for the Device model class. Maps the Device attributes to the associated Cassandra table.
 * We use Devices with the Messages Table
 */
sealed class Devices extends CassandraTable[Devices,Device]{
  object DeviceID extends  UUIDColumn(this) with PartitionKey[UUID]
  object OwnerID extends StringColumn(this)
  object Identifier extends StringColumn(this)
  object Activated extends BooleanColumn(this)

  /**
   * Mapping function
   * @param row
   * @return
   */
  def fromRow(row: Row): Device={
    Device(
      DeviceID(row),
      OwnerID(row),
      Identifier(row),
      Activated(row))
  }
}

/**
 * Companion object (Singleton class) containing the DataBase methods
 */
object Devices  extends Devices with PhantomCassandraConnector{
  //test
  override def tableName = "devices"

  /**
   * Insert new Device
   * @param device
   * @return
   */
  def insertNewDevice(device:Device): ScalaFuture[ResultSet] = {
    insert.value(_.DeviceID,device.DeviceID)
      .value(_.OwnerID, device.AccountID)
      .value(_.Identifier,device.Identifier)
      .value(_.Activated,device.Activated)
      .future()
  }

  /**
   * Find by DeviceID
   * @param id
   * @return
   */
  def getDeviceById(id: UUID): ScalaFuture[Option[Device]] = {
    select
      .where(_.DeviceID eqs id)
      .one()
  }

  /**
   * FindAll
   * @return
   */
  def getEntireTable: ScalaFuture[Seq[Device]] = {
    select.fetchEnumerator() run Iteratee.collect()
  }

  /**
   * Delete Device
   * @param id
   * @return
   */
  def deleteDevice(id:UUID): ScalaFuture[ResultSet] = {
    delete
    .where(_.DeviceID eqs id)
    .future()
  }

  /**
   * Activate device
   * @param deviceId
   * @return
   */
  //You can only update rows that are not part of the Primary Key
  def activateDevice(deviceId:UUID):ScalaFuture[ResultSet] ={
    update
      .where(_.DeviceID eqs deviceId)
      .modify(_.Activated setTo true)
      .future()
  }

  /**
   * Deactivate device
   * @param deviceId
   * @return
   */
  //You can only update rows that are not part of the Primary Key
  def deactivateDevice(deviceId:UUID):ScalaFuture[ResultSet] ={
    update
      .where(_.DeviceID eqs deviceId)
      .modify(_.Activated setTo false)
      .future()
  }
}

/**
 * Data Mapper class for the Device model class. Maps the Device attributes to the associated Cassandra table.
 */
//"Compound Key" Table (One To Many Relation)
sealed class DevicesByAccount extends CassandraTable[DevicesByAccount,Device]{
  object DeviceID extends  UUIDColumn(this) //Probably not useful in the end...
  object AccountID extends StringColumn(this) with PartitionKey[String] //multiple rows can belong to the same PARTITION KEY
  object Identifier extends StringColumn(this) with PrimaryKey[String] //but only one row can belong to the rest of primary
  object Activated extends BooleanColumn(this)

  /**
   * Mapping function. Order matters.
   * @param row
   * @return
   */
  def fromRow(row:Row):Device={
    Device(
      DeviceID(row),
      AccountID(row),
      Identifier(row),
      Activated(row)

    )
  }

}

/**
 * Companion object (Singleton class) containing the DataBase methods
 */
object DevicesByAccount extends DevicesByAccount with PhantomCassandraConnector{
  override def tableName = "devicesByAccount"

  /**
   * Insert new Device
   * @param device
   * @return
   */
  def insertNewDevice(device:Device): ScalaFuture[ResultSet] = {
    insert.value(_.DeviceID,device.DeviceID)
      .value(_.AccountID, device.AccountID)
      .value(_.Identifier,device.Identifier)
      .value(_.Activated,device.Activated)
      .future()
  }

  /**
   * Find all devices by AccountId
   * @param Account
   * @return
   */
  def getDevicesByAccountId(Account:String): ScalaFuture[Seq[Device]] = {
    select.where(_.AccountID eqs Account).fetch()
  }

  /**
   * Find all devices from an Account with a specific ID
   * @param Account
   * @param id
   * @return
   */
  def getDeviceByID(Account:String,id:String): ScalaFuture[Option[Device]] = {
    select.where(_.AccountID eqs Account).and(_.Identifier eqs id).one()
  }

  /**
   * Activate device
   * @param account
   * @param device
   * @return
   */
  //You can only update rows that are not part of the Primary Key
  def activateDevice(account: String,device: String):ScalaFuture[ResultSet] = {
    update
      .where(_.AccountID eqs account)
      .and(_.Identifier eqs device)
      .modify(_.Activated setTo true)
      .future()
  }

  /**
   * Deactivate device
   * @param account
   * @param device
   * @return
   */
  //You can only update rows that are not part of the Primary Key
  def deactivateDevice(account: String,device: String):ScalaFuture[ResultSet] = {
    update
      .where(_.AccountID eqs account)
      .and(_.Identifier eqs device)
      .modify(_.Activated setTo false)
      .future()
  }

  /**
   * Delete Device
   * @param account
   * @param identifier
   * @return
   */
  def deleteDevice(account:String, identifier:String):ScalaFuture[ResultSet] = {
    delete
      .where(_.AccountID eqs account)
      .and(_.Identifier eqs identifier)
      .future()
  }

}
