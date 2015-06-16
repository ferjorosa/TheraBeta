package UnitTests.Model

import Utility.CustomSpec
import models.{NormalUser, Account, TestUser, UserLogin}
import org.scalatest.time.SpanSugar._
import services.Accounts

import scala.concurrent.Await
import scala.util.{Failure, Success}

/**
 * Created by Fer on 03/03/2015.
 */
//TODO integration test?
class AccountSpec extends CustomSpec{

  val account = Account("user", "user@mail.com", "password",
    "Juan", "Spain", "+34984567890", TestUser.value)
  val updatedAccount = Account("user", "newmail@mail.com", "NEWPASSWORD",
    "Juan Hernandez Garcia", "France", "+65789990345", NormalUser.value)
  val userLogin = UserLogin(updatedAccount.username,updatedAccount.password)

  override def beforeAll(){
    super.beforeAll()//CustomSpec's "beforeAll"
    Accounts.insertNewAccount(account)
  }

  "The Account Model" should "be able to retrieve a specific account by its identifier" in{
    Account.findAccountByUsername("user")onComplete{
      case Success(acc) => acc match {
        case Some(a) => assertResult(a.toString)(account.toString)
        case None => fail("No account was retrieved")
      }
      case Failure(f) => fail("Error accessing the DB")
    }
  }
  it should "be able to update a specific account's data"in{
    Await.ready(Accounts.updateAccount("user",updatedAccount),5 seconds) onFailure{
      case f=> fail("Couldn't update the account")
    }

    val select = Accounts.getAccountByUsername("user")

    select onSuccess  {
      case acc => acc match {
        case Some(a) => assertResult(a.toString)(updatedAccount.toString)
        case None => fail("No account was retrieved")
      }
    }

    select onFailure {
      case f=> fail("No account was retrieved")
    }
  }
  it should "be able to check a user's authentication credentials" is (pending)

}
