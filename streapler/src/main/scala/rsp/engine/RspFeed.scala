package rsp.engine

import collection.mutable.ArrayBuffer
import concurrent.duration._
import akka.actor._
import akka.event.Logging
import rsp.data._
import scala.util.Random
import rsp.data.RdfTools._
import rsp.data.RdfStream._
import scala.concurrent.Future
import scala.language.postfixOps

trait Feed{
  val stream:RdfStream  
}

class RspFeed(val stream:RdfStream,rate:Int) extends Feed with Actor{
  val log = Logging(context.system, this)
  private val subscribed=new ArrayBuffer[ActorRef]
  private implicit val ctx=context.dispatcher
  var launcher:Cancellable=null
  
  def produce:Future[Seq[Graph]] = ???
  
  protected def dispatch(g:Graph)={
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
      //println("Init feeder "+rate)
      launcher=context.system.scheduler.schedule(0 seconds, rate milliseconds){   
        produce.map{g=>
          //println("to dispatch")
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

class PullFeed(stream:RdfStream,rate:Int) extends RspFeed(stream,rate){
  override def produce={
    stream.data
  }
}