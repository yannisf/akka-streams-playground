package eu.frlab.akka.playground

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, RunnableGraph, Sink, Source}
import akka.{Done, NotUsed}

import scala.concurrent.{ExecutionContextExecutor, Future}

object Playground1b extends App {

  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  //Propagate materialized value from the source
  val source: Source[Int, Future[String]] = Source(1 to 10)
    .mapMaterializedValue(_ => Future.successful("source initialization successful"))
  val done = source.toMat(Sink.foreach(println))(Keep.left).run()

  done.onComplete { s =>
    println(s.get)
    system.terminate()
  }

}
