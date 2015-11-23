package rsp.query.algebra

import rsp.data._
import org.joda.time.Period
import com.hp.hpl.jena.rdf.model.Property

trait Op {
  
  
}

object Op {
  implicit def String2Var(s:String)=Var(s)
  implicit class IriTerm(val iri:Iri) extends PatternTerm{    
  }
}

case class BgpOp(patterns:Seq[TriplePattern]) extends Op{
  
}

case class FilterOp(xpr:BinaryXpr,op:Op) extends Op


trait PatternTerm {

}

case class TriplePattern(s:PatternTerm,p:PatternTerm,o:PatternTerm)

case class Var(name:String) extends PatternTerm

case class QueryAlgebra(op:Op,win:TimeWindow)

case class TimeWindow(period:Period) {
  lazy val duration=period.toStandardDuration
}



case class Func(name:String)


object OpAdd extends Func("+")
object OpGt extends Func(">")
object OpLt extends Func("<")
object OpEq extends Func("=")

trait Xpr

case class BinaryXpr(op:Func,left:Xpr,right:Xpr) extends Xpr

case class VarXpr(name:String) extends Xpr

case class ValueXpr(value:Any) extends Xpr



