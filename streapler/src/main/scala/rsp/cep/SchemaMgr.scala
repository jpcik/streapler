package rsp.cep

import rsp.io.csvw.CsvWeb
import rsp.io.csvw.Table
import collection.JavaConversions._
import com.espertech.esper.client.ConfigurationOperations
import rsp.io.csvw.Datatype

object SchemaMgr {
  
  def defineStreams(csvwFile:String,config:ConfigurationOperations)={
    val meta=CsvWeb.readMetadata(csvwFile)
    meta.foreach { table => defineStream(table,config) }

  }
  
  def defineStream(table:Table,config:ConfigurationOperations)={
    val cols=table.columns.map{column=>
      column.name -> resolveType(column.datatype.get.format)
    }.toMap
    println(cols.mkString(","))
    config.addEventType(table.id,cols)    
  }
  
  def resolveType(typing:String):Object=typing match{
    case "number" | "double"=> classOf[Double]
    case "datetime" => classOf[Long]
    case "dateTime" => classOf[Long]
    case "string" => classOf[String]
    case "integer" | "int" => classOf[Int]
    case "long" => classOf[Long]
  }
}