package models

import com.websudos.phantom.Implicits._
import services.Accounts

import scala.concurrent.Future

case class Account(
                 Username:String,
                 Email:String,
                 Password:String,
                 RealName:String,
                 Country:String,
                 PhoneNumber:String,
                 Role:String) {
}

object Account{

  //TODO check return type (Success / Failure) or do it on the presentation layer
  def registerNewAccount(account:Account):Future[ResultSet] = Accounts.insertNewAccount(account)

  def findAccountByUsername(username:String): Future[Option[Account]] = Accounts.getAccountByUsername(username)

  def authenticate(user:UserLogin): Future[Boolean] ={
    val userRetrieved = Account.findAccountByUsername(user.Identifier)

    userRetrieved.map{
      case u => u match{
        case Some(userRetrievedFromDB) => userRetrievedFromDB.Password.equals(user.Password)
        case None => false
      }
    }

  }

  //TODO check return type (Success / Failure) or do it on the presentation layer
  def updateUser(username:String,newAccount:Account):Future[ResultSet] =  Accounts.updateAccount(username,newAccount)

}

case class UserLogin(Identifier:String,
                     Password:String) {

}
