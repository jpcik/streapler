package rsp.io.rml

import scala.io.Source
import java.io.FileWriter
import java.io.BufferedWriter
import java.net.URI
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.slick.jdbc.StaticQuery
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.PositionedResult
import collection.JavaConversions._

object RmlGenerate {
}
 object ResultMap extends GetResult[Map[String,Any]] {
  def apply(pr: PositionedResult) = {
    val rs = pr.rs // <- jdbc result set
    val md = rs.getMetaData();
    println(md.getColumnCount)
    val res = (1 to pr.numColumns).map{ i=> md.getColumnName(i) -> rs.getObject(i) }.toMap
    pr.nextRow // <- use Slick's advance method to avoid endless loop
    res
  }
}
 
class RmlDb(rml:TriplesMap) {
   type StringTriple=(String,String,String)
  
  def tip(data:Map[String,Any],tmap:TriplesMap):Seq[StringTriple]={
    val res=new ArrayBuffer[StringTriple]   
    val s=MutateDB.generate(data,tmap.sMap).toString
    tmap.poMaps.foreach {po=>
      val p=MutateDB.generate(data, po.pMap).toString
      val parentMap=po.oMap.parentTriplesMap
      val o=
        if (parentMap.isEmpty)
          MutateDB.generate(data, po.oMap).toString
        else {
          val bib=tip(data,parentMap.get)
          res++=bib
          if (parentMap.get.source.isDefined && parentMap.get.source.get.cached.isDefined){
            //println(data(po.oMap.child))
            val list=parentMap.get.source.get.cached.get(data(po.oMap.child).toString)
            val map=list.zipWithIndex.map(a=>a._2.toString->a._1).toMap
            //println(map.keys)
            MutateDB.generate(map, parentMap.get.sMap)
          }
          else 
            MutateDB.generate(data, parentMap.get.sMap)
        } 
      res+=((s,p,o.toString))
    }
    res
  }
 

  def generate={
    val filters=Array(
        ("time",">","2003-04-02 05:50:00"),
        ("time","<","2003-04-09 00:00:00"),
        ("obsprop","=","http://knoesis.wright.edu/ssw/ont/weather.owl#_AirTemperature"))
    val fw=new FileWriter("output.nt")
    val bw=new BufferedWriter(fw)//,4*Math.pow(1024, 2).toInt)
    import scala.slick.driver.JdbcDriver.simple._
    import scala.slick.jdbc.StaticQuery.interpolation
    val db=Database.forURL("jdbc:postgresql://localhost:5433/pg_test","postgres","password")
    val params=rml.source.get.params
    val qq=rml.source.get.query.get
    val qfils=filters.map{f=>
      val col=params.filter { _.name==f._1 }.head.map
      s"${MutateDB.renderMapValue(col)} ${f._2} '${f._3}'"
    }.mkString(" and ")
    
    val qqqq=qq+" where "+qfils
    println(qqqq)
    db.withSession { implicit session =>
      val rs=session.conn.prepareStatement(qqqq).executeQuery
      val md = rs.getMetaData
      while (rs.next){
        val res =  (1 to md.getColumnCount).map{ i=> md.getColumnName(i) -> rs.getObject(i) }.toMap
        tip(res,rml) foreach { case (s,p,o)=>
          bw write s"$s $p $o\n"
        }    
      }
    }
    
    bw.close
    fw.close
  }
  
}

class RmlCsv(rml:TriplesMap) {
  type StringTriple=(String,String,String)
  
  def tip(data:Array[String],tmap:TriplesMap):Seq[StringTriple]={
    val res=new ArrayBuffer[StringTriple]   
    val s=Mutate.generate(data,tmap.sMap)
    tmap.poMaps.foreach {po=>
      val p=Mutate.generate(data, po.pMap)
      val parentMap=po.oMap.parentTriplesMap
      val o=
        if (parentMap.isEmpty)
          Mutate.generate(data, po.oMap)
        else {
          val bib=tip(data,parentMap.get)
          res++=bib
          if (parentMap.get.source.isDefined && parentMap.get.source.get.cached.isDefined){
            val list=parentMap.get.source.get.cached.get(data(po.oMap.child.toInt))
            Mutate.generate(list, parentMap.get.sMap)
          }
          else 
            Mutate.generate(data, parentMap.get.sMap)
        } 
      res+=((s,p,o))
    }
    res
  }
  
  def generate={
    //import concurrent.ExecutionContext.Implicits.global
    val fw=new FileWriter("output.nt")
    val bw=new BufferedWriter(fw)//,4*Math.pow(1024, 2).toInt)
    val lines=Source.fromFile(new URI(rml.source.get.uri)).getLines
    lines foreach { line =>             
      val data=line split ","
      tip(data,rml) foreach{case (s,p,o)=>
        bw write s"$s $p $o\n"
      }    
    }
    
    bw.close
    fw.close
  }
}


object MutateDB {
  def generate(vals:Map[String,Any],map:TermMap)={
    map.value match{
      case t:Template=>
        var res=t.template
        vals.keys.foreach{k=>
          res=res.replace(s"{$k}", vals(k).toString)
        }
        res
      case c:Constant=>
        c.const
      case c:Column=>
        vals(c.col)
      case r:Reference=>
        val data=vals(r.ref)
        if (map.datatype.isEmpty) data          
        else
          s"""\"${data}\"^^${map.datatype.get.getURI}"""
    }
  }
  
  def renderMapValue(map:MapValue)=map match{    
    case t:Template=>
      var rep=t.template
      val str=new ArrayBuffer[String]
      while (rep.size>0){
        val i=rep.indexOf("{")
        if (i>=0) {
          if (i>0)
            str+=s"'${rep.take(i)}'"
          rep=rep.drop(i)
          val j=rep.indexOf("}")
          str+=rep.substring(1, j)
          rep=rep.drop(j+1)
        }          
        else {           
          str+=s"'$rep'"
          rep=""          
        }       
      }      
      "concat("+str.mkString(",")+")"
    case c:Column=>
      c.col
  }
   
}


object Mutate {
  def generate(vals:Array[String],map:TermMap)={
    map.value match{
      case t:Template=>
        var res=t.template
        (0 to vals.size-1).foreach{i=>
          res=res.replace(s"{$i}", vals(i))
        }
        res
      case c:Constant=>
        c.const
      case r:Reference=>
        val data=vals( r.ref.toInt)
        if (map.datatype.isEmpty) data          
        else
          s"""\"${data}\"^^${map.datatype.get.getURI}"""
    }
  }
}