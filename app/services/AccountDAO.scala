package services

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{StringColumn, _}
import com.websudos.phantom.iteratee.Iteratee
import com.websudos.phantom.testing.PhantomCassandraConnector
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

object Accounts extends Accounts with PhantomCassandraConnector{

  override def tableName="accounts"

  //Register a new account
  def insertNewAccount(account:Account): ScalaFuture[ResultSet] ={
    insert.value(_.Username,account.username)
      .value(_.Email, account.email)
      .value(_.Password,account.password)
      .value(_.RealName,account.realName)
      .value(_.Country,account.country)
      .value(_.PhoneNumber,account.phoneNumber)
      .value(_.Role,account.Role)
      .future()
  }

  //Find by Username(PK)
  def getAccountByUsername(username: String): ScalaFuture[Option[Account]] = {
    select.where(_.Username eqs username).one()
  }

  //Update Account's data
  def updateAccount(oldAccount:String,newAccount:Account): ScalaFuture[ResultSet] = {
    update
      .where(_.Username eqs oldAccount)
      .modify(_.Email setTo newAccount.email)
      .and(_.Password setTo newAccount.password)
      .and(_.RealName setTo newAccount.realName)
      .and(_.Country setTo newAccount.country)
      .and(_.PhoneNumber setTo newAccount.phoneNumber)
      .and(_.Role setTo newAccount.Role)
      .future()
  }

  //FindAll
  def getEntireTable: ScalaFuture[Seq[Account]] = {
    select.fetchEnumerator() run Iteratee.collect()
  }
}
