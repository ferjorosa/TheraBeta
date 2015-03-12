
package models

import java.util.UUID
import services.Devices
import scala.concurrent.Future

case class Device(
                   DeviceID:UUID,
                   OwnerID:String,
                   Identifier:String,
                   Activated:Boolean,
                   Subscriptions:Set[UUID])
object Device{

  def save(device:Device): Unit ={

  }

  def list: Future[Seq[Device]] = Devices.getEntireTable

}