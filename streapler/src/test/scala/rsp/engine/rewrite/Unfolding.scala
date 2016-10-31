package rsp.engine.rewrite

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import rsp.rspql.Rspql
import rsp.io.rml.RmlEngine

class UnfoldingTest extends FlatSpec with Matchers  {
  private val logger= LoggerFactory.getLogger(this.getClass)
  
  "room person query" should "be rewritten" in{
    
    val tmaps=RmlEngine.readMappings("r2rml/social.r2rml.ttl")
    
    tmaps.size should be (1)
    
    tmaps.head
    
    val pfx="""
      PREFIX : <http://example.org/rsp/>
      PREFIX ex: <http://example.org/rsp> """

    val qs=s"""$pfx
      SELECT ?room ?person
      FROM NAMED WINDOW :win ON ex:social [RANGE PT1M ]
      WHERE { WINDOW :win { ?person ex:isIn ?room } }"""    
    val q=Rspql.parse(qs)
  }
  
}