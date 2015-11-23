package rsp.engine.cqels

import java.util.UUID
import scala.util.Random
import scala.concurrent.duration._
import akka.actor._
import rsp.data.{Triple=>RspTriple}
import rsp.data.Rdf._
import rsp.vocab._

case class StartStream()
case object StopStream

abstract class CqelsStream(cqels:CqelsReasoner,uri:String,rate:Int) extends Actor {
  var sleep=0
  var count=0  
  var sched:Cancellable=null
  
  def stream(s:String,p:String,o:String)={
    count+=1
    cqels.consume(uri,RspTriple(s, p, o))
  }
  def receive ={
    case StartStream =>
      sched=context.system.scheduler.schedule(0 seconds, rate milliseconds){
        //println("papas")
        produce()
      }(context.dispatcher)
      
    case StopStream =>
      if (!sched.isCancelled)
        sched.cancel
      
  }

  def produce():Unit
  
}

