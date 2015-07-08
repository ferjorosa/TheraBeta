package models

import com.websudos.phantom.Implicits._
import services.Accounts

import scala.concurrent.Future

/**
 * Model class that represents an Account
 * @param username
 * @param email
 * @param password
 * @param realName
 * @param country
 * @param phoneNumber
 * @param Role
 */
case class Account(
                 username:String,
                 email:String,
                 password:String,
                 realName:String,
                 country:String,
                 phoneNumber:String,
                 Role:String) {
}

object Account{

  /**
   *
   * @param account
   * @return
   */
  //TODO check return type (Success / Failure) or do it on the presentation layer
  def registerNewAccount(account:Account):Future[Boolean] = {
    Accounts.insertNewAccount(account) map(res=>res.wasApplied())
  }

  /**
   *
   * @param username
   * @return
   */
  def findAccountByUsername(username:String): Future[Option[Account]] = Accounts.getAccountByUsername(username)

  /**
   *
   * @param user
   * @return
   */
  def authenticate(user:UserLogin): Future[Boolean] ={
    val userRetrieved = Account.findAccountByUsername(user.Identifier)

    userRetrieved.map{
      case u => u match{
        case Some(userRetrievedFromDB) => userRetrievedFromDB.password.equals(user.Password)
        case None => false
      }
    }

  }

  /**
   *
   * @param username
   * @param newAccount
   * @return
   */
  //TODO check return type (Success / Failure) or do it on the presentation layer
  def updateAccount(username:String,newAccount:Account):Future[Boolean] =  {
    Accounts.updateAccount(username,newAccount) map(res=>res.wasApplied())
  }

}

/**
 *
 * @param Identifier
 * @param Password
 */
case class UserLogin(Identifier:String,
                     Password:String) {

}
