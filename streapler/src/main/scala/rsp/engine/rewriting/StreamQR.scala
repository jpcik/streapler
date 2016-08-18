package rsp.engine.rewriting

import org.deri.cqels.engine.ConstructListener
import org.deri.cqels.engine.ExecContext
import rsp.engine.RspListener
import rsp.engine.RspReasoner
import rsp.engine.rewrite.KyrieRewriter
import rsp.util.JenaTools
import rsp.engine.cqels.CqelsQueryWriter

case class RegisterQuery(q:String)
case class StreamTriple(uri:String,s:String,p:String,o:String)


class StreamQR(ontologyFile:String) extends RspReasoner{
  val engine= new ExecContext("./",false)
  import rsp.data.{Triple=>RdfTriple}
  import rsp.util.JenaTools._
  
  def stop={
    engine.env().cleanLog()
    
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