package UnitTests.Persistence

import Utility.CustomSpec
import models.Network
import org.scalatest.time.SpanSugar._
import services.Networks

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

/**
 * Created by Fer on 02/06/2015.
 */
//These test rely on each other. if one the above fails the others behind that one will probably fail

// IMPORTANT: Cassandra doesn't permit duplicates, when you insert the same data on the same table
// and keyspace it will update it, so i do not test against it
class NetworksSpec extends CustomSpec{

  val accountA = "accountA"
  val accountB = "accountB"

  "The Network Persistence Layer" should "be able to insert new networks" in{
    val insertionSequence = Future.sequence(List(
      Networks.insertNewNetwork(Network(accountA,"Network_A",true)),
      Networks.insertNewNetwork(Network(accountA,"Network_B",true)),
      Networks.insertNewNetwork(Network(accountB,"Network_C",true))
    ))

    //Synchronous (only for unit testing))
    Await.ready(insertionSequence, 5 seconds).onFailure{case t => fail("Fail to insert rows in the Networks Table")}
  }

  it should "be able to retrieve a network by its ID" in{
    Networks.getNetwork(accountA,"Network_A") onComplete{
      case Success(network) => network match{
        case Some(n) =>{
          assertResult(n.name)("Network_A")
          assertResult(n.accountID)(accountA)
        }
        case None => fail("Network not retrieved properly")
      }
      case Failure(t)=> fail("Network not retrieved properly")
    }
  }

  it should "be able to retrieve all the networks belonging to an account" in {
    Networks.getAllNetworks(accountA) onComplete {
      case Success(networks) => {
        //Number of networks retrieved should be 2
        assertResult(2)(networks.size)
        //All retrieved networks should have accountID = "accountA"
        for (row <- networks) assertResult(accountA)(row.accountID)
      }
      case Failure(t) => fail("Couldn't retrieve the networks from the DB: " + t.getMessage)
    }
  }

  it should "be able to delete existing networks" in {
    Networks.deleteNetwork(accountB,"Network_C") onFailure{
      case failure => fail("Could not delete the row")
    }
  }

}
