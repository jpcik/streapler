package rsp.io.web

import org.jfarcand.wcs.WebSocket
import org.jfarcand.wcs.TextListener
import org.jfarcand.wcs.BinaryListener
import org.apache.jena.rdf.model.RDFReader
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.Lang
import org.apache.jena.riot.system.StreamRDF
import org.apache.jena.graph.Triple
import org.apache.jena.sparql.core.Quad
import java.io.StringReader

object WebSocketDemo {
  val pp="""{"http://www.w3.org/ns/prov#generatedAtTime":"2016-09-16T13:06:07.492Z","@id":"http://localhost:4000/1474031167492","@graph":{"@id":"http://localhost:4000/1474031167492","http://www.w3.org/1999/02/22-rdf-syntax-ns#type":{"@id":"https://schema.org/UpdateAction"},"https://schema.org/agent":{"@id":"http://en.wikipedia.org/wiki/User:Nanny89"},"https://schema.org/object":{"@id":"http://en.wikipedia.org/wiki/Draft:Anamika_Mishra"}}}"""
  def pato()={
    
    RDFDataMgr.parse(new StreamRDF{
      override def triple(t:Triple){
        println("dibi "+t)
      }
      override def quad(q:Quad){
        println("qq "+q)
      }
      override def start(){}
      override def finish(){}
      override def base(b:String){}
      override def prefix(p:String,iri:String){}
      
    }, new StringReader(pp), Lang.JSONLD)
    
    WebSocket().open("ws://localhost:4040/primus")
        .listener(new TextListener {
            override def onMessage(message: String) {
                println("venga: "+message)
            }
        })
        /*.send("Hello World")
        .send("WebSockets are cool!")
        .listener(new BinaryListener {
            override def onMessage(message: Array[Byte]) {
                println("venga: "+message)
            }
        })
        .send("Hello World".getBytes)*/
  }
 
  def main(args:Array[String]):Unit={
    pato
  }
}