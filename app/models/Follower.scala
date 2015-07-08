package models

import java.util.UUID

import play.api.libs.concurrent.Execution.Implicits._
import services.{Followed, Following}

import scala.concurrent.{Future => ScalaFuture}

/**
 * Created by Fer on 31/05/2015.
 */
// X -(follows)-> Y
case class Follower(
                    accountID: String,
                    networkID: String,
                    deviceX: UUID,
                    deviceY: UUID,
                    deviceX_Name: String,
                    deviceY_Name: String) {

}

object Follower{

  //TODO: Persistence should return booleans so there is no possibility of throwing NullPointerExceptions in the for (result2.wasApplied -> result2 == true)
  //TODO: Should check if the device exists here too? (Model AND Controller)
   def save(follower:Follower): ScalaFuture[Boolean] = {

    Follower.getFollower(follower.accountID,follower.networkID,follower.deviceX,follower.deviceY) flatMap {
      case Some(followerRetrieved: Follower) => ScalaFuture.successful(false)
      case None => for {
          result1 <- Following.insertNewFollowing(follower)
          result2 <- Followed.insertNewFollowed(follower)
      }yield result1.wasApplied() && result2.wasApplied()
    }
   }

  def getFollower(accountID: String,networkID: String, deviceX: UUID, deviceY: UUID):ScalaFuture[Option[Follower]]= {
    Following.getFollower(accountID,networkID,deviceX,deviceY)
  }

  def getAllFollowers(accountID: String,networkID: String): ScalaFuture[Seq[Follower]] = {
    Following.getFollowings(accountID,networkID)
  }
  //'Followed' Table
  def getAllFollowersOfDevice(accountID: String,networkID: String,deviceID: UUID): ScalaFuture[Seq[String]] = {
    Followed.getFollowersOfDevice(accountID,networkID,deviceID)
  }
  //'Followed' Table
  def getAllFollowersIDs(accountID: String,networkID: String,deviceID: UUID): ScalaFuture[Seq[UUID]] = {
    Followed.getFollowersIDs(accountID,networkID,deviceID)
  }
  //'Following' Table
  def getAllDevicesBeingFollowedBy(accountID: String,networkID: String,deviceID: UUID): ScalaFuture[Seq[String]] = {
    Following.getFollowingsOfDevice(accountID,networkID,deviceID)
  }

  def deleteFollower(follower:Follower): ScalaFuture[Boolean] = {

    Follower.getFollower(follower.accountID,follower.networkID,follower.deviceX,follower.deviceY) flatMap {
      case Some(followerRetrieved: Follower) => for {
        result1 <- Followed.deleteFollowed(follower)
        result2 <- Following.deleteFollowing(follower)
      }yield result1.wasApplied() && result2.wasApplied()

      case None => ScalaFuture.successful(false)
    }


  }

  def deleteAllFollowers(accountID: String,networkID: String): ScalaFuture[Boolean]={
    for{
      result1 <- Following.deleteAllFollowings(accountID,networkID)
      result2 <- Followed.deleteAllFolloweds(accountID,networkID)
    }yield result1.wasApplied() && result2.wasApplied()
  }
}
