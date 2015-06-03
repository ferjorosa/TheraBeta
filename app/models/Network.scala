package models

import com.websudos.phantom.Implicits._
import services.Networks
import scala.concurrent.{Future => ScalaFuture}

/**
 * Created by Fer on 02/06/2015.
 */
//In the future it may have more data, for example if it is public or private
case class Network(
                    accountID: String,
                    name: String) {
}

object Network{

  def insertNewNetwork(network: Network): ScalaFuture[ResultSet] ={
    Networks.insertNewNetwork(network)
  }

  def getAllNetworks(accountID: String): ScalaFuture[Seq[Network]] ={
    Networks.getAllNetworks(accountID)
  }

  def getNetwork(accountID: String,name: String): ScalaFuture[Option[Network]] ={
    Networks.getNetwork(accountID,name)
  }

  def deleteNetwork(accountID: String,name: String): ScalaFuture[ResultSet] ={
    Networks.deleteNetwork(accountID,name)
  }
}
