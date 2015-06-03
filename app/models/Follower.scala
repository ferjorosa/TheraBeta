package models

import java.util.UUID

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
                    deviceX: UUID,
                    deviceY: UUID) {

}

object Follower{

   def insertNewFollower(follower:Follower): ScalaFuture[ResultSet] = {
     Following.insertNewFollowing(follower)
     Followed.insertNewFollowed(follower)
   }

  def getAllFollowers(accountID: String,networkID: String,deviceID: UUID): ScalaFuture[Seq[UUID]] = {
    Followed.getFollowersOfDevice(accountID,networkID,deviceID)
  }

  def getAllDevicesBeingFollowedBy(accountID: String,networkID: String,deviceID: UUID): ScalaFuture[Seq[UUID]] = {
    Following.getFollowingsOfDevice(accountID,networkID,deviceID)
  }
  //TODO Which ResultSet should return (or both)
  def deleteFollower(follower:Follower): ScalaFuture[ResultSet] = {
    Follower.deleteFollower(follower)
    Following.deleteFollowing(follower)
  }
}
