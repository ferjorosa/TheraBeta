package services

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraConnector
import models.Network

import scala.concurrent.{Future => ScalaFuture}


/**
 * Created by Fer on 02/06/2015.
 */
sealed class Networks extends CassandraTable[Networks,Network]{

  object AccountID extends StringColumn(this) with PartitionKey[String]
  object Name extends StringColumn(this) with PrimaryKey[String]
  object Activated extends BooleanColumn(this)

  def fromRow(row: Row): Network ={
    Network(
      AccountID(row),
      Name(row),
      Activated(row)
    )
  }
}

object Networks extends Networks with PhantomCassandraConnector{
  override def tableName = "networks"

  // Insert a new network
  def insertNewNetwork(network: Network): ScalaFuture[ResultSet] ={
    insert
      .value(_.AccountID, network.accountID)
      .value(_.Name,network.name)
      .value(_.Activated, network.activated)
      .future()
  }
  // Get all the networks belonging to an account
  def getAllNetworks(accountID: String): ScalaFuture[Seq[Network]] ={
    select
    .where(_.AccountID eqs accountID)
    .fetch()
  }
  // Get a network by its name
  // TODO: Right not of much use because there are only 2 attributes
  def getNetwork(accountID: String,name: String): ScalaFuture[Option[Network]] ={
    select
    .where(_.AccountID eqs accountID)
    .and(_.Name eqs name)
    .one
  }
  // Delete a network
  def deleteNetwork(accountID: String,name: String): ScalaFuture[ResultSet] ={
    delete
      .where(_.AccountID eqs accountID)
      .and(_.Name eqs name)
      .future()
  }
}
