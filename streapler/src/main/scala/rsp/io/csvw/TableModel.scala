package rsp.io.csvw

import rsp.io.Template

class TableModel {
  
}

case class Datatype(base:String,format:String)

case class Column(name:String,titles:Array[String],virtual:Boolean=false,
    aboutUrl:Option[String],propertyUrl:Option[String],valueUrl:Option[String],
    datatype:Option[Datatype]) {
}
 
case class TableContext(language:String,base:String,
    prefix:Map[String,String])
  
case class Table(url:String,id:String,columns:Seq[Column],ctx:TableContext){
  val csvColumnNameIdx=
    columns.filterNot(_.virtual).map(_.name).zipWithIndex.toMap
  
  //val csvColumnIdx=csvColumnNameIdx.values.toSeq
  private val colMap=columns.map(c=>c.name->c).toMap
  //def col(idx:Int)=colMap(csvColumnNames(idx))
  def col(name:String)=colMap(name)
  
  def value(colName:String,values:Array[String])=
    values(csvColumnNameIdx(colName))
  
  def namespace(str:String)={
    val pre="""(^[a-z]+\:)""".r.findFirstIn(str)
    pre.map { p => 
      str.replace(p, ctx.prefix(p.dropRight(1))) 
    }.getOrElse(str)
  }
    
  def template(temp:String,data:Array[String])={
    val tpl=namespace(temp)
    var result=tpl
    Template(tpl).vars.foreach { v =>
      //println(v)
      result=result.replace(s"{$v}", value(v,data))
    }
    result
  }
  
    
}

