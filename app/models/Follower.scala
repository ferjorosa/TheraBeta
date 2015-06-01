package models

import java.util.UUID

import com.datastax.driver.core.ResultSet
import services.{Followed, Following}
import scala.concurrent.{Future => ScalaFuture}

/**
 * Created by Fer on 31/05/2015.
 */
// A -(follows)-> B
case class Follower(
                    deviceX: UUID,
                    deviceY: UUID) {

}

object Follower{

   def insertNewFollower(follower:Follower): ScalaFuture[ResultSet] = {
     Following.insertNewFollowing(follower)
     Followed.insertNewFollowed(follower)
   }

  def getAllFollowers(deviceId:UUID): ScalaFuture[Seq[UUID]] = {
    Followed.getFollowersOfDevice(deviceId)
  }

  def getAllDevicesBeingFollowedBy(deviceId:UUID): ScalaFuture[Seq[UUID]] = {
    Following.getFollowingsOfDevice(deviceId)
  }
  //TODO Which ResultSet should return (or both)
  def deleteFollower(follower:Follower): ScalaFuture[ResultSet] = {
    Follower.deleteFollower(follower)
    Following.deleteFollowing(follower)
  }
}
