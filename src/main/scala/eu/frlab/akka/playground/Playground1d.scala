package eu.frlab.akka.playground

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent.{ExecutionContextExecutor, Future}

object Playground1d extends App {

  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val source = Source(1 to 10)

  private val sink1 = Sink.fold[Int, Int](1)(_ * _)
  private val sink2 = Sink.fold[Int, Int](0)(_ + _)

  private val futuresTuple: (Future[Int], Future[Int]) = source
    .alsoToMat(sink1)(Keep.right) // Propagate this value
    .alsoToMat(sink2)((left, right) => (left, right)) // While also calculating and keeping this
    .toMat(Sink.foreach(println))(Keep.left) // And doing also this
    .run()

  private val (future1, future2) = futuresTuple
  val done = Future.sequence(Seq(future1, future2))

  done.onComplete { f =>
    println(f.get)
    system.terminate()
  }

}
