package rsp.io.rml

import scala.io.Source
import java.io.FileWriter
import java.io.BufferedWriter
import org.joda.time.DateTime

object MutateData {
  def main(args:Array[String])={
    val lines=Source.fromFile("/Users/calbimon/data/lsd/nevada.nt").getLines
    val fw=new FileWriter("/Users/calbimon/data/lsd/nevada.csv")
    val bw=new BufferedWriter(fw)
    lines.foreach { line =>
      if (line.contains("#floatValue")){
        val trip=line.split(" ")
        val sub=trip(0).split("_")
        val obsProp=sub(1)
        val sens=sub(2)        
        val dt=new DateTime(sub(3).toInt,sub(4).toInt,sub(5).toInt,
            sub(6).toInt,sub(7).toInt,sub(8).dropRight(1).toInt)
        val floatVal=trip(2).split("\"")(1).toDouble
        bw.write(s"${dt},$sens,$obsProp,$floatVal\n")
        
      }
    }
    bw.close
    fw.close
  }
}