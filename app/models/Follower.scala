package models

import com.datastax.driver.core.ResultSet
import services.{Followed, Following}

import scala.concurrent.{Future => ScalaFuture}

/**
 * Created by Fer on 31/05/2015.
 */
// X -(follows)-> Y
case class Follower(
                    accountID: String,
                    networkID: String,
                    deviceX: String,
                    deviceY: String) {

}

object Follower{

   def insertNewFollower(follower:Follower): ScalaFuture[ResultSet] = {
     Following.insertNewFollowing(follower)
     Followed.insertNewFollowed(follower)
   }

  def getAllFollowings(accountID: String,networkID: String,deviceID: String): ScalaFuture[Seq[String]] = {
    Followed.getFollowersOfDevice(accountID,networkID,deviceID)
  }

  def getAllFollowers(accountID: String,networkID: String): ScalaFuture[Seq[Follower]] = {
    Following.getFollowings(accountID,networkID)
  }

  def getAllDevicesBeingFollowedBy(accountID: String,networkID: String,deviceID: String): ScalaFuture[Seq[String]] = {
    Following.getFollowingsOfDevice(accountID,networkID,deviceID)
  }
  //TODO Which ResultSet should return (or both)
  def deleteFollower(follower:Follower): ScalaFuture[ResultSet] = {
    Follower.deleteFollower(follower)
    Following.deleteFollowing(follower)
  }
}
