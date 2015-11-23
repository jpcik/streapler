package rsp.util

import com.hp.hpl.jena.graph.NodeFactory
import com.hp.hpl.jena.graph.Triple
import rsp.data.{Triple=>RdfTriple}
import rsp.data.RdfTerm
import rsp.data._
import com.hp.hpl.jena.rdf.model.AnonId
import com.hp.hpl.jena.graph.Node
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import com.hp.hpl.jena.rdf.model.ResourceFactory

object JenaTools {
  import com.hp.hpl.jena.graph.NodeFactory._
  import com.hp.hpl.jena.datatypes.xsd.XSDDatatype._
  
  def toJenaRes(s:String)=
    ResourceFactory.createResource(s)  
  
  implicit def toIriNode(s:String)=
    NodeFactory.createURI(s)

  implicit def toJenaProperty(iri:Iri)=
    ResourceFactory.createProperty(iri.value)
    
  implicit def toJenaTriple(t:RdfTriple):Triple={
    new Triple(t.subject ,t.predicate ,t._object )
  }
  
  implicit def toJenaNode(t:RdfTerm):Node=t match {
    case i:Iri=>createURI(i.toString)
    case bn:Bnode=>createAnon(new AnonId(bn.id))
    case l:Literal=>l.value match {
      case i:Integer=>createLiteral(l.value.toString,XSDinteger)
      case i:Double=>createLiteral(l.value.toString,XSDdouble)
      case i:Boolean=>createLiteral(l.value.toString,XSDboolean)
      case ln:Long=>createLiteral(ln.toString,XSDlong)
      case _=>createLiteral(l.value.toString,XSDstring)
    }
      
  }

}