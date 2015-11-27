package rsp.engine.cqels

import org.deri.cqels.engine.ExecContext
import org.deri.cqels.engine.ConstructListener
import com.hp.hpl.jena.graph.Triple
import com.hp.hpl.jena.query.Query
import rsp.engine.rewrite.KyrieRewriter
import org.deri.cqels.lang.cqels.ElementStreamGraph
import com.hp.hpl.jena.sparql.syntax.Element
import com.hp.hpl.jena.sparql.syntax.ElementGroup
import collection.JavaConversions._
import org.deri.cqels.engine.Window
import org.deri.cqels.engine.RangeWindow
import com.hp.hpl.jena.sparql.serializer.FormatterTemplate
import com.hp.hpl.jena.sparql.serializer.FmtTemplate
import akka.actor.Actor
import com.hp.hpl.jena.graph.NodeFactory
import rsp.engine.RspReasoner
import rsp.engine.RspListener

case class RegisterQuery(q:String)
case class StreamTriple(uri:String,s:String,p:String,o:String)


class CqelsReasoner(ontologyFile:String) extends RspReasoner{
  //var regQueries=0
  //var inputCount=0
  val engine= new ExecContext("./",false)
  import rsp.data.{Triple=>RdfTriple}
  import rsp.util.JenaTools._
  
  def stop={
    engine.env().flushLog(true)
    
    //engine.env().close()
  }
  
  def consume(uri:String,t:RdfTriple) ={
    inputCount+=1
    engine.engine().send(uri, t)
  }
  
  def registerQuery(q:String,listener:RspListener,reasoner:Boolean=false)={
    import org.deri.cqels.lang.cqels._

    println(q)
    val qt=
    if (reasoner){
      val query=CqelsQueryWriter.readCqels(q)
    
      val k=new KyrieRewriter(ontologyFile)
      k.rewriteInUcq(query).map{q=>
      //println("new: "+newQ)
         CqelsQueryWriter.writeCqels(q)
      //println("now cqels: "+ss)
        
      }
      //context.loadDefaultDataset("{DIRECTORY TO LOAD DEFAULT DATASET}");
      //context.loadDataset("{URI OF NAMED GRAPH}", "{DIRECTORY TO LOAD NAMED GRAPH}");     
    }
    else Seq(q)
    regQueries=qt.size
    qt.foreach{cqelsQuery=>
      println(cqelsQuery)
      val selQuery=engine.registerConstruct(cqelsQuery)    
      selQuery.register(listener.listener.asInstanceOf[ConstructListener])      
    }
  }
    


    
}