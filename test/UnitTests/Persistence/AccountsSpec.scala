package UnitTests.Persistence

import Utility.CustomSpec
import models.{Account, NormalUser, TestUser}
import org.scalatest.time.SpanSugar._
import services.Accounts

import scala.concurrent.Await

/**
 * Created by Fer on 03/03/2015.
 */
class AccountsSpec extends CustomSpec {

  val registeredAccount = Account("user1", "user1@mail.com", "password",
    "Juanito", "Spain", "+34984567890", NormalUser.value)
  val updatedAccount = Account("user1", "newmail@mail.com", "NEWPASSWORD",
    "Juanito Hernandez Garcia", "France", "+65789990345", TestUser.value)

  "The Account Persistence Layer" should "be able to insert a new account " in {

    Await.ready(Accounts.insertNewAccount(registeredAccount),5 seconds) onFailure{
      case f => fail("Couldn't insert the new account")
    }

  }
    it should "be able to retrieve a specific account by its identifier" in {

      val select = Accounts.getAccountByUsername("user1")

      select onSuccess {
        case account => account match {
          case Some(acc) => assertResult(acc.toString)(registeredAccount.toString)
          case None => fail("No account was retrieved")
        }
      }
      select onFailure{
        case f=> fail("Couldn't retrieve the account")
      }
    }

  it should "be able to update the data of a specific account" in {

    Await.ready(Accounts.updateAccount(registeredAccount.Username,updatedAccount),5 seconds) onFailure{
      case f=> fail("Couldn't update the account")
    }

    val select = Accounts.getAccountByUsername("user1")

    select onSuccess  {
      case account => account match {
        case Some(acc) => assertResult(acc.toString)(updatedAccount.toString)
        case None => fail("No account was retrieved")
      }
    }

    select onFailure {
      case f=> fail("No account was retrieved")
    }

  }

}
