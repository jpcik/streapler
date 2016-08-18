package rsp.io.rml

import scala.collection.mutable.ArrayBuffer
import rsp.data.Iri
import rsp.vocab.Rdf

object RmlPlus {
  
  object R2rml{
    val pref=new collection.mutable.HashMap[String,String]
    val tmaps=new ArrayBuffer[TMap]
    def prefix(p:(String,String))={
      pref+=p
      this
    }
    
    def triplesMap(tm:TMap)={
      tmaps+=tm
      this
    }
 
  }
  
  case class TMap(uri:String,src:Option[LogSource]=None,s:Option[SMap]=None,po:Seq[POMap]=Seq()){
    def subjectMap(ss:SMap):TMap=
      TMap(this.uri,this.src,Some(ss),this.po)
      
    def subjectMap(t:TermMap):TMap=subjectMap(SMap(t))
    
    def poMap(p:TermMap,o:TermMap)=
      TMap(this.uri,this.src,this.s,this.po++Seq(POMap(p,OMap(o))))
      
    def poMap(p:Iri,o:TermMap):TMap=poMap(::constant(p.toString),o)
    def logicalSource(src:String,rf:Formulation)=
      TMap(this.uri,Some(LogSource(src,rf)),this.s,this.po)
  }
  
  trait Formulation
  object CsvPath extends Formulation
  
  case class SMap(term:TermMap)
  case class POMap(pred:TermMap,o:OMap)
  case class OMap(term:TermMap)
  case class TermMap(constant:Option[String],template:Option[String],reference:Option[String])
  case class LogSource(source:String,refFormulation:Formulation)
  
  import language.implicitConversions
  implicit def str2tm(s:String)=TMap(s)
  
  object :: {
    def constant(c:String)=TermMap(Some(c),None,None)
    def template(t:String)=TermMap(None,Some(t),None)
    def reference(r:String)=TermMap(None,None,Some(r))
    def source(s:String)=s
    def referenceFormulation(rf:Formulation)=rf
  }
  
  def create={
    val tmap=TriplesMap("",None,SubjectMap(Template(""),None),Seq())

    R2rml. 
    prefix (":"  ->"http://epfl.ch/mapping/") . 
    prefix ("obs"->"http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#") .
    prefix ("ssw"->"http://knoesis.wright.edu/ssw/") .
    prefix ("weather"->"http://knoesis.wright.edu/ssw/ont/weather.owl#") .
    
    triplesMap {    
      ":UnitMap" .
        subjectMap    (::template "weather:"+"{1}" ) .
        logicalSource (::source   "src/test/resources/data/units.csv",   
                       ::referenceFormulation CsvPath) 
    } .
    
    triplesMap {
      ":MeasureDataMap" .
        subjectMap  (::template "ssw:"+"MeasureData_{2}_{1}_{0}") .
        poMap       (Rdf.a, ::constant "obs:MeasureData") .
        poMap       (Rdf._type, ::constant "bip")
    }
  }
}
/*

@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.
@prefix obs: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>.
@prefix time: <http://www.w3.org/2006/time#>.
@prefix : <http://epfl.ch/mapping/>.

:MeasureDataMap
  rr:subjectMap [ 
    rr:template "http://knoesis.wright.edu/ssw/MeasureData_{2}_{1}_{0}"];
  rr:predicateObjectMap [ 
    rr:predicate rdf:type; rr:objectMap [ rr:constant obs:MeasureData]];
  rr:predicateObjectMap [ 
    rr:predicate obs:floatValue; rr:objectMap [ rml:reference "3"; rr:datatype xsd:float]];
  rr:predicateObjectMap [ 
    rr:predicate obs:uom; rr:objectMap [ rr:parentTriplesMap :UnitMap; rr:child "2"; rr:parent "0" ]].
 

 */  
