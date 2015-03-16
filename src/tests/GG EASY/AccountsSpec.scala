import Utility.CustomSpec
import models.{Account, NormalUser, TestUser}
import org.scalatest.time.SpanSugar._
import services.Accounts

import scala.concurrent.Await
import scala.util.{Failure, Success}

/**
 * Created by Fer on 03/03/2015.
 */
class AccountsSpec extends CustomSpec {

  val registeredAccount = Account("user1", "user1@mail.com", "password",
    "Juanito", "Spain", "+34984567890", NormalUser.value)
  val updatedAccount = Account("user1", "newmail@mail.com", "NEWPASSWORD",
    "Juanito Hernandez Garcia", "France", "+65789990345", TestUser.value)

  "The Account Persistence Layer" should "be able to insert a new account " in {

    val insert = Accounts.insertNewAccount(registeredAccount)

    insert onFailure{
      fail("JODER")
    }

  }
    it should "be able to retrieve a specific account by its identifier" in {

        Accounts.getAccountByUsername("user1") onComplete {
          case Success(account) => account match {
            case Some(acc) => assertResult(acc.toString)(registeredAccount.toString)
            case None => fail("No account was retrieved")
          }
          case Failure(t) =>fail("ddddd")
        }

    }

  it should "be able to update the data of a specific account" in {

      Await.ready(Accounts.updateAccount(registeredAccount,updatedAccount),5 seconds) onFailure{
        case t => fail("JODER.....")
      }

      Accounts.getAccountByUsername("user1").onComplete {
        case Success(account) => account match {
          case Some(acc) => assertResult(acc.toString)(updatedAccount.toString)
          case None => fail("No account was retrieved")
        }
        case Failure(t) => fail("Failure")
      }

  }

}
