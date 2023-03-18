package eu.frlab.akka.playground

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source

import scala.concurrent.{ExecutionContextExecutor, Future}

object Playground1a extends App {

  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  // https://doc.akka.io/docs/akka/current/stream/stream-quickstart.html
  val source: Source[Int, NotUsed] = Source(1 to 100)
  val done: Future[Done] = source.runForeach(i => println(i))

  done.onComplete(_ => system.terminate())

}
