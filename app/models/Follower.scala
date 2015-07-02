package models

import java.util.UUID

import com.datastax.driver.core.ResultSet
import services.{Followed, Following}
import play.api.libs.concurrent.Execution.Implicits._
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
  //TODO Return Boolean
   def insertNewFollower(follower:Follower): ScalaFuture[ResultSet] = {
     Following.insertNewFollowing(follower)
     Followed.insertNewFollowed(follower)
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
  //TODO Return Boolean
  def deleteFollower(follower:Follower): ScalaFuture[ResultSet] = {
    Follower.deleteFollower(follower)
    Following.deleteFollowing(follower)
  }

  def deleteAllFollowers(accountID: String,networkID: String): ScalaFuture[Boolean]={
    for{
      result1 <- Following.deleteAllFollowings(accountID,networkID)
      result2 <- Followed.deleteAllFolloweds(accountID,networkID)
    }yield result1.wasApplied() && result2.wasApplied()
  }
}
