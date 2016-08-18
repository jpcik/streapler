package rsp.io.csvw

import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.Lang
import collection.JavaConversions._
import play.api.libs.json.Json
import scala.io.Source
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import java.net.URI
import org.apache.commons.httpclient.util.URIUtil
import java.net.URLEncoder

object CsvWeb {
  def readMetadata(csvwFile:String)={
    
    val json=Json.parse(Source.fromFile(csvwFile).getLines.mkString(" "))
    val imports=(json \ "@context").as[JsArray].value(1).as[JsObject]
    
    val pref=imports.keys.filter(k =>k!="@language" && k!="base")
      .map { k => k->(imports \ k).as[String]}.toMap
    val ctx=TableContext((imports \ "@language").as[String],
        (imports \ "base").as[String],pref)
    
    val cols=(json \\ "columns").head.as[JsArray]
    val columns=cols.value.map{col=>
      Column(
        (col \ "name").as[String],
        Array(),
        (col \ "virtual").asOpt[Boolean].getOrElse(false),
        (col \ "aboutUrl").asOpt[String],
        (col \ "propertyUrl").asOpt[String],
        (col \ "valueUrl").asOpt[String],
        None)
    }
    Table(csvwFile,columns,ctx)
    //val m=RDFDataMgr.loadModel(csvwFile,Lang.JSONLD)
    //m.listStatements.size
  }
  
  def generateRdf(csvFile:String,csvwFile:String)={
    val table=readMetadata(csvwFile)
    Source.fromFile(csvFile).getLines.foreach{line=>
      val data=line.split(",")
      var i=0
      table.columns.foreach { col => 
        val s=table.ctx.base+table.template(col.aboutUrl.get, data)
          val p=table.namespace(col.propertyUrl.get)
        
        val o=
          if (col.valueUrl.isDefined){
            table.template(col.valueUrl.get, data)
          }
          else table.value(col.name, data)
        println(s"$s $p $o")
        
        //println(col.name+": "+values(i))
        //i+=1
      }
      

    }
  }
}