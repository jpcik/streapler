package rsp.engine.trowl

import org.semanticweb.owlapi.apibinding.OWLManager
import eu.trowl.owlapi3.rel.tms.reasoner.dl.RELReasonerFactory
import java.io.File
import rsp.util.OwlApiTools._
import rsp.engine.cqels.CommonPrefixes
import collection.JavaConversions._
import rsp.engine.RspReasoner
import rsp.data.{Triple=>RdfTriple}
import rsp.engine.RspListener
import concurrent.duration._
import scala.collection.mutable.ArrayBuffer
import org.semanticweb.owlapi.model.OWLAxiom

class TrowlReasoner(ontoFile:String) extends RspReasoner{
  implicit val mgr=OWLManager.createOWLOntologyManager
  implicit val fac=mgr.getOWLDataFactory    
  val onto=loadOntology(ontoFile)
  val reasoner = new RELReasonerFactory().createReasoner(onto)
  val memory=new ArrayBuffer[OWLAxiom]

  def stop={    
  }
  
  
  def reason=synchronized{
    reasoner.classify
    clean
  }
  def clean={
    reasoner.clean(memory.toSet)
  }
  
  def consume(uri:String,t:RdfTriple)={
    inputCount+=1
    val subj = ind(t.s.toString)
    val pred = prop(t.p.toString)
    val obj = ind(t.o.toString)
    val trip= subj (pred->obj)
 memory+=trip
    this.synchronized{
    reasoner += trip
    }
  }
  
  def registerQuery(q:String,listener:RspListener,reasoner:Boolean=false)={
  }
}

object RunTrowl extends CommonPrefixes{
  def main(args:Array[String])={
    implicit val mgr=OWLManager.createOWLOntologyManager
    implicit val fac=mgr.getOWLDataFactory
    
    val onto=loadOntology("src/test/resources/envsensors.owl")
    val Thermistor=clazz(aws+"Thermistor")
    val TemperatureSensor=clazz(aws+"TemperatureSensor")
    val Observation=clazz(ssn+"Observation")
    val observedBy=prop(ssn+"observedBy")
    
    val reasoner = new RELReasonerFactory().createReasoner(onto)
    println (reasoner.getInstances(TemperatureSensor, true).size)

    //(1 to 10).foreach{i=>
      reasoner += ind(met+"sens1") ofClass Thermistor
      val obs = ind("obs")
      reasoner += obs (observedBy->ind("abc"))
      reasoner += obs ofClass Observation
      
      reasoner.reclassify
    //}
    println (reasoner.getInstances(Observation, true).size)
  }
}