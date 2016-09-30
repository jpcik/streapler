package rsp.engine

import rsp.engine.streams.WebSocketStream
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import rsp.engine.cqels.CqelsEngine
import org.deri.cqels.engine.ConstructListener
import org.apache.jena.graph.Triple

class WebSocketTest  extends FlatSpec with Matchers  {
  type TripleList = java.util.List[Triple]
  private val logger= LoggerFactory.getLogger(this.getClass)
  val conf = ConfigFactory.load.getConfig("experiments.rsp")

  val qq="""CONSTRUCT { ?s ?p ?o }  
            WHERE { STREAM <ws://localhost:4040/primus> [RANGE 0ms]  {?s ?p ?o}}"""
  
  val sys=new RspSystem("wstreams")
  val cqels=new CqelsEngine
  
 "WS Stream" should "put triples" in {
    
 //   val ws=sys.actorOf(Props(new WebSocketStream(cqels,"ws://localhost:4040/primus",conf)))
       
    sys.startStream(Props(new WebSocketStream(cqels,"ws://localhost:4040/primus",conf)))
    cqels.registerQuery(qq, cqels.createListener(lissy))
    
    //ws ! StartStream
     
    Thread.sleep(5000)
    
  }
  
  def lissy(triples:TripleList):Unit={
    println("tikki: "+triples)
  }
  
}