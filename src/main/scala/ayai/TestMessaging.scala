import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import ayai.networking.messaging._
import ayai.persistence.User

object TestMessaging{
  def main(args: Array[String]) = {
    val u = new User(1, "tim", "tim")

    val pm = new PublicMessage("hello", u)

    val system = ActorSystem("test")

    val mss =  system.actorOf(Props[MessageSendingService], name="mss")

    val mh = new MessageHolder(pm)

    mss ! mh
  }
}