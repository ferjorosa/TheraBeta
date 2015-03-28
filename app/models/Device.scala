
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
  //TODO we'll see if its necessary to discriminate on Model Level between Devices/DevicesByAccount
  def subscribeDevice(subscriber:Device,subscription:Device):ScalaFuture[ResultSet] = {
    Devices.subscribeDevice(subscriber.DeviceID,subscription.DeviceID)
    DevicesByAccount.subscribeDevice(subscriber.AccountID,subscriber.Identifier,subscription.DeviceID)
  }
  //TODO updateDevice, all in one execution (two different methods for that)
  def activateDevice(device:Device):ScalaFuture[ResultSet] = {
    val activatedDevice = Device(device.DeviceID,device.AccountID,device.Identifier,true,device.Subscriptions)
    Devices.updateDevice(device.DeviceID, activatedDevice)
    DevicesByAccount.updateDevice(device,activatedDevice)
  }

  def deactivateDevice(device:Device):ScalaFuture[ResultSet] = {
    val activatedDevice = Device(device.DeviceID,device.AccountID,device.Identifier,false,device.Subscriptions)
    Devices.updateDevice(device.DeviceID, activatedDevice)
    DevicesByAccount.updateDevice(device,activatedDevice)
  }

  def deleteDevice(device:Device):ScalaFuture[ResultSet] = {
    Devices.deleteDevice(device.DeviceID)
    DevicesByAccount.deleteDevice(device.AccountID,device.Identifier)
  }

}