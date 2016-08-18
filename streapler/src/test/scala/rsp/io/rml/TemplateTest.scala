package rsp.io.rml

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory

class TemplateTest extends FlatSpec with Matchers  {
  private val logger= LoggerFactory.getLogger(this.getClass)
  import MutateDB._
  
  def check(temp:String,result:String)={
    val template=Template(temp)
    println(renderMapValue(template))
    renderMapValue(template) should be (result)
  }
    
  "template" should "be replaced" in{
    check("abc{top1}bcd{top2}cde{top3}efg",
          "concat('abc',top1,'bcd',top2,'cde',top3,'efg')")
    check("{top1}bcd{top2}cde{top3}efg",
          "concat(top1,'bcd',top2,'cde',top3,'efg')")
    check("abc{top1}bcd{top2}cde{top3}",
          "concat('abc',top1,'bcd',top2,'cde',top3)")
  }
  
  "template vars" should "be extracted" in {
    val template=rsp.io.Template("ab{first}bsp{second}sadasd{obs1}")
    template.vars should be (Seq("first","second","obs1"))
  }
  
}