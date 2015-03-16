package models

import com.websudos.phantom.Implicits.ResultSet
import services.Accounts

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

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

  def findUserByUsername(username:String): Future[Option[Account]] = Accounts.getAccountByUsername(username)
  //TODO check return type
  def updateUser(username:String,newAccount:Account):Future[ResultSet] =  Accounts.updateAccount(username,newAccount)

  def authenticate(user:UserLogin): Future[Boolean] ={
    val userRetrieved = Account.findUserByUsername(user.Identifier)

    userRetrieved.map{
      case u => u match{
        case Some(userRetrievedFromDB) => userRetrievedFromDB.Password.equals(user.Password)
        case None => false
      }
    }

  }

}

case class UserLogin(Identifier:String,
                     Password:String) {

}
