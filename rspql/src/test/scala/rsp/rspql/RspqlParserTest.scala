package rsp.rspql

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import rsp.rspql.syntax.ElementTimeWindow
import org.apache.jena.sparql.syntax.ElementGroup
import rsp.rspql.syntax.ElementNamedWindowGraph
import collection.JavaConversions._

class RspqlParserTest extends FlatSpec with Matchers  {
  private val logger= LoggerFactory.getLogger(this.getClass)
  val pfx="""
    PREFIX : <http://example.org/rsp/>
    PREFIX ex: <http://example.org/> """

  "Query with no streams" should "parse no streams" in{
    val qs=s"""$pfx
      SELECT ?room
      WHERE { :axel :isIn ?room } """
    val q=Rspql.parse(qs)
    q.streams should be(empty)
  }
  
  "Query with 1 stream" should "parse 1 stream" in{
    val qs=s"""$pfx
      SELECT ?room
      FROM NAMED WINDOW :win ON ex:social [RANGE PT10M ]
      WHERE { WINDOW :win { :axel :isIn ?room } }"""    
    val q=Rspql.parse(qs)
    logger.debug(q.serialize)
    
    q.streams.size should be(1)
    q.streams should contain key ("http://example.org/rsp/win")
    val win=q.streams("http://example.org/rsp/win")
    win.streamUri should be ("http://example.org/social")
    win.uri should be ("http://example.org/rsp/win")
    win.window shouldBe a [ElementTimeWindow]
    val timewin=win.window.asInstanceOf[ElementTimeWindow]
    timewin.slide should be(null)
    timewin.range.duration.getStandardMinutes should be (10)
    timewin.range.period.getMinutes should be (10)
    timewin.range.period.getHours should be (0)
    timewin.range.periodExpr should be ("PT10M")
    
    val bodyGrp= q.getQueryPattern.asInstanceOf[ElementGroup]
    val bodyWin= bodyGrp.getLast.asInstanceOf[ElementNamedWindowGraph]
    bodyWin.node.getURI should be (win.uri)
    println(bodyWin.getClass)
    

  }

  "Query with 2 windows" should "parse 2 windows" in{
    val qs=s"""$pfx
      SELECT ?room
      FROM NAMED WINDOW :win1 ON ex:social [RANGE PT10M ]
      FROM NAMED WINDOW :win2 ON ex:social [RANGE PT1H20M ]
      WHERE { WINDOW :win1 { :axel :isIn ?room } 
              WINDOW :win2 { :axel :isIn ?room } }"""    
    val q=Rspql.parse(qs)
    logger.debug(q.serialize)
    
    q.streams.size should be(2)
    val win1=q.streams("http://example.org/rsp/win1")
    win1.uri should be ("http://example.org/rsp/win1")
    val timewin1=win1.window.asInstanceOf[ElementTimeWindow]
    timewin1.range.period.getMinutes should be (10)
    val win2=q.streams("http://example.org/rsp/win2")
    win2.uri should be ("http://example.org/rsp/win2")
    val timewin2=win2.window.asInstanceOf[ElementTimeWindow]
    timewin2.range.period.getMinutes should be (20)
    timewin2.range.period.getHours should be (1)
    
    val bodyGrp= q.getQueryPattern.asInstanceOf[ElementGroup]
    val bodyWin1= bodyGrp.getElements.get(0).asInstanceOf[ElementNamedWindowGraph]
    bodyWin1.node.getURI should be (win1.uri)
    val bodyWin2= bodyGrp.getElements.get(1).asInstanceOf[ElementNamedWindowGraph]
    bodyWin2.node.getURI should be (win2.uri)
    

  }


}