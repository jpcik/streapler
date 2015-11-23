package rsp.engine

import collection.mutable.ArrayBuffer
import concurrent.duration._
import akka.actor._
import akka.event.Logging
import rsp.data._
import scala.util.Random
import rsp.data.Rdf._
import rsp.data.RdfStream._
import scala.concurrent.Future

trait Feed{
  val stream:RdfStream
  
}



abstract class RspFeed(val stream:RdfStream,rate:Int) extends Feed with Actor{
  val log = Logging(context.system, this)
  private val subscribed=new ArrayBuffer[ActorRef]
  private implicit val ctx=context.dispatcher
  var launcher:Cancellable=null
  
  def produce:Future[Seq[Graph]]
  
  protected def dispatch(g:Graph)={
    //println("dispatching to "+subscribed.size)
    subscribed.foreach{     
      s=>s ! g    
  }}
  
  override def preStart()={
    log.info(s"RspFeed path: ${self.path}")
  } 
  
  def receive={
    case Subscribe(a)=>
      subscribed+=a
    case Unsubscribe(a)=>      
    case Init=>
      println("Init feeder "+rate)
      launcher=context.system.scheduler.schedule(0 seconds, rate milliseconds){   
        produce.map{g=>
          println("to dispatch")
          g.foreach(dispatch)
          
        }
        
      }
    case End=>
      launcher.cancel
      subscribed.foreach{      
        s=>s ! End    
      }
  }  
}

case class End()
case class Init()
case class Subscribe(actor:ActorRef)
case class Unsubscribe(actor:ActorRef)


class RandomRdfStream(override val name:Iri) extends RdfStream{
  import concurrent.ExecutionContext.Implicits.global 
  def data={
    def s:Seq[Triple]={
      (1 to 10).map(i=>Triple("a","p",Random.nextDouble.toString)).toSeq
    }
    val triples=(1 to 1).flatMap{i=>s}
    
    val g=Graph(triples:_*)
    Future(Seq(g))
  }
}

class RandomRspFeed(n:String,rate:Int) extends RspFeed(new RandomRdfStream(n),rate){
  import concurrent.ExecutionContext.Implicits.global 
  override def produce={
    def s:Seq[Triple]={
      (1 to 10).map(i=>Triple("a","p",Random.nextDouble.toString)).toSeq
    }
    val triples=(1 to 1).flatMap{i=>s}
    
    val g=Graph(triples:_*)
    Future(Seq(g))
  }
}
