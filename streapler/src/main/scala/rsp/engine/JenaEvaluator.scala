package rsp.engine

import com.hp.hpl.jena.sparql.algebra.{Op=>JenaOp}
import com.hp.hpl.jena.graph.{Graph=>JenaGraph}
import com.hp.hpl.jena.graph.{Triple=>JenaTriple}
import com.hp.hpl.jena.sparql.engine.binding.{Binding=>JenaBinding}
import com.hp.hpl.jena.sparql.algebra.Algebra
import rsp.query.algebra.Op
import com.hp.hpl.jena.sparql.algebra.op.OpBGP
import rsp.query.algebra._
import rsp.data._
import com.hp.hpl.jena.graph.NodeFactory
import com.hp.hpl.jena.graph.Node
import rsp.query.algebra.TriplePattern
import rsp.query.algebra.PatternTerm
import rsp.query.algebra.Var
import com.hp.hpl.jena.sparql.util.graph.GraphFactory
import collection.JavaConversions._
import org.slf4j.LoggerFactory
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.vocabulary.RDFS
import com.hp.hpl.jena.sparql.core.{Var=>JenaVar}
import com.hp.hpl.jena.rdf.model.{Literal=>JenaLit}
import com.hp.hpl.jena.sparql.core.BasicPattern
import com.hp.hpl.jena.sparql.algebra.op.OpFilter
import com.hp.hpl.jena.sparql.expr.nodevalue._
import com.hp.hpl.jena.sparql.expr._

class JenaEvaluator(q:JenaOp) {
  val log=LoggerFactory.getLogger(this.getClass)
  
  def evaluate(g:JenaGraph)={
    val it=Algebra.exec(q, g)
    println("pipipin")
    it
    /*while (it.hasNext()){
      val r=it.nextBinding()
      println("lallala "+r)
    }*/
  }
}

object JenaBindings{
  implicit def b2bind(b:JenaBinding)={    
    val bnd=b.vars().map{vari=>
      val term=b.get(vari)
      (vari.getVarName->JenaAlgebra.node2Term(term))
    }.toMap    
    Bindings(bnd)
  }
}

object JenaAlgebra{
  import rsp.data.RdfTools._
  implicit def jena(op:Op):JenaOp = op match{
    case bgp:BgpOp=>
      val bp=new BasicPattern
      bgp.patterns.foreach{p=>        
        bp.add(p)        
      }
      val jBgp=new OpBGP(bp)
      //println(jBgp)
      jBgp
    case filter:FilterOp=>
      val fp= OpFilter.filter(jenaExpr(filter.xpr ),jena(filter.op))
      fp
  }
  implicit def jenaG(g:Graph):JenaGraph={
    val jg=GraphFactory.createDefaultGraph
    g.triples.foreach{t=>
      jg.add(t)  
    }
    jg
  }
  
  implicit def jenaT(t:TriplePattern):JenaTriple={
    new JenaTriple(t.s,t.p ,t.o )
  }

  implicit def jenaT(t:Triple):JenaTriple={
    new JenaTriple(t.s,t.p ,t.o )
  }

  implicit def jenaTerm(term:PatternTerm):Node=term match{
    case iri:Op.IriTerm=>jenaIri(iri.iri)
    case varr:Var=>JenaVar.alloc(varr.name )
    
  }

  implicit def jenaTerm(term:RdfTerm):Node=term match{
    
    case iri:Iri=>jenaIri(iri)
    case varr:Var=>JenaVar.alloc(varr.name) 
    case lit:Literal=>NodeFactory.createLiteral(lit.value.toString)
    
  }
  
  implicit def node2Term(node:Node):RdfTerm=
    if (node.isURI)
      Iri(node.getURI)
    else if (node.isLiteral)
      AnyLiteral(node.getLiteralValue)//.toString,node.getLiteralDatatypeURI(),node.getLiteralLanguage)
    else
      null
  

  implicit def jenaIri(iri:Iri):Node={
    NodeFactory.createURI(iri)
  }
  
  def jenaExpr(v:ValueXpr)=v.value match {
      case i:Int=>new NodeValueInteger(i)
      case d:Double=>new NodeValueDouble(d)
      case s:String=>new NodeValueString(s)
    }
  
  def jenaExpr(v:VarXpr)=new ExprVar(v.name)
  
  def jenaExpr(xpr:Xpr):Expr = xpr match {
    case v:VarXpr=>jenaExpr(v)
    case v:ValueXpr=>jenaExpr(v)
    case bi:BinaryXpr=>bi.op match {
      case OpGt=>new E_GreaterThan(jenaExpr(bi.left),jenaExpr(bi.right))
      case OpLt=>new E_LessThan(jenaExpr(bi.left),jenaExpr(bi.right))
      case OpEq=>new E_Equals(jenaExpr(bi.left),jenaExpr(bi.right))
    }
     
  }
  
  
}