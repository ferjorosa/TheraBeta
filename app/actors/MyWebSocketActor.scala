package actors

import akka.actor.{Actor, ActorRef, Props}

/**
 * Created by Fer on 26/06/2015.
 */
//AkkaDoc: Props is a configuration object using in creating an Actor; it is immutable, so it is thread-safe and fully shareable.
object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      out ! ("I received your message: " + msg)
  }
}
