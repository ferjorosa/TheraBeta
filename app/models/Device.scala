
package models

import java.util.UUID
import services.{DevicesByAccount, Devices}
import scala.concurrent.{Future => ScalaFuture}
import com.datastax.driver.core.{ResultSet}

case class Device(
                   DeviceID:UUID,
                   AccountID:String,
                   Identifier:String,
                   Activated:Boolean,
                   Subscriptions:Set[UUID])
object Device{
  //TODO implement service layer / similar if there is more logic
  def save(device:Device): ScalaFuture[ResultSet] ={
    Devices.insertNewDevice(device)
    DevicesByAccount.insertNewDevice(device)
  }

  def getDeviceById(id:UUID):ScalaFuture[Option[Device]] = {
    Devices.getDeviceById(id)
  }

  def getDeviceByIdentifier(Account:String,id:String):ScalaFuture[Option[Device]] ={
    DevicesByAccount.getDeviceByID(Account,id)
  }

  def getDevicesByAccountId(Account:String):ScalaFuture[Seq[Device]] = {
    DevicesByAccount.getDevicesByAccountId(Account)
  }

  def getAllDevices: ScalaFuture[Seq[Device]] = Devices.getEntireTable

  def subscribeDevice(subscriber:Device,subscription:Device):ScalaFuture[ResultSet] = {
    Devices.subscribeDevice(subscriber.DeviceID,subscription.DeviceID)
    DevicesByAccount.subscribeDevice(subscriber.AccountID,subscriber.Identifier,subscription.DeviceID)
  }
  //TODO updateDevice
  /*def activateDevice(device:Device):ScalaFuture[ResultSet] = {
    val dev = Device(device.DeviceID,device.OwnerID,device.Identifier,true,device.Subscriptions)
    Devices.updateDevice(device.DeviceID)
  }*/

}