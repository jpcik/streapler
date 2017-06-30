package rsp.engine

import akka.actor._
import rsp.data._
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.streaming.akka.ActorReceiver

class RspReceiver(feed:String) extends ActorReceiver{
  var count=0
  private var feeder:ActorSelection=null
  override def preStart()={
    println(s"tobo ${self.path}")
    println(s"tobo ${feed}")
    feeder=context.actorSelection(feed)
    if (feeder!=null)
      feeder ! Subscribe(self)
    else println("nothing shakin")
  }
  
  def receive={
    case s:Dataload=>
      store(s)
    case s:Array[Double]=>
      println("pupi: "+s)
      s.foreach(sa=>store(Vectors.dense(Array(sa))))
    case g:Graph=>
      //println("getting graph "+g)
      count+=g.triples.size
      store(g)
    case End=>
      println("counted: "+count )
    case a:Any => println("basura "+ a)
  }
}
