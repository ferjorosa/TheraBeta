package services

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{StringColumn, _}
import com.websudos.phantom.iteratee.Iteratee
import models.Account

import scala.concurrent.{Future => ScalaFuture}

sealed class Accounts extends CassandraTable[Accounts,Account]{

  //One object per Column
  object Username extends StringColumn(this)with PartitionKey[String]
  object Email extends StringColumn(this)
  object Password extends StringColumn(this)
  object RealName extends StringColumn(this)
  object Country extends StringColumn(this)
  object PhoneNumber extends StringColumn(this)
  object Role extends StringColumn(this)

  //Mapping function
  def fromRow(row: Row): Account={
    Account(
      Username(row),
      Email(row),
      Password(row),
      RealName(row),
      Country(row),
      PhoneNumber(row),
      Role(row))
  }
}

object Accounts extends Accounts with ExampleConnector{

  //Find by Username(PK)
  def getAccountByUsername(username: String): ScalaFuture[Option[Account]] = {
    select.where(_.Username eqs username).one()
  }
  //FindAll
  def getEntireTable: ScalaFuture[Seq[Account]] = {
    select.fetchEnumerator() run Iteratee.collect()
  }
}
