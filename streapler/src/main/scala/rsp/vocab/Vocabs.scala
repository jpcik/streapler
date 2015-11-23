package rsp.vocab

import rsp.data._
import rsp.data.Rdf._

object OmOwl extends Vocab{
  override val iri:Iri="http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#"
  val procedure=iri+"procedure"
  val observedProperty=iri+"observedProperty"
  val floatValue=iri+"floatValue"
  val timestamp=iri+"timestamp"    
}


object Ssn extends Vocab{
  override val iri:Iri="http://purl.oclc.org/NET/ssnx/ssn#"
  val Sensor=iri+"Sensor"
  val Observation=iri+"Observation"
  val procedure=iri+"procedure"
  val observes=iri+"observes"
  val observedBy=iri+"observedBy"
  val featureOfInterest=iri+"featureOfInterest"
  
}

object Rsp extends Vocab{
  override val iri:Iri="http://www.w3.org/ns/rsp#"
  val recordedAtTime=iri+"recordedAtTime"  
}

object Prov extends Vocab{
  override val iri:Iri="http://www.w3.org/ns/prov#"
  val generatedAtTime=iri+"generatedAtTime"  
  val startedAtTime=iri+"startedAtTime"  
  val endedAtTime=iri+"endedAtTime"  
  val atTime=iri+"atTime"  
}

object R2rml extends Vocab{
  override val iri:Iri="http://www.w3.org/ns/r2rml#"
  val subjectMap=R2rml("subjectMap")  
  val parentTriplesMap=R2rml("parentTriplesMap")  
  val predicateObjectMap=R2rml("predicateObjectMap")  
  val predicateMap=R2rml("predicateMap")  
  val objectMap=R2rml("objectMap")  
  val predicate=R2rml("predicate")  
  val template=R2rml("template")  
  val constant=R2rml("constant")  
}

object Rml extends Vocab{
  override val iri:Iri="http://semweb.mmlab.be/ns/rml#"
  val logicalSource=Rml("logicalSource")  
  val source=Rml("source")  
  val reference=Rml("reference")  
  
}
