package models

import com.websudos.phantom.Implicits._
import services.Networks

import scala.concurrent.{Future => ScalaFuture}

/**
 * Created by Fer on 02/06/2015.
 */
//In the future it may have more data, for example if it is public or private

//NOTE: Only one network active at a time (Logic rule to keep things simple)
//NOTE: When a new network is created is deactivated by default (see NetworkController)
case class Network(
                    accountID: String,
                    name: String,
                    activated: Boolean) {
}

object Network{

  def insertNewNetwork(network: Network): ScalaFuture[ResultSet] ={
    Networks.insertNewNetwork(network)
  }
  //Small adjustment, instead of having multiple networks per Account, there is only 1 (to make things easier)
  def getNetworks(accountID: String): ScalaFuture[Seq[Network]] ={
    Networks.getAllNetworks(accountID)
  }

  def getNetwork(accountID: String,name: String): ScalaFuture[Option[Network]] ={
    Networks.getNetwork(accountID,name)
  }

  def deleteNetwork(accountID: String,name: String): ScalaFuture[Boolean] ={

    Network.getNetwork(accountID,name) flatMap{
      case Some(network:Network) =>
        for{
          result1: ResultSet <- Networks.deleteNetwork(network.accountID,network.name)
          result2: Boolean <- Follower.deleteAllFollowers(network.accountID,network.name)
        }yield result1.wasApplied() && result2

      case None => ScalaFuture.successful(false)
    }
  }
  //Only one network active at a time, so when a network is activated, the rest are deactivated
  def activateNetwork(accountID: String,name: String): ScalaFuture[Boolean] ={
    // Generate a Combined future: when the first list of futures is finished the second one will take place
    // TODO: Does it correctly?, does it really waits till the deactivation takes place to activate the network???
    // Resp: I think it iterates through all the networks and then send a seq of futures, but it doesnt
    // start to activate until it has send the seq, so it will never deactivate a network that it is been activated
    val deactivation = Network.getNetworks(accountID) map{ networks =>
      for{
        network <- networks
        if network.activated
      }yield Network.deactivateNetwork(accountID,network.name)
    }

    deactivation flatMap{res =>
      Networks.activateNetwork(accountID: String,name: String) map(result => result.wasApplied())
    }
  }

  def deactivateNetwork(accountID: String,name: String): ScalaFuture[Boolean] ={
    Networks.deactivateNetwork(accountID: String,name: String) map(res => res.wasApplied())
  }
}
