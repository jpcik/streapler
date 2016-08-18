package rsp.rspql

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory

class RspqlParserTest extends FlatSpec with Matchers  {
  private val logger= LoggerFactory.getLogger(this.getClass)
  val q1="""
    PREFIX : <http://xmlns.com/foaf/0.1/>
    PREFIX ex: <http://xmlns.com/foaf/0.1/>
    SELECT ?room
    FROM NAMED WINDOW :win ON ex:social [RANGE PT10M ]
    WHERE {
      WINDOW :win { :axel :isIn ?room }
    }"""

  "Query with no streams" should "parse no streams" in{
    val q=Rspql.parse(q1)
    logger.debug(q.serialize)
    q.streams.size should be(1)
  }
}