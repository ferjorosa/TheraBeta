package UnitTests.Persistence

import java.util.UUID

import Utility.CustomSpec
import models.Device
import org.scalatest.time.SpanSugar._
import services.{Devices, DevicesByAccount}

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

/**
 * Created by Fer on 03/03/2015.
 * TODO: Too much boilerplate code
 */
class DevicesSpec  extends CustomSpec{

  "The Device Persistence Layer" should "be able to insert some devices correctly" in{
    val d1 = Device(UUID.randomUUID(),"user1","device1",false,Set.empty[UUID])
    val d2 = Device(UUID.randomUUID(),"user2","device2",false,Set.empty[UUID])

    val r1 = Devices.insertNewDevice(d1)
    val r2 = Devices.insertNewDevice(d2)
    val r3 = DevicesByAccount.insertNewDevice(d1)
    val r4 = DevicesByAccount.insertNewDevice(d2)
    r1.onFailure{case t => fail("Could not insert "+d1.Identifier +" on the Devices table")}
    r2.onFailure{case t => fail("Could not insert "+d2.Identifier + " on the Devices table")}
    r3.onFailure{case t => fail("Could not insert "+d1.Identifier +" on the DevicesByAccount table")}
    r4.onFailure{case t => fail("Could not insert "+d2.Identifier + " on the DevicesByAccount table")}
  }
  /*it should "be able to retrieve an entire table of devices" in {
    Devices.getEntireTable onComplete{
      case Success(table) => assertResult(2)(table.size)
      case Failure(t) => fail("Couldn't retrieve the devices from the DB: "+ t.getMessage)
    }
  }*/
  it should "be able to retrieve all devices belonging to an account" in{
    val res = DevicesByAccount.getDevicesByAccountId("user1")
    res onComplete{
      case Success(table) => {
        //Number of devices retrieved should be 1
        assertResult(1)(table.size)
        //All retrieved devices should have OwnerID = "user1"
        for(row <- table) assertResult("user1")(row.OwnerID)
      }
      case Failure(t) => fail("Couldn't retrieve the devices from the DB: "+ t.getMessage)
    }
  }
  //TODO check not only the right cases, but the wrong cases too
  it should "be able to retrieve a specific device by its identifier" in{
    val res = DevicesByAccount.getDeviceByID("user1","device1")
    res onComplete{
      case Success(device) => device match {
        case Some(d) => {
          assertResult("user1")(d.OwnerID)
          assertResult("device1")(d.Identifier)
        }
        case None => fail("Couldn't retrieve the devices from the DB")
      }
      case Failure(t) => fail("Couldn't retrieve the devices from the DB: "+ t.getMessage)
    }

  }
  it should "be able to activate a specific device" in{
    //TODO getDevice from DB
    val d1 = Device(UUID.randomUUID(),"user1","device1",false,Set.empty[UUID])
    val d2 = Device(d1.DeviceID,d1.OwnerID,d1.Identifier,true,Set.empty[UUID])

    val update = DevicesByAccount.updateDevice(d1,d2)
    update onFailure{
      case failed => fail("Device not updated properly")
    }

    Await.ready(update,5 seconds)

    DevicesByAccount.getDeviceByID(d1.OwnerID,d1.Identifier).onComplete{
      case Success(device) => device match{
        case Some(d)=> assertResult(true)(d.Activated)
        case None => fail("Device not retrieved properly")
      }
      case Failure(t)=> fail("Device not retrieved properly")
    }

  }
  it should "be able to deactivate a specific device" in{
    //TODO getDevice from DB
    val d1 = Device(UUID.randomUUID(),"user1","device1",true,Set.empty[UUID])
    val d2 = Device(d1.DeviceID,d1.OwnerID,d1.Identifier,false,Set.empty[UUID])

    val res = DevicesByAccount.updateDevice(d1,d2)
    res onFailure{
      case failed => fail("Device not updated properly")
    }

    Await.ready(res,5 seconds)

    DevicesByAccount.getDeviceByID(d1.OwnerID,d1.Identifier).onComplete{
      case Success(device) => device match{
        case Some(d)=> assertResult(false)(d.Activated)//TODO: Doesn't fail the test if wrong...(only throw exception)
        case None => fail("Device not retrieved properly")
      }
      case Failure(t)=> fail("Device not retrieved properly")
    }
  }
  //TODO check that the device does not exist on the DB
  it should "be able to delete a specific device by its DeviceID" in{

    val dev = Device(UUID.randomUUID,"user3","device3",false,Set.empty[UUID])
    val insert = Devices.insertNewDevice(dev)
    Await.ready(insert, 5 seconds)

    val get = Devices.getDeviceById(dev.DeviceID)
    get onFailure{
      case failure => fail("Could not get device3")
    }

    Await.ready(get,5 seconds)

    val delete = Devices.deleteDevice(dev.DeviceID)
    delete onFailure{
      case failure => fail("Could not delete device3")
    }

  }
  //TODO check that the device does not exist on the DB
  it should "be able to delete a specific device by its AccountId + Identifier" in{
    val dev = Device(UUID.randomUUID,"user3","device3",false,Set.empty[UUID])
    val insert = DevicesByAccount.insertNewDevice(dev)
    Await.ready(insert, 5 seconds)

    val get = DevicesByAccount.getDeviceByID(dev.OwnerID,dev.Identifier)
    get onFailure{
      case failure => fail("Could not get device3")
    }

    Await.ready(get,5 seconds)

    val delete = DevicesByAccount.deleteDevice(dev.OwnerID,dev.Identifier)
    delete onFailure{
      case failure => fail("Could not delete device3")
    }

  }
  it should "be able to subscribe one device to other device's message channel in 'DevicesByAccount'" in {
    val subscriber = Device(UUID.randomUUID,"subscriber1","SubscribersDevice1",false,Set.empty[UUID])
    val generator = Device(UUID.randomUUID,"subscription1","Device1",false,Set.empty[UUID])

    val insert = Future.sequence(List(
      DevicesByAccount.insertNewDevice(subscriber),
      DevicesByAccount.insertNewDevice(generator))
    )

    Await.ready(insert,5 seconds)

    val subscription = DevicesByAccount.subscribeDevice(subscriber.OwnerID,subscriber.Identifier,generator.DeviceID)

    Await.ready(subscription,5 seconds)

    DevicesByAccount.getDeviceByID(subscriber.OwnerID,subscriber.Identifier) onComplete{
      case Success(device) => device match{
        case Some(d) => assert(d.Subscriptions.contains(generator.DeviceID))
        case None => fail("Device not retrieved properly")
      }
      case Failure(t)=> fail("Device not retrieved properly")
    }

  }
  it should "be able to subscribe one device to other device's message channel in 'Devices'" in {
    val subscriber = Device(UUID.randomUUID,"subscriber2","SubscribersDevice2",false,Set.empty[UUID])
    val generator = Device(UUID.randomUUID,"subscription2","Device2",false,Set.empty[UUID])

    val insert = Future.sequence(List(
      Devices.insertNewDevice(subscriber),
      Devices.insertNewDevice(generator))
    )

    Await.ready(insert,5 seconds)

    val subscription = Devices.subscribeDevice(subscriber.DeviceID,generator.DeviceID)

    Await.ready(subscription,5 seconds)

    Devices.getDeviceById(subscriber.DeviceID) onComplete{
      case Success(device) => device match{
        case Some(d) => assert(d.Subscriptions.contains(generator.DeviceID))
        case None => fail("Device not retrieved properly")
      }
      case Failure(t)=> fail("Device not retrieved properly")
    }

  }


}
