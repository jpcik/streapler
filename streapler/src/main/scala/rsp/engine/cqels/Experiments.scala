package rsp.engine.cqels
import com.hp.hpl.jena.graph.Triple
import akka.actor.ActorSystem
import akka.actor.Props
import org.deri.cqels.engine.ConstructListener
import com.typesafe.config.ConfigFactory
import collection.JavaConversions._
import rsp.vocab.Ssn
import rsp.engine.trowl.TrowlReasoner
import rsp.engine.RspReasoner
import rsp.engine.RspListener

object Experiments extends CommonPrefixes{
  type TripleList = java.util.List[Triple]
  val exStreams="http://example.org/streams/"

    val config = ConfigFactory.load.getConfig("experiments.rsp")
    val engine=config.getString("engine")
    val inputConfig = ConfigFactory.load.getConfig("experiments.rsp.input")
    val timeSpan=config.getLong("timeSpan")
    val registerQuery=config.getBoolean("registerQuery")
    val queryIds=config.getIntList("queryIds")
    val enableRewriting=config.getBoolean("enableRewriting")
    val ontology=config.getString("ontology")

  def main(args:Array[String]):Unit={
    
    val reasoner=engine match {
      case "cqels"=>new CqelsReasoner(ontology)
      case "trowl"=>new TrowlReasoner(ontology)
    }
    
    val system=ActorSystem.create("rspSystem")
    val ssw=system.actorOf(Props(new SsnStream(reasoner,exStreams+"s1",inputConfig)))
    val listener=createListener(reasoner)
  
    if (registerQuery)
      reasoner.registerQuery(queries(queryIds(0)-1), listener,enableRewriting)
    ssw ! StartStream
    Thread.sleep(timeSpan)
    println("time: "+timeSpan)
    println("queries: "+reasoner.regQueries  )
    println("input: "+reasoner.inputCount )
    println("output: "+listener.count)
    
    ssw ! StopStream
    system.shutdown
    reasoner.stop    
    System.exit(0)
  }
  
  def createListener(reasoner:RspReasoner)=reasoner match {
    case cqels:CqelsReasoner=>
      val lis=new ConstructListener(cqels.engine ){  
        var count=0L                 
        def update(triples:TripleList):Unit=
          count+=triples.size
      }
      new RspListener(lis){
        override def count=lis.count
      }
    case _ => 
      new RspListener(null) {
        override def count=123L
      }    
    }
  

  def queries={

    def template(bgp:String,construct:String=null) ={
      val const=if (construct==null) s"?s <${met}found> <triple> ."
        else construct    
  	  s"""CONSTRUCT { $const }  
       WHERE { 
         STREAM <${exStreams}s1> [RANGE 0ms]  {
           $bgp        
       }
      }"""
    }  
    val ssn1 =template(
      s"""?s  a <${met}TemperatureObservation>.""")        

    val ssn2 =template(
      s"""?s <${Ssn.iri}observedBy> ?po .
          ?po a <${aws}TemperatureSensor>.
      """)
          
    val ssn3 =template(
      s"""?s  a <${met}AirTemperatureObservation>.        
      """)
      
    val ssn4 =template(  	 
      s"""?t a <${met}HumidityObservation> .   
          ?s a <${met}AirTemperatureObservation> .               
      """,s"?s <${met}found> ?t .")
      
    val ssn5 =template(
      """ ?t a <${met}HumidityObservation> .   
          ?s a <${met}AirTemperatureObservation> .               
          ?i a <${met}RadiationObservation> .               
      """,s"?s <${met}found> ?t ; <${met}found> ?i")
      
    val ssn6 =template(
      s"""?s <${Ssn.iri}observedBy> ?po .
          ?po a <${aws}Thermistor>.           
      """)

    val ssn7 =template(
      s"""?s  a <${ssn}Observation>.        
      """)
      
    List(ssn1,ssn2,ssn3,ssn4,ssn5,ssn6,ssn7)
  }
}

trait CommonPrefixes {
  val met="http://purl.org/env/meteo#"
  val ssn="http://purl.oclc.org/NET/ssnx/ssn#"
  val aws="http://purl.oclc.org/NET/ssnx/meteo/aws#"
  val qudim="http://purl.oclc.org/NET/ssnx/qu/dim#"
  val qu="http://purl.oclc.org/NET/ssnx/qu/qu#"
}


