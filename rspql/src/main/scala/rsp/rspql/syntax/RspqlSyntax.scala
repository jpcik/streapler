package rsp.rspql.syntax

import scala.beans.BeanProperty
import org.apache.jena.graph.Node
import org.apache.jena.sparql.syntax.Element
import org.apache.jena.sparql.util.NodeIsomorphismMap
import org.apache.jena.sparql.syntax.ElementVisitor
import org.apache.jena.sparql.syntax.ElementNamedGraph
import org.joda.time.Duration
import org.joda.time.Period

class ElementNamedWindowGraph(val node:Node,val element:Element) 
  extends ElementNamedGraph(node,element){  
}

class ElementNamedWindow(
    @BeanProperty val uri:String,
    @BeanProperty val streamUri:String,
    @BeanProperty val window:ElementWindow) extends Element{
  override def equalTo(el2:Element,isoMap:NodeIsomorphismMap)=false
  override def hashCode() =	0
  override def visit(v:ElementVisitor) {}
  
}

class ElementDuration(val periodExpr:String) extends Element{
  val period=Period.parse(periodExpr)
  val duration=period.toStandardDuration()
  override def equalTo(el2:Element, isoMap:NodeIsomorphismMap) =
	el2 match{
	  case el2:ElementDuration=>el2.duration.isEqual(duration)
	  case _=> false
    }
  
  override def hashCode() =0
  override def visit(v:ElementVisitor) {
		// TODO Auto-generated method stub
  }	
}

class ElementWindow extends Element{
  override def equalTo(el2:Element, isoMap:NodeIsomorphismMap)=false
  override def hashCode() =0
  override def visit(v:ElementVisitor ) {}
}

class ElementTimeWindow(
    val range:ElementDuration,
    //val to:ElementTimeValue,
    val slide:ElementDuration) extends ElementWindow {
}

class ElementCountWindow(val range:Int,val delta:Int) extends ElementWindow {  
}


