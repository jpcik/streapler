package rsp.util

import org.apache.jena.graph.NodeFactory
import org.apache.jena.graph.Triple
import rsp.data.{Triple=>RdfTriple}
import rsp.data.RdfTerm
import rsp.data._
import org.apache.jena.rdf.model.AnonId
import org.apache.jena.graph.Node
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.rdf.model.ResourceFactory
import scala.language.implicitConversions
import org.apache.jena.graph.BlankNodeId
import org.apache.jena.graph.Node_URI

package object JenaTypes {
  type TripleList = java.util.List[Triple]
}

object JenaTools {
  val TTL="TTL"
  import org.apache.jena.graph.NodeFactory._
  import org.apache.jena.datatypes.xsd.XSDDatatype._
  
  def toJenaRes(s:String)=
    ResourceFactory.createResource(s)  
  
  implicit def toIriNode(s:String)=
    NodeFactory.createURI(s)

  implicit def toJenaProperty(iri:Iri)=
    ResourceFactory.createProperty(iri.value)

  implicit def toJenaLit(lit:AnyLiteral)=
    NodeFactory.createLiteral(lit.value.toString)
    
  implicit def toJenaTriple(t:RdfTriple):Triple={
    new Triple(t.subject ,t.predicate ,t._object )
  }
 
  def iri(n:Node)=Iri(n.getURI)
  implicit def fromNode(n:Node)=n match {
    case i:Node_URI=> iri(i.getURI)
    case l: org.apache.jena.rdf.model.Literal =>rsp.data.Literal.lit(l.getString) 
  }
  
  implicit def fromJenaTriple(t:Triple):RdfTriple={
    RdfTriple(iri(t.getSubject),iri(t.getPredicate),t.getObject)
  }
  
  implicit def toJenaNode(t:RdfTerm):Node=t match {
    case i:Iri=>createURI(i.toString)
    case bn:Bnode=>createBlankNode(new BlankNodeId(bn.id))
    case l:Literal=>l.value match {
      case i:Integer=>createLiteral(l.value.toString,XSDinteger)
      case i:Double=>createLiteral(l.value.toString,XSDdouble)
      case i:Boolean=>createLiteral(l.value.toString,XSDboolean)
      case ln:Long=>createLiteral(ln.toString,XSDlong)
      case _=>createLiteral(l.value.toString,XSDstring)
    }
      
  }

}