package rsp.engine

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import akka.stream._
import akka.stream.scaladsl._
import akka.NotUsed
import akka.actor.ActorSystem
import concurrent.duration._
import scala.language.postfixOps
import akka.stream.actor.ActorPublisher
import akka.actor.Props
import scala.util.Random
import rsp.io.rml.RmlEngine
import rsp.io.web.JsonWebStream
import rsp.data.RdfStream
import rsp.data.Graph
import rsp.io.web.RandomRdfStream
import rsp.data.Iri
import rsp.query.algebra.TriplePattern
import rsp.query.algebra.BgpOp
import rsp.data.RdfTools
import concurrent.Future
    import rsp.query.algebra.Op._

class AkkaStreamTest extends FlatSpec with Matchers  {
  private val logger= LoggerFactory.getLogger(this.getClass)
  
 "akka Stream" should "materialize" in {
   implicit val system = ActorSystem("QuickStart")
   implicit val materializer = ActorMaterializer()

   import JenaAlgebra._
   import collection.JavaConversions._
   implicit val ctx=system.dispatcher
   
   val t=TriplePattern("papa","p","o")
    val bgp=new BgpOp(Seq(t))
    val jev=new JenaEvaluator(bgp)

   //val map=RmlEngine.readMappings("src/test/resources/citybikes.rml")
    //val props=Map("sourceid"->"metropolradruhr-germany-dortmund")
    //val json=new JsonWebStream(map.head,props)
   val random= new RandomRdfStream(Iri("demo"))
//val s2=Source.fromFuture(json.data)
   val s1=Source.actorPublisher[Graph](Props(new TripPublisher(random)))
   //val source: Source[Int, NotUsed] = Source(1 to 100).throttle(10,1 seconds , 2, ThrottleMode.Shaping)
   val dop=s1.groupedWithin(2000000, 2000 milliseconds)
   
   
   dop.mapAsync(10) { x => 
     Future(x.map { a => jev.evaluate(a).toSeq }) 
     //x.map { a => a.asInstanceOf[Graph] } 
     }
     //.to(Sink.ignore)
   .runForeach { b => println(b.flatten.size) }
     //val bin=i.flatMap { x =>  jev.evaluate(x.asInstanceOf[Graph]).toSeq}
     
     //println(bin)


   
   Thread.sleep(40000)

  }  
}

class TripPublisher(str:RdfStream) extends ActorPublisher[Graph]{
  def receive={
    case i:Int=>
      //println(i)
     // onNext(i)
  }
    private implicit val ctx=context.dispatcher

  override def preStart()={

    val launcher=context.system.scheduler.schedule(1 seconds, 10 milliseconds){   
      //self ! Random.nextInt(20)
      str.data.map{gs=>
        gs.foreach { x => onNext(x)}
      }
      //onNext(Random.nextInt(20))
    }
  }
}