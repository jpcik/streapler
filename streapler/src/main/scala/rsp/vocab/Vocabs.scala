package rsp.vocab

import rsp.data._
import rsp.data.RdfTools._

object Rdf extends Vocab{
  override val iri:Iri="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  val a=Rdf("type")
  val _type=a
  val subject=Rdf("subject")
  val predicate=Rdf("predicate")
  val _object=Rdf("object")
  val value=Rdf("value")  
}

object Rdfs extends Vocab{
  override val iri:Iri="http://www.w3.org/2000/01/rdf-schema# "
  val subClassOf=Rdfs("subClassOf")
  val subPropertyOf=Rdfs("subPropertyOf")
  val comment=Rdfs("comment")
  val label=Rdfs("label")
  val domain=Rdfs("domain")
  val range=Rdfs("range")

}

object OmOwl extends Vocab{
  override val iri:Iri="http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#"
  val procedure=iri+"procedure"
  val observedProperty=iri+"observedProperty"
  val floatValue=iri+"floatValue"
  val timestamp=iri+"timestamp"    
}


object Ssn extends Vocab{
  override val iri:Iri="http://purl.oclc.org/NET/ssnx/ssn#"
  val Sensor=Ssn("Sensor")
  val Observation=Ssn("Observation")
  val procedure=Ssn("procedure")
  val observes=Ssn("observes")
  val observedBy=Ssn("observedBy")
  val featureOfInterest=Ssn("featureOfInterest")
  val observedProperty=Ssn("observedProperty")
  
}

object Rsp extends Vocab{
  override val iri:Iri="http://www.w3.org/ns/rsp#"
  val recordedAtTime=Rsp("recordedAtTime")  
}

object Prov extends Vocab{
  override val iri:Iri="http://www.w3.org/ns/prov#"
  val generatedAtTime=Prov("generatedAtTime")  
  val startedAtTime=Prov("startedAtTime")  
  val endedAtTime=Prov("endedAtTime")  
  val atTime=Prov("atTime")  
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
