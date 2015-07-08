
package models

import java.util.UUID

import play.api.libs.concurrent.Execution.Implicits._
import services.{Devices, DevicesByAccount}

import scala.concurrent.{Future => ScalaFuture}

case class Device(
                   DeviceID:UUID,
                   AccountID:String,
                   Identifier:String,
                   Activated:Boolean)
object Device{

  //TODO: Persistence should return booleans so there is no possibility of throwing NullPointerExceptions in the for (result2.wasApplied -> result2 == true)
  def save(account: String,device:Device): ScalaFuture[Boolean] ={

    Device.getDeviceByIdentifier(account,device.Identifier) flatMap{
      case Some(deviceRetrieved:Device) => ScalaFuture.successful(false)
      case None => for{
        result1 <- Devices.insertNewDevice(device)
        result2 <- DevicesByAccount.insertNewDevice(device)
      }yield result1.wasApplied() && result2.wasApplied()
    }
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