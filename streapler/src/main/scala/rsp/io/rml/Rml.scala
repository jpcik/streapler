package rsp.io.rml

import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.datatypes.RDFDatatype

case class TermType(typeId:String)

object IriType extends TermType("IRI")
object BlankNodeType extends TermType("BlankNode")
object LiteralType extends TermType("Literal")

case class Param(name:String,column:String,map:MapValue)

case class DataSource(uri:String,query:Option[String],params:Seq[Param],
    iterator:Option[String],dataPath:Option[String],timestamp:Option[String],
    isRoot:Boolean,cached:Option[Map[String,Array[String]]])

abstract class TermMap(val value:MapValue,val datatype:Option[RDFDatatype]){
}

trait MapValue {
  //def transform(a:Any):Any
  def vars:Seq[String]
}

case class Template(template:String) extends MapValue{
  override lazy val vars={    
    template.split('{').map{part=>
      val i=part.indexOf('}')
      if (i<0) ""
      else part.substring(0,i)
    }.filter(_.size>0).toSeq
  }
  
  override def toString=template  
}

case class Constant(const:String) extends MapValue {
  override val vars=Seq()
  override def toString=const
}

case class Reference(ref:String) extends MapValue {
  override val vars=Seq(ref)
  override def toString=ref
}

case class Column(col:String) extends MapValue {
  override val vars=Seq(col)
  override def toString=col
}

object TermMap{
  def sMapReference(reference:String)={    
  }
}

case class SubjectMap(v:MapValue,tType:Option[TermType]) extends TermMap(v,None) 

case class ObjectMap(v:MapValue,dtype:Option[RDFDatatype]=None,
    parentTriplesMap:Option[TriplesMap], parent:String,child:String
) extends TermMap(v,dtype)
    
case class PredicateMap(v:MapValue) extends TermMap(v,None)

case class GraphMap(value:MapValue)
    
case class PredicateObjectMap(pMap:PredicateMap,oMap:ObjectMap)    

case class TriplesMap(uri:String,source:Option[DataSource],
    sMap:SubjectMap,poMaps:Seq[PredicateObjectMap]) {
  lazy val refPoMaps=poMaps.filter(_.oMap.parentTriplesMap.isDefined)
}