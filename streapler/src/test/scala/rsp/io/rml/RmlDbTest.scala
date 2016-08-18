package rsp.io.rml

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory

class RmlDbTest extends FlatSpec with Matchers  {
  private val logger= LoggerFactory.getLogger(this.getClass)
  
  "rml" should "be parsed" in{
    val tmaps=RmlEngine.readMappings("lsd.r2rml")
    val db=new RmlDb(tmaps.head)
    val started=System.currentTimeMillis
    db.generate
    println(s"Elapsed time: ${System.currentTimeMillis-started}")
  }
}