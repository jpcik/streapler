package rsp.engine

import scala.util.Random
import scala.concurrent.duration._
import akka.actor._
import rsp.data.{Triple=>RspTriple}
import rsp.data.Iri
import com.typesafe.config.Config
import scala.language.postfixOps

case object StartStream
case object StopStream

trait RspStream extends Actor{
  def receive ={
    case StartStream =>
      startStream
    case StopStream =>
      stopStream
  }  
  def startStream():Unit
  def stopStream():Unit
}

abstract class RateRspStream(reasoner:RspReasoner,uri:String,conf:Config) extends RspStream {
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

  override def startStream()={
    sched=context.system.scheduler.schedule(0 seconds, rate milliseconds){
      produce()
    }(context.dispatcher)
  }
   
  override def stopStream()={
    if (sched!= null && !sched.isCancelled)
      sched cancel      
  }

  def produce():Unit  
}

