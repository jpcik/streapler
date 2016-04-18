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
     import JenaAlgebra._
     val t=TriplePattern("papa","p","o")
     val bgp=new BgpOp(Seq(t))
     val jev=new JenaEvaluator(bgp)
     
     jev.evaluate(pp.getGraph)
     
  }

}