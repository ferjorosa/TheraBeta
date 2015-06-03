package services

import java.util.UUID

import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraConnector
import models.Follower

import scala.concurrent.{Future => ScalaFuture}

/**
 * Created by Fer on 31/05/2015.
 */
sealed class Following extends CassandraTable[Following,Follower]{

  object AccountID extends StringColumn(this) with PartitionKey[String] //Composite Partition Key
  object NetworkID extends StringColumn(this) with PartitionKey[String] //Composite Partition Key
  object DeviceA extends  UUIDColumn(this) with PrimaryKey[UUID]
  object DeviceB extends  UUIDColumn(this) with PrimaryKey[UUID]

  def fromRow(row: Row): Follower = {
    Follower(
      AccountID(row),
      NetworkID(row),
      DeviceA(row),
      DeviceB(row)
    )
  }
}

object Following extends Following with PhantomCassandraConnector{
  override def tableName = "following"

  //Insert new device to be following (A -> B)
  def insertNewFollowing(follower: Follower): ScalaFuture[ResultSet] ={
    insert
      .value(_.AccountID, follower.accountID)
      .value(_.NetworkID, follower.networkID)
      .value(_.DeviceA,follower.deviceX)
      .value(_.DeviceB,follower.deviceY)
      .future()
  }
  //Get all the devices the device is following (A -> B ; A -> C)
  def getFollowingsOfDevice(accountID: String,networkID: String,deviceID: UUID): ScalaFuture[Seq[UUID]] = {
    select(_.DeviceB)
      .where(_.AccountID eqs accountID)
      .and(_.NetworkID eqs networkID)
      .and(_.DeviceA eqs deviceID)
      .fetch()
  }
  //Delete
  def deleteFollowing(follower: Follower): ScalaFuture[ResultSet] ={
    delete
      .where(_.AccountID eqs follower.accountID)
      .and(_.NetworkID eqs follower.networkID)
      .and(_.DeviceA eqs follower.deviceX)
      .and(_.DeviceB eqs follower.deviceY)
      .future()
  }
}

//Inverse to 'Following'
sealed class Followed extends CassandraTable[Followed,Follower]{

  object AccountID extends StringColumn(this) with PartitionKey[String] //Composite Partition Key
  object NetworkID extends StringColumn(this) with PartitionKey[String] //Composite Partition Key
  object DeviceA extends  UUIDColumn(this) with PrimaryKey[UUID]
  object DeviceB extends  UUIDColumn(this) with PrimaryKey[UUID]

  def fromRow(row: Row): Follower = {
    Follower(
      AccountID(row),
      NetworkID(row),
      DeviceA(row),
      DeviceB(row)
    )
  }
}

object Followed extends Followed with PhantomCassandraConnector{
  override def tableName = "followed"

  //Insert new followed (B <- A)
  def insertNewFollowed(follower: Follower): ScalaFuture[ResultSet] ={
    insert
      .value(_.AccountID, follower.accountID)
      .value(_.NetworkID, follower.networkID)
      .value(_.DeviceA,follower.deviceY)
      .value(_.DeviceB,follower.deviceX)
      .future()
  }
  //Get all the followers of a device (B <- A ; B <- C)
  def getFollowersOfDevice(accountID: String,networkID: String,deviceID: UUID): ScalaFuture[Seq[UUID]] = {
    select(_.DeviceB)
      .where(_.AccountID eqs accountID)
      .and(_.NetworkID eqs networkID)
      .and(_.DeviceA eqs deviceID)
      .fetch()
  }
  //Delete
  def deleteFollowed(follower: Follower): ScalaFuture[ResultSet] ={
    delete
    .where(_.AccountID eqs follower.accountID)
    .and(_.NetworkID eqs follower.networkID)
    .and(_.DeviceA eqs follower.deviceY)
    .and(_.DeviceB eqs follower.deviceX)
    .future()
  }
}