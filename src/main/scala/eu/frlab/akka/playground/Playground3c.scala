package eu.frlab.akka.playground

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ClosedShape, Graph}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, Zip}

import scala.concurrent.{ExecutionContextExecutor, Future}

// https://blog.rockthejvm.com/akka-streams-graphs/
object Playground3c extends App {

  implicit val system: ActorSystem = ActorSystem("GraphBasics")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val sink: Sink[(Int, Int), Future[Done]] = Sink.foreach[(Int, Int)](println)

  // step 1 - setting up the fundamentals for the graph
  val graph: Graph[ClosedShape.type, Future[Done]] = GraphDSL.createGraph(sink) { implicit builder =>output => // builder = MUTABLE data structure
        import GraphDSL.Implicits._ // brings some nice operators into scope

        // step 2 - add the necessary components of this graph
        val input = builder.add(Source(1 to 1000))
        val incrementer = builder.add(Flow[Int].map(x => x + 1)) // hard computation
        val multiplier = builder.add(Flow[Int].map(x => x * 10)) // hard computation

        val broadcast = builder.add(Broadcast[Int](2)) // fan-out operator
        val zip = builder.add(Zip[Int, Int]) // fan-in operator

        // step 3 - tying up the components
        input ~> broadcast

        broadcast.out(0) ~> incrementer ~> zip.in0
        broadcast.out(1) ~> multiplier ~> zip.in1

        zip.out ~> output

        // step 4 - return a closed shape
        ClosedShape
    }

  val eventualDone: Future[Done] = RunnableGraph.fromGraph(graph).run()

  eventualDone.onComplete(_ => system.terminate())
}