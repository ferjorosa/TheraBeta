package services

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraConnector
import models.Network

import scala.concurrent.{Future => ScalaFuture}


/**
 * Data Mapper class for the Network model class. Maps the Network attributes to the associated Cassandra table.
 */
sealed class Networks extends CassandraTable[Networks,Network]{

  object AccountID extends StringColumn(this) with PartitionKey[String]
  object Name extends StringColumn(this) with PrimaryKey[String]
  object Activated extends BooleanColumn(this)

  /**
   * Mapping function
   * @param row
   * @return
   */
  def fromRow(row: Row): Network ={
    Network(
      AccountID(row),
      Name(row),
      Activated(row)
    )
  }
}

/**
 * Companion object (Singleton class) containing the DataBase methods
 */
object Networks extends Networks with PhantomCassandraConnector{
  override def tableName = "networks"

  /**
   * Insert a new network
   * @param network
   * @return
   */
  def insertNewNetwork(network: Network): ScalaFuture[ResultSet] ={
    insert
      .value(_.AccountID, network.accountID)
      .value(_.Name,network.name)
      .value(_.Activated, network.activated)
      .future()
  }

  /**
   * Get all the networks belonging to an account
   * @param accountID
   * @return
   */
  def getAllNetworks(accountID: String): ScalaFuture[Seq[Network]] ={
    select
    .where(_.AccountID eqs accountID)
    .fetch()
  }

  /**
   * Get a network by its name
   * @param accountID
   * @param name
   * @return
   */
  def getNetwork(accountID: String,name: String): ScalaFuture[Option[Network]] ={
    select
    .where(_.AccountID eqs accountID)
    .and(_.Name eqs name)
    .one
  }

  /**
   * Delete a network
   * @param accountID
   * @param name
   * @return
   */
  def deleteNetwork(accountID: String,name: String): ScalaFuture[ResultSet] ={
    delete
      .where(_.AccountID eqs accountID)
      .and(_.Name eqs name)
      .future()
  }

  /**
   * Activate a network
   * @param accountID
   * @param name
   * @return
   */
  def activateNetwork(accountID: String,name: String): ScalaFuture[ResultSet] ={
    update
      .where(_.AccountID eqs accountID)
      .and(_.Name eqs name)
      .modify(_.Activated setTo true)
      .future()
  }

  /**
   * Deactivate a network
   * @param accountID
   * @param name
   * @return
   */
  def deactivateNetwork(accountID: String,name: String): ScalaFuture[ResultSet] ={
    update
      .where(_.AccountID eqs accountID)
      .and(_.Name eqs name)
      .modify(_.Activated setTo false)
      .future()
  }
}
