package services

import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraConnector
import models.Follower

import scala.concurrent.{Future => ScalaFuture}

/**
 * Data Mapper class for the Follower model class. Maps the Follower attributes to the associated Cassandra table.
 */
sealed class Following extends CassandraTable[Following,Follower]{

  /** Composite Partition Key */
  object AccountID extends StringColumn(this) with PartitionKey[String]
  /** Composite Partition Key */
  object NetworkID extends StringColumn(this) with PrimaryKey[String]
  /** Device X */
  object DeviceA extends  UUIDColumn(this) with PrimaryKey[UUID]
  /** Device Y */
  object DeviceB extends  UUIDColumn(this) with PrimaryKey[UUID]
  object DeviceA_Name extends StringColumn(this)
  object DeviceB_Name extends StringColumn(this)

  /**
   * Mapping function
   * @param row
   * @return
   */
  def fromRow(row: Row): Follower = {
    Follower(
      AccountID(row),
      NetworkID(row),
      DeviceA(row),
      DeviceB(row),
      DeviceA_Name(row),
      DeviceB_Name(row)
    )
  }
}

/**
 * Companion object (Singleton class) containing the DataBase methods
 */
object Following extends Following with PhantomCassandraConnector{
  override def tableName = "following"

  //Insert new device to be following (A -> B)
  def insertNewFollowing(follower: Follower): ScalaFuture[ResultSet] ={
    insert
      .value(_.AccountID, follower.accountID)
      .value(_.NetworkID, follower.networkID)
      .value(_.DeviceA,follower.deviceX)
      .value(_.DeviceB,follower.deviceY)
      .value(_.DeviceA_Name,follower.deviceX_Name)
      .value(_.DeviceB_Name,follower.deviceY_Name)
      .future()
  }

  /**
   * Get a specific follower
   * @param accountID
   * @param networkID
   * @param deviceX
   * @param deviceY
   * @return
   */
  def getFollower(accountID: String,networkID: String,deviceX: UUID, deviceY: UUID): ScalaFuture[Option[Follower]]={
    select
      .where(_.AccountID eqs accountID)
      .and(_.NetworkID eqs networkID)
      .and(_.DeviceA eqs deviceX)
      .and(_.DeviceB eqs deviceY)
      .one()
  }

  /**
   * Get all the devices the device is following (A -> B ; A -> C)
   * @param accountID
   * @param networkID
   * @param deviceID
   * @return
   */
  def getFollowingsOfDevice(accountID: String,networkID: String,deviceID: UUID): ScalaFuture[Seq[String]] = {
    select(_.DeviceB_Name)
      .where(_.AccountID eqs accountID)
      .and(_.NetworkID eqs networkID)
      .and(_.DeviceA eqs deviceID)
      .fetch()
  }

  /**
   * Get all the 'followings' of a specific network
   * @param accountID
   * @param networkID
   * @return
   */
  def getFollowings(accountID: String,networkID: String): ScalaFuture[Seq[Follower]] = {
    select
      .where(_.AccountID eqs accountID)
      .and(_.NetworkID eqs networkID)
      .fetch()
  }

  /**
   * Delete follower
   * @param follower
   * @return
   */
  def deleteFollowing(follower: Follower): ScalaFuture[ResultSet] ={
    delete
      .where(_.AccountID eqs follower.accountID)
      .and(_.NetworkID eqs follower.networkID)
      .and(_.DeviceA eqs follower.deviceX)
      .and(_.DeviceB eqs follower.deviceY)
      .future()
  }

  /**
   * Delete all followers
   * @param accountID
   * @param networkID
   * @return
   */
  def deleteAllFollowings(accountID: String,networkID: String): ScalaFuture[ResultSet] ={
    delete
      .where(_.AccountID eqs accountID)
      .and(_.NetworkID eqs networkID)
      .future()
  }
}

//Inverse to 'Following'
sealed class Followed extends CassandraTable[Followed,Follower]{
  /** Composite Partition Key */
  object AccountID extends StringColumn(this) with PartitionKey[String]
  /** Composite Partition Key */
  object NetworkID extends StringColumn(this) with PrimaryKey[String]
  /** Device Y */
  object DeviceA extends  UUIDColumn(this) with PrimaryKey[UUID]
  /** Device X */
  object DeviceB extends  UUIDColumn(this) with PrimaryKey[UUID]
  object DeviceA_Name extends StringColumn(this)
  object DeviceB_Name extends StringColumn(this)

  /**
   * Mapping function
   * @param row
   * @return
   */
  def fromRow(row: Row): Follower = {
    Follower(
      AccountID(row),
      NetworkID(row),
      DeviceA(row),
      DeviceB(row),
      DeviceA_Name(row),
      DeviceB_Name(row)
    )
  }
}

/**
 * Companion object (Singleton class) containing the DataBase methods
 */
object Followed extends Followed with PhantomCassandraConnector{
  override def tableName = "followed"

  /**
   * Insert new followed (B <- A)
   * @param follower
   * @return
   */
  def insertNewFollowed(follower: Follower): ScalaFuture[ResultSet] ={
    insert
      .value(_.AccountID, follower.accountID)
      .value(_.NetworkID, follower.networkID)
      .value(_.DeviceA,follower.deviceY)
      .value(_.DeviceB,follower.deviceX)
      .value(_.DeviceA_Name,follower.deviceY_Name)
      .value(_.DeviceB_Name,follower.deviceX_Name)
      .future()
  }

  /**
   * Get all the followers of a device (B <- A ; B <- C)
   * @param accountID
   * @param networkID
   * @param deviceID
   * @return
   */
  def getFollowersOfDevice(accountID: String,networkID: String,deviceID: UUID): ScalaFuture[Seq[String]] = {
    select(_.DeviceB_Name)
      .where(_.AccountID eqs accountID)
      .and(_.NetworkID eqs networkID)
      .and(_.DeviceA eqs deviceID)
      .fetch()
  }

  /**
   * Get all the followers IDs of a device (B <- A ; B <- C)
   * @param accountID
   * @param networkID
   * @param deviceID
   * @return
   */
  def getFollowersIDs(accountID: String,networkID: String,deviceID: UUID): ScalaFuture[Seq[UUID]] = {
    select(_.DeviceB)
      .where(_.AccountID eqs accountID)
      .and(_.NetworkID eqs networkID)
      .and(_.DeviceA eqs deviceID)
      .fetch()
  }

  /**
   * Delete Follower
   * @param follower
   * @return
   */
  def deleteFollowed(follower: Follower): ScalaFuture[ResultSet] ={
    delete
    .where(_.AccountID eqs follower.accountID)
    .and(_.NetworkID eqs follower.networkID)
    .and(_.DeviceA eqs follower.deviceY)
    .and(_.DeviceB eqs follower.deviceX)
    .future()
  }

  /**
   * Delete all followers
   * @param accountID
   * @param networkID
   * @return
   */
  def deleteAllFolloweds(accountID: String,networkID: String): ScalaFuture[ResultSet] ={
    delete
      .where(_.AccountID eqs accountID)
      .and(_.NetworkID eqs networkID)
      .future()
  }
}