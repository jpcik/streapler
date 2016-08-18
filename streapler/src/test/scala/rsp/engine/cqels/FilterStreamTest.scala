package rsp.engine.cqels

import org.deri.cqels.engine.ExecContext
import org.apache.jena.graph.Triple
import collection.JavaConversions._
import scala.concurrent.Future
import rsp.vocab.SSN
import org.deri.cqels.engine.ConstructListener
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import org.scalatest.FlatSpec
import org.scalatest.Matchers

class BigEngine extends Actor{
  val engine= new ExecContext("",false)
      engine.loadDefaultDataset("file:///C:/Users/calbimon/git/rsp-engine/rsp-reasoner/src/test/resources/static.ttl")

  import rsp.util.JenaTools._

  def receive={
    case t:rsp.data.Triple=>
      engine.engine().send("someuri", t)
    case q:Qry=>
      val listener=new ConstructListener(engine ){                 
        def update(triples:java.util.List[Triple]):Unit={
          println("dip")
          triples.foreach{t=>
            println(t.getSubject)
            q.rec ! t
          }
        } 
      }
        
      val reg=engine.registerConstruct(q.q)
      reg.register(listener)
  }
}

case class Qry(q:String,rec:ActorRef)


class FilterStreamTest  extends FlatSpec with Matchers{
  
  "engine " should "execute" in{
    filter
  }
  
  def filter={
    
    
    val sys= ActorSystem("systy")
    val eng=sys.actorOf(Props[BigEngine],"engino")
    import concurrent.ExecutionContext.Implicits.global
    import rsp.data.{Triple => RspTriple}
      import rsp.util.JenaTools._
    Future {
      while (true){
        val obs=SSN("obs"+System.currentTimeMillis)        
        val sens=rsp.data.Iri("http://oeg-upm.net/onto/sensordemo/sens1")//+System.currentTimeMillis)
        eng ! RspTriple(obs,SSN.observedBy,sens)
        Thread.sleep(2000)
      }
    }
    
    
         Thread.sleep(10000)

    
  }
}