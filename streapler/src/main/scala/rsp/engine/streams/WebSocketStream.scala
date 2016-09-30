package rsp.engine.streams

import rsp.engine.RspReasoner
import rsp.engine.RspStream
import com.typesafe.config.Config
import org.jfarcand.wcs.WebSocket
import org.jfarcand.wcs.TextListener
import org.apache.jena.riot.system.StreamRDF
import org.apache.jena.graph.Triple
import org.apache.jena.sparql.core.Quad
import java.io.StringReader
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr._
import rsp.util.JenaTools._


class WebSocketStream(reasoner:RspReasoner,uri:String,conf:Config) extends RspStream {
  
  class RspStreamRdf extends StreamRDF{
      override def triple(t:Triple){
        //println("dibi "+t)
      }
      override def quad(q:Quad){
        reasoner.consume(uri, q.asTriple)
        //println("qq "+q)
      }
      override def start(){}
      override def finish(){}
      override def base(b:String){}
      override def prefix(p:String,iri:String){}    
  }
  
  val ws=WebSocket().open(uri)  
  
  val streamRdf=new RspStreamRdf
  
  
  override def startStream()={
    ws.listener(new TextListener {
      override def onMessage(message: String) {
        parse(streamRdf, new StringReader(message), Lang.JSONLD)
        
        //println("venga: "+message)
      }
    })    
  }
  
  override def stopStream()={
    ws.close
  }
}