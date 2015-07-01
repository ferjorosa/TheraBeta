
package models

import java.util.UUID
import services.{DevicesByAccount, Devices}
import scala.concurrent.{Future => ScalaFuture}
import com.datastax.driver.core.{ResultSet}
import play.api.libs.concurrent.Execution.Implicits._

case class Device(
                   DeviceID:UUID,
                   AccountID:String,
                   Identifier:String,
                   Activated:Boolean,
                   Subscriptions:Set[UUID])
object Device{
  //TODO implement service layer / similar if there is more logic
  //TODO: which one should return? (or BOTH)
  def save(device:Device): ScalaFuture[ResultSet] ={

    Devices.insertNewDevice(device)
    DevicesByAccount.insertNewDevice(device)
  }

  def getDeviceById(id:UUID): ScalaFuture[Option[Device]] = {
    Devices.getDeviceById(id)
  }

  def getDeviceByIdentifier(Account:String,id:String): ScalaFuture[Option[Device]] ={
    DevicesByAccount.getDeviceByID(Account,id)
  }

  def getDevicesByAccountId(Account:String): ScalaFuture[Seq[Device]] = {
    DevicesByAccount.getDevicesByAccountId(Account)
  }

  def getAllDevices: ScalaFuture[Seq[Device]] = Devices.getEntireTable

  //TODO we'll see if its necessary to discriminate on Model Level between Devices/DevicesByAccount
  def subscribeDevice(subscriber:Device,subscription:Device):ScalaFuture[ResultSet] = {
    Devices.subscribeDevice(subscriber.DeviceID,subscription.DeviceID)
    DevicesByAccount.subscribeDevice(subscriber.AccountID,subscriber.Identifier,subscription.DeviceID)
  }
  //TODO: Persistence should return booleans so there is no possibility of throwing NullPointerExceptions in the for (result2.wasApplied -> result2 == true)
  def activateDevice(account:String,device:String): ScalaFuture[Boolean] = {

    Device.getDeviceByIdentifier(account,device) flatMap{
      case Some(device:Device) =>
        for{
          result1 <- Devices.activateDevice(device.DeviceID)
          result2 <- DevicesByAccount.activateDevice(device.AccountID,device.Identifier)
        }yield result1.wasApplied() && result2.wasApplied()

      case None => ScalaFuture.successful(false)
    }
  }
  //TODO: Persistence should return booleans so there is no possibility of throwing NullPointerExceptions in the for (result2.wasApplied -> result2 == true)
  def deactivateDevice(account:String,device:String): ScalaFuture[Boolean] = {

    Device.getDeviceByIdentifier(account,device) flatMap{
      case Some(device:Device) =>
        for{
          result1 <- Devices.deactivateDevice(device.DeviceID)
          result2 <- DevicesByAccount.deactivateDevice(device.AccountID,device.Identifier)
        }yield result1.wasApplied() && result2.wasApplied()

      case None => ScalaFuture.successful(false)
    }
  }
  //TODO: It needs a way to distinguish between "the device doesnt exist" (404) and "couldn't delete everything" (500)
  //TODO: Persistence should return booleans so there is no possibility of throwing NullPointerExceptions in the for (result2.wasApplied -> result2 == true)
  def deleteDevice(account:String,device:String):ScalaFuture[Boolean] = {

    Device.getDeviceByIdentifier(account,device) flatMap{
      case Some(device:Device) =>
        for{
          result1 <- Devices.deleteDevice(device.DeviceID)
          result2 <- DevicesByAccount.deleteDevice(device.AccountID,device.Identifier)
          messagesResult <- Message.deleteMessages(device.DeviceID)
        }yield result1.wasApplied() && result2.wasApplied() && messagesResult

      case None => ScalaFuture.successful(false)
    }

  }


}