package services

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{StringColumn, _}
import com.websudos.phantom.iteratee.Iteratee
import com.websudos.phantom.testing.PhantomCassandraConnector
import models.Account

import scala.concurrent.{Future => ScalaFuture}

/**
 * Data Mapper class for the Account model class. Maps the Account attributes to the associated Cassandra table.
 */
sealed class Accounts extends CassandraTable[Accounts,Account]{

  //One object per Column
  object Username extends StringColumn(this)with PartitionKey[String]
  object Email extends StringColumn(this)
  object Password extends StringColumn(this)
  object RealName extends StringColumn(this)
  object Country extends StringColumn(this)
  object PhoneNumber extends StringColumn(this)
  object Role extends StringColumn(this)

  /**
   * Mapping function
   * @param row
   * @return
   */
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

/**
 * Companion object (Singleton class) containing the DataBase methods
 */
object Accounts extends Accounts with PhantomCassandraConnector{

  override def tableName="accounts"

  /**
   * Register a new account
   * @param account the new account that is going to be inserted in the DB
   * @return a Future containing a Datastax ResultSet
   */
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

  /**
   * Finds an account by its username
   * @param username the account´s username (Partition Key)
   * @return the account if it exists or None if it doesn't.
   */
  def getAccountByUsername(username: String): ScalaFuture[Option[Account]] = {
    select.where(_.Username eqs username).one()
  }

  /**
   * Updates the data stored in teh Database
   * @param oldAccount
   * @param newAccount
   * @return a Future containing a Datastax ResultSet
   */
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

  /**
   * Only for test
   * @return A Sequence of all the accounts registered in the Database
   */
  def getEntireTable: ScalaFuture[Seq[Account]] = {
    select.fetchEnumerator() run Iteratee.collect()
  }
}
