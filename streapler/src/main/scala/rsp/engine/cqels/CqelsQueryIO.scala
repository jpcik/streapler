package rsp.engine.cqels

import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.sparql.syntax.ElementGroup
import com.hp.hpl.jena.sparql.syntax.Element
import com.hp.hpl.jena.sparql.serializer.FmtTemplate
import org.deri.cqels.engine.Window
import org.deri.cqels.engine.RangeWindow
import org.deri.cqels.lang.cqels.ElementStreamGraph
import org.deri.cqels.lang.cqels.ParserCQELS
import collection.JavaConversions._
import com.hp.hpl.jena.sparql.syntax.ElementUnion
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph
 
object CqelsQueryWriter {
  
  def readCqels(q:String)={
    val parser=new ParserCQELS
    val query=new Query
    val qq=parser.parse(query, q)
    qq
  }
  
  def writeCqels(query:Query)={
    val tmpl=FmtTemplate.asString(query.getConstructTemplate)
    val body=serElem(query.getQueryPattern)
    s"""CONSTRUCT $tmpl 
       |WHERE {
       |  $body
       |}""".stripMargin
  }  
   
  def serWindow(w:Window)=w match{
    case rw:RangeWindow=>
      val dur=(rw.getDuration/1000000).toLong
      s"[RANGE ${dur}ms]"
  }
    
  def serElem(e:Element):String=e match{
    case s:ElementStreamGraph=>
      s"""STREAM <${s.getGraphNameNode.getURI}> ${serWindow(s.getWindow)}
         {
           ${serElem(s.getElement)}
         }"""
    case g:ElementGroup=>
      g.getElements.map(serElem).mkString("")
    case g:ElementNamedGraph=>
      s"""GRAPH <${g.getGraphNameNode().getURI}>
      {
        ${serElem(g.getElement)}
      }"""
      
    case u:ElementUnion=>
      u.getElements().map(e=>s"{ ${serElem(e)} } ").mkString(" UNION ")
    case _ => e.toString
  }
  
}