package rsp.io.rml

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype

case class TermType(typeId:String)

object IriType extends TermType("IRI")
object BlankNodeType extends TermType("BlankNode")
object LiteralType extends TermType("Literal")


case class DataSource(uri:String,iterator:Option[String])


trait TermMap{
  
  /*val column:String
  val constant:String
  val template:String
  val termType:TermType
  val language:String
  val datatype:XSDDatatype  */
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
  

  
}

case class Constant(const:String) extends MapValue {
  override val vars=Seq()
}

case class Reference(ref:String) extends MapValue {
  override val vars=Seq(ref)
}

object TermMap{
  def sMapReference(reference:String)={
    //ImplTermMap(Some(reference),None,None,None,None)
    
  }
}

case class SubjectMap(value:MapValue,tType:Option[TermType],
    dtype:Option[XSDDatatype]) extends TermMap 

case class ObjectMap(value:MapValue,
    parentTriplesMap:Option[TriplesMap]) extends TermMap
    
case class PredicateMap(value:MapValue
    ) extends TermMap

case class GraphMap(value:MapValue)
    
case class PredicateObjectMap(pMap:PredicateMap,oMap:ObjectMap)    

case class TriplesMap(uri:String,source:Option[DataSource],
    sMap:SubjectMap,poMaps:Seq[PredicateObjectMap]) {
  
}