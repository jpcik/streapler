package rsp.engine.cqels

import java.util.UUID
import scala.util.Random
import scala.concurrent.duration._
import akka.actor._
import rsp.data.{Triple=>RspTriple}
import rsp.vocab._
import rsp.data.Iri
import com.typesafe.config.Config
import rsp.engine.RspReasoner
import scala.language.postfixOps

case object StartStream
case object StopStream

abstract class RspStream(reasoner:RspReasoner,uri:String,conf:Config) extends Actor {
  var sleep=0
  var count=0  
  var sched:Cancellable=null
  val rate=conf.getInt("rate")
  
  def stream(t:RspTriple):Unit={
    count+=1
    reasoner.consume(uri,t)
  }
  
  def stream(s:Iri,p:Iri,o:Iri):Unit={
    stream(RspTriple(s,p,o))
  }

  def receive ={
    case StartStream =>
      sched=context.system.scheduler.schedule(0 seconds, rate milliseconds){
        produce()
      }(context.dispatcher)
      
    case StopStream =>
      if (sched!= null && !sched.isCancelled)
        sched cancel
      
  }

  def produce():Unit  
}

