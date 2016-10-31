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
import play.api.libs.json.JsValue

object CsvWeb {
  def readMetadata(csvwFile:String)={
    
    val json=Json.parse(Source.fromFile(csvwFile).getLines.mkString(" "))
    val ct=(json \ "@context")
    val imports=ct.asOpt[JsArray] map {
        v=>v.value(1).as[JsObject]       
    }
    val lang=imports.map(i=>(i \ "@language").as[String]).getOrElse("und")
    val base=imports.map(i=>(i \ "base").as[String]).getOrElse(csvwFile)
    
    val pref=imports.map{i=>
      i.keys.filter(k =>k!="@language" && k!="base")
       .map { k => k->(i \ k).as[String]}.toMap
    }
    val ctx=TableContext(lang,base,pref.getOrElse(Map()))
    
    val tables=(json \ "tables").asOpt[JsArray]
    
    if (tables.isDefined){
      tables.get.value.map{table=>
        parseTable(table,ctx)
      }
    }
    else Seq(parseTable(json,ctx))
       
    //val m=RDFDataMgr.loadModel(csvwFile,Lang.JSONLD)
    //m.listStatements.size
  }
  
  def parseTable(tableJson:JsValue,ctx:TableContext)={
    
    def parseDatatype(dt:Option[String])=dt.map(Datatype("",_))
    
    val url=(tableJson \ "url").as[String]
    val id=(tableJson \ "id").asOpt[String].getOrElse(url)
    val schema=tableJson \ "tableSchema"
    val cols=(schema \ "columns").as[JsArray]
    val columns=cols.value.map{col=>
      Column(
        (col \ "name").as[String],
        Array(),
        (col \ "virtual").asOpt[Boolean].getOrElse(false),
        (col \ "aboutUrl").asOpt[String],
        (col \ "propertyUrl").asOpt[String],
        (col \ "valueUrl").asOpt[String],
        parseDatatype((col \ "datatype").asOpt[String]))
    }
    Table(url,id,columns,ctx)
  }
  
  def generateRdf(csvFile:String,csvwFile:String)={
    val tables=readMetadata(csvwFile)
    tables.foreach { table =>
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
}