package rsp.data

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype

trait RdfTerm

case class Iri(value:String) extends RdfTerm{
  override def toString=value
}

trait Literal extends RdfTerm{
  val value:Any
  val datatype:Iri
  val langTag:Option[String]
}

case class AnyLiteral(value:Any) extends Literal {
  import RdfTools._
  lazy val datatype:Iri=value match {
    case s:String =>XSDDatatype.XSDstring
    case Double =>XSDDatatype.XSDdouble
    case Long =>XSDDatatype.XSDlong
    case Int =>XSDDatatype.XSDinteger
    case Boolean =>XSDDatatype.XSDboolean
  }
  val langTag=None
}

class ExtLiteral(anyValue:Any,dtype:Iri,lang:Option[String]) extends Literal{
  override val value=anyValue
  override val datatype=dtype
  override val langTag=lang
}


object Literal{
  import RdfTools._
  def lit(s:Any)=AnyLiteral(s)
  
}

case class Bnode(id:String) extends RdfTerm{
  
}

case class Triple(subject:RdfTerm,predicate:Iri,_object:RdfTerm){
  lazy val s=subject
  lazy val p=predicate
  lazy val o=_object
}


object b{
  def apply(vali:String)=Bnode(vali)
}

case class Graph(name:Option[Iri],triples:Set[Triple]) {
  
}

object Graph{
  def apply(iri:Iri,triples:Triple*):Graph=Graph(Some(iri),triples.toSet)
  def apply(triples:Triple*):Graph=Graph(None,triples.toSet)
}


object RdfTools{
  //implicit def str2lit(s:String)=Literal(s,null,null)
  implicit def str2iri(s:String)=Iri(s)
  implicit def iri2str(iri:Iri)=iri.toString
  implicit def xsd2iri(xsd:XSDDatatype):Iri=xsd.toString
}