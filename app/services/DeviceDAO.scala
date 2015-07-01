package services

import java.util.UUID

import com.datastax.driver.core.{ResultSet, Row}
import com.websudos.phantom.Implicits._
import com.websudos.phantom.iteratee.Iteratee
import com.websudos.phantom.testing.PhantomCassandraConnector
import models.Device

import scala.concurrent.{Future => ScalaFuture}

//We only use Devices table as an example of Cassandra Data Modeling (with de-normalization)
//Update (10-03-2015) We use Devices with the Messages Table
sealed class Devices extends CassandraTable[Devices,Device]{
  object DeviceID extends  UUIDColumn(this) with PartitionKey[UUID]
  object OwnerID extends StringColumn(this)
  object Identifier extends StringColumn(this)
  object Activated extends BooleanColumn(this)
  object Subscriptions extends SetColumn[Devices,Device,UUID](this)

  //Mapping function
  def fromRow(row: Row): Device={
    Device(
      DeviceID(row),
      OwnerID(row),
      Identifier(row),
      Activated(row),
      Subscriptions(row))
  }
}

object Devices  extends Devices with PhantomCassandraConnector{

  override def tableName = "devices"

  //Insert new Device
  def insertNewDevice(device:Device): ScalaFuture[ResultSet] = {
    insert.value(_.DeviceID,device.DeviceID)
      .value(_.OwnerID, device.AccountID)
      .value(_.Identifier,device.Identifier)
      .value(_.Activated,device.Activated)
      .value(_.Subscriptions,device.Subscriptions)
      .future()
  }
  //Find by DeviceID
  def getDeviceById(id: UUID): ScalaFuture[Option[Device]] = {
    select
      .where(_.DeviceID eqs id)
      .one()
  }
  //FindAll
  def getEntireTable: ScalaFuture[Seq[Device]] = {
    select.fetchEnumerator() run Iteratee.collect()
  }
  //Delete Device
  def deleteDevice(id:UUID): ScalaFuture[ResultSet] = {
    delete
    .where(_.DeviceID eqs id)
    .future()
  }
  //You can only update rows that are not part of the Primary Key
  def activateDevice(deviceId:UUID):ScalaFuture[ResultSet] ={
    update
      .where(_.DeviceID eqs deviceId)
      .modify(_.Activated setTo true)
      .future()
  }

  def deactivateDevice(deviceId:UUID):ScalaFuture[ResultSet] ={
    update
      .where(_.DeviceID eqs deviceId)
      .modify(_.Activated setTo false)
      .future()
  }

  //Subscribe
  def subscribeDevice(subscriber:UUID, subscription:UUID):ScalaFuture[ResultSet] = {
    update
      .where(_.DeviceID eqs subscriber)
      .modify(_.Subscriptions add subscription)
      .future()
  }
}

//"Compound Key" Table (One To Many Relation)
sealed class DevicesByAccount extends CassandraTable[DevicesByAccount,Device]{
  object DeviceID extends  UUIDColumn(this) //Probably not useful in the end...
  object AccountID extends StringColumn(this) with PartitionKey[String] //multiple rows can belong to the same PARTITION KEY
  object Identifier extends StringColumn(this) with PrimaryKey[String] //but only one row can belong to the rest of primary
  object Activated extends BooleanColumn(this)
  object Subscriptions extends SetColumn[DevicesByAccount,Device,UUID](this)

  //Mapping function. Order matters.
  def fromRow(row:Row):Device={
    Device(
      DeviceID(row),
      AccountID(row),
      Identifier(row),
      Activated(row),
      Subscriptions(row)

    )
  }

}

object DevicesByAccount extends DevicesByAccount with PhantomCassandraConnector{
  override def tableName = "devicesByAccount"

  //Insert new Device
  def insertNewDevice(device:Device): ScalaFuture[ResultSet] = {
    insert.value(_.DeviceID,device.DeviceID)
      .value(_.AccountID, device.AccountID)
      .value(_.Identifier,device.Identifier)
      .value(_.Activated,device.Activated)
      .value(_.Subscriptions,device.Subscriptions)
      .future()
  }
  //Find all devices by AccountId
  def getDevicesByAccountId(Account:String): ScalaFuture[Seq[Device]] = {
    select.where(_.AccountID eqs Account).fetch()
  }
  //Find all devices from an Account with a specific ID
  def getDeviceByID(Account:String,id:String): ScalaFuture[Option[Device]] = {
    select.where(_.AccountID eqs Account).and(_.Identifier eqs id).one()
  }

  //You can only update rows that are not part of the Primary Key
  def activateDevice(account: String,device: String):ScalaFuture[ResultSet] = {
    update
      .where(_.AccountID eqs account)
      .and(_.Identifier eqs device)
      .modify(_.Activated setTo true)
      .future()
  }

  def deactivateDevice(account: String,device: String):ScalaFuture[ResultSet] = {
    update
      .where(_.AccountID eqs account)
      .and(_.Identifier eqs device)
      .modify(_.Activated setTo false)
      .future()
  }

  //Delete Device
  def deleteDevice(account:String, identifier:String):ScalaFuture[ResultSet] = {
    delete
      .where(_.AccountID eqs account)
      .and(_.Identifier eqs identifier)
      .future()
  }
  //Subscribe
  def subscribeDevice(subscriberAccount:String, subscriberDevice:String,subscription:UUID):ScalaFuture[ResultSet] = {
    update
      .where(_.AccountID eqs subscriberAccount)
      .and(_.Identifier eqs subscriberDevice)
      .modify(_.Subscriptions add subscription)
      .future()
  }
}
