package rsp.engine.cqels

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import rsp.vocab._
import rsp.data.RdfTools._
import org.deri.cqels.engine.ConstructListener
import akka.actor.ActorSystem
import akka.actor.Props
import java.util.UUID
import scala.util.Random
import com.hp.hpl.jena.graph.Triple
import rsp.engine.rewrite.KyrieRewriter
import com.hp.hpl.jena.vocabulary.RDF
import collection.JavaConversions._
import org.deri.cqels.engine.ExecContext
import scala.concurrent.Future

class CqelsReasonerTest extends FlatSpec with Matchers  {
  private val logger= LoggerFactory.getLogger(this.getClass)
  type TripleList = java.util.List[Triple]
  val exStreams="http://example.org/streams/"
  val met="http://purl.org/env/meteo#"
  val aws="http://purl.oclc.org/NET/ssnx/meteo/aws#"
  val qudim="http://purl.oclc.org/NET/ssnx/qu/dim#"
  val qu="http://purl.oclc.org/NET/ssnx/qu/qu#"
  
//          ?po a <http://purl.oclc.org/NET/ssnx/meteo/aws#TemperatureSensor>.
  
  val ssnQuery3 =s"""
  	CONSTRUCT {?s <http://pop.org/prod> <pipo> . }  
    WHERE { 
      STREAM <${exStreams}s1> [RANGE 200ms]  {
        ?s  a <${met}AirTemperatureObservation>.        
      }

    }"""
  val ssnQuery1 =s"""
  	CONSTRUCT {?s <http://pop.org/prod> <pipo> . }  
    WHERE { 
      STREAM <${exStreams}s1> [RANGE 200ms]  {
        ?s  a <${met}TemperatureObservation>.        
      }
    }"""

  val ssnQuery2 =s"""
  	CONSTRUCT {?s <http://pop.org/prod> <pipo> . }  
    WHERE { 
      STREAM <${exStreams}s1> [RANGE 1ms]  {
        ?s <${Ssn.iri}observedBy> ?po .
        ?po a <${aws}TemperatureSensor>.
      }

    }"""

  val ssnQuery4 =s"""
  	CONSTRUCT {?t <http://pop.org/prod> ?s . }  
    WHERE { 
      STREAM <${exStreams}s1> [RANGE 200ms]  {
        ?t a <${met}HumidityObservation> .   
        ?s a <${met}AirTemperatureObservation> .               
      }

    }"""        

  val ssnQuery =s"""
  	CONSTRUCT {?t <http://pop.org/prod> ?s . }  
    WHERE { 
      STREAM <${exStreams}s1> [RANGE 200ms]  {
        ?t a <${met}HumidityObservation> .   
        ?s a <${met}AirTemperatureObservation> .               
      }

    }"""       
  val tempQuery =s"""
  	CONSTRUCT {?s <http://pop.org/prod> ?po . }  
    WHERE { 
      STREAM <http://example.org/streams/s1> [RANGE 200ms]  {
        ?s a <http://oeg-upm.net/onto/sensordemo/AirTemperatureObservation> .        
      }
    }""" 
//?s a <${Ssn.Observation}> .
  val queryString =s"""
  	CONSTRUCT {?s <http://pop.org/prod> ?o. }  
    WHERE { 
      STREAM <http://example.org/streams/s1> [RANGE 1000ms]  {
        ?s <${OmOwl.timestamp }> ?o.
      }
    }""" 
  
  "Write query" should "write cqels query" in {
    val q=CqelsQueryWriter.readCqels(queryString)
    println(CqelsQueryWriter.writeCqels(q))
  }

  "Rewrite query" should "produce several queries" in {
     val query=CqelsQueryWriter.readCqels(ssnQuery )
    
      val k=new KyrieRewriter("src/test/resources/envsensors.owl")
      val newQ=k.rewriteInUcq(query)
      .map(q=>CqelsQueryWriter.writeCqels(q))
      println("tipi"+newQ)
  }
  
  ignore  should "evaluate stream" in{
    runNativeCqels(exStreams+"s1")
  }
  
 
  
  
  def runNativeCqels(uri:String)={
    val engine= new ExecContext("",false)
    engine.loadDefaultDataset("file:///C:/Users/calbimon/git/rsp-engine/rsp-reasoner/src/test/resources/static.ttl")
    import concurrent.ExecutionContext.Implicits.global
    import rsp.data.{Triple => RspTriple}
      import rsp.util.JenaTools._
    Future {
      while (true){
        println("trala")
        val obs=Ssn("obs"+System.currentTimeMillis)        
        val sens=rsp.data.Iri("http://oeg-upm.net/onto/sensordemo/sens1")//+System.currentTimeMillis)
        engine.engine().send(uri, RspTriple(obs,Ssn.observedBy,sens))
        //engine.engine().send(uri,RspTriple(sens,RDF.`type`.toString,"http://purl.oclc.org/NET/ssnx/meteo/aws#Thermistor"))    
        Thread.sleep(2000)
      }
    }
    
    val listener=new ConstructListener(engine ){
      var count=0
                 
      def update(triples:java.util.List[Triple]):Unit={
        count+=triples.size
        println("dip")
        triples.foreach{t=>
          println(t.getSubject)
        }
      } 
    }
        
    val reg=engine.registerConstruct(ssnQuery)
    reg.register(listener)
         Thread.sleep(10000)

  }
}




