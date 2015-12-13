package rsp.util

import eu.trowl.owlapi3.rel.tms.reasoner.dl.RELReasoner
import org.semanticweb.owlapi.model._
import collection.JavaConversions._
import java.io.File
import scala.language.implicitConversions

object OwlApiTools {

  implicit class TrowlRelReasoner(reasoner:RELReasoner){
    
    def += (axiom:OWLAxiom)=
      reasoner.add(Set(axiom))
  }
  
  implicit class OwlClassPlus(theClass:OWLClass){
    def subClassOf(superclass:OWLClass)(implicit fac:OWLDataFactory)=   
      fac.getOWLSubClassOfAxiom(theClass, superclass)
  }
  implicit class OwlOntologyPlus(onto:OWLOntology){
    def += (axiom:OWLAxiom)(implicit mgr:OWLOntologyManager)=
      mgr.addAxiom(onto, axiom)
  }
  implicit class OwlIndividualPlus(ind:OWLIndividual){
    def ofClass (theclass:OWLClass)(implicit fac:OWLDataFactory)=
      fac.getOWLClassAssertionAxiom(theclass, ind)
    def apply(prop:OWLObjectProperty,obj:OWLNamedIndividual)(implicit fac:OWLDataFactory)={
      fac.getOWLObjectPropertyAssertionAxiom(prop, ind, obj)     
    }
    def apply(t:(OWLObjectProperty,OWLNamedIndividual))(implicit fac:OWLDataFactory)={
      fac.getOWLObjectPropertyAssertionAxiom(t._1 , ind, t._2 )     
    }
  }
  
  implicit def str2Iri(s:String):IRI=IRI.create(s)

  def loadOntology(ontoFile:String)(implicit mgr:OWLOntologyManager)={
    mgr.loadOntologyFromOntologyDocument(new File(ontoFile))
  }
  
  object clazz{
    def apply(iri:String)(implicit fac:OWLDataFactory)=
      fac.getOWLClass(iri)
  }
  
  object ind{
    def apply(iri:String)(implicit fac:OWLDataFactory)=      
      fac.getOWLNamedIndividual(iri)       
  }
  
  object prop{
    def apply(iri:String)(implicit fac:OWLDataFactory)=
      fac.getOWLObjectProperty(iri)
  }
  

}