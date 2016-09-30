package rsp.engine.cqels

import rsp.engine.RspReasoner
import org.deri.cqels.engine.ExecContext
import rsp.engine.RspListener
import org.deri.cqels.engine.ConstructListener
import org.apache.jena.graph.Triple

class CqelsEngine extends RspReasoner{
  val engine= new ExecContext("./",false)
  import rsp.data.{Triple=>RdfTriple}
  import rsp.util.JenaTools._
  import rsp.util.JenaTypes._
  
  def stop={    
  }
  
  def consume(uri:String,t:RdfTriple) ={
    //inputCount+=1
    engine.engine.send(uri, t)
  }
  
  def registerQuery(cqelsQuery:String,listener:RspListener,reasoner:Boolean=false)={
    import org.deri.cqels.lang.cqels._

    println(cqelsQuery)
    val selQuery=engine.registerConstruct(cqelsQuery)    
    selQuery.register(listener.listener.asInstanceOf[ConstructListener])      
   
  }

  def createListener(list:TripleList=>Unit)= {
    val lis=new ConstructListener(engine){  
      def update(triples:TripleList):Unit=list(triples)                    
    }
    new RspListener(lis){
      override def count=0
    }
  }
      
    //val ws=sys.actorOf(Props(new WebSocketStream(cqels,"ws://localhost:4040/primus",conf)))
  
}