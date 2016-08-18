package rsp.query.algebra

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.apache.jena.sparql.algebra.Algebra
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.vocabulary.RDFS
import rsp.engine.JenaAlgebra
import org.apache.jena.sparql.core.{Var=>JVar}
import org.apache.jena.sparql.core.BasicPattern
import org.apache.jena.sparql.algebra.op.OpBGP
import org.apache.jena.graph.Triple
import java.net.URI
import rsp.data.Iri
import rsp.engine.JenaEvaluator


class AlgebraTest extends FlatSpec with Matchers  {
  private val logger= LoggerFactory.getLogger(this.getClass)
  import rsp.query.algebra.Op._
  import org.apache.jena.sparql.algebra.{Op=>JenaOp}
  
  "uri" should "be parsed" in{
    val uri=new URI("http://products")
    println(uri.getHost)
  }
  
  "bgp" should "load" in{
    val t=TriplePattern("s","p","o")
    val bgp=new BgpOp(Seq(t))
  }
  
  "bgp" should "be executed" in {
     val pp=ModelFactory.createDefaultModel
     pp.add(ResourceFactory.createResource("http://papa.com/topo"),
        RDFS.label , ResourceFactory.createResource("http://popo.org/copo"))
     pp.add(ResourceFactory.createResource("http://papa.com/topo"),
        RDFS.comment, ResourceFactory.createPlainLiteral("3"))        
     import JenaAlgebra._
     val t=TriplePattern("papa","p","o")
     val bgp=new BgpOp(Seq(t))     
     val fil:JenaOp=FilterOp(BinaryXpr(OpEq,VarXpr("o"),ValueXpr("3")),bgp)
     println(fil)
     val jev=new JenaEvaluator(fil)
     
     val qi=jev.evaluate(pp.getGraph)
     qi.hasNext should be (true)
  }

}