package eu.frlab.akka.playground

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.{Done, NotUsed}

import scala.concurrent.{ExecutionContextExecutor, Future}

object Playground1c extends App {

  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val source = Source(1 to 10)

  private val sink1 = Sink.fold[Int, Int](1)(_ * _)
  private val sink2 = Sink.fold[Int, Int](0)(_ + _)

  val done = source
    .alsoToMat(sink1)(Keep.right) // Propagate this value
    .alsoToMat(sink2)(Keep.left)  // While also calculating this
    .toMat(Sink.foreach(println))(Keep.left) // And doing also this
    .run()

  done.onComplete { f =>
    println(f.get)
    system.terminate()
  }

}
