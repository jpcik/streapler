package rsp.engine.cqels

import org.apache.jena.query.Query
import org.apache.jena.sparql.syntax.ElementGroup
import org.apache.jena.sparql.syntax.Element
import org.apache.jena.sparql.serializer.FmtTemplate
import org.deri.cqels.engine.Window
import org.deri.cqels.engine.RangeWindow
import org.deri.cqels.lang.cqels.ElementStreamGraph
import org.deri.cqels.lang.cqels.ParserCQELS
import collection.JavaConversions._
import org.apache.jena.sparql.syntax.ElementUnion
import org.apache.jena.sparql.syntax.ElementNamedGraph
 
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