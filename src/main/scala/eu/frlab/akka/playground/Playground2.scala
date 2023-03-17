package eu.frlab.akka.playground

import akka.actor.ActorSystem
import akka.stream.{Graph, SourceShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Source}
import akka.{Done, NotUsed}

import scala.concurrent.{ExecutionContextExecutor, Future}

object Playground2 extends App {

  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val outer = Outer(
    Seq(Inner(1, b = true), Inner(2, b = true)),
    Seq(Inner(3, b = false), Inner(4, b = false), Inner(5, b = false)),
  )

  // https://doc.akka.io/docs/akka/current/stream/stream-graphs.html
  val graph: Graph[SourceShape[String], NotUsed] = GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val source: Source[Outer, NotUsed] = Source.single(outer)

    val bcast = b.add(Broadcast[Outer](2))
    val merge = b.add(Merge[String](2))

    source ~> bcast.in

    val flowX: Flow[Outer, String, NotUsed] = Flow[Outer].map(_.innerX).mapConcat(i => i.map(ii => s"${ii.i}${ii.b}"))
    val flowY: Flow[Outer, String, NotUsed] = Flow[Outer].map(_.innerY).mapConcat(i => i.map(ii => s"${ii.i}${ii.b}"))

    bcast.out(0) ~> flowX ~> merge.in(0)
    bcast.out(1) ~> flowY ~> merge.in(1)

    SourceShape(merge.out)
  }

  val done: Future[Done] = Source.fromGraph(graph).runForeach(i => println(i))
  done.onComplete(_ => system.terminate())

}

case class Inner(i: Int, b: Boolean)

case class Outer(innerX: Seq[Inner], innerY: Seq[Inner])