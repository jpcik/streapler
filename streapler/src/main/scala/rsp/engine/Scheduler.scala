package rsp.engine

import collection.mutable.ArrayBuffer
import collection.JavaConversions._
import concurrent.ExecutionContext
import concurrent.duration._
import akka.actor._
import play.api.libs.iteratee.Iteratee
import org.joda.time.Period
import com.hp.hpl.jena.rdf.model.ModelFactory
import rsp.data._
import rsp.query.algebra._
import rsp.query.algebra.Op._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.query.DatasetFactory
import com.hp.hpl.jena.sparql.graph.GraphFactory
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory

class Scheduler extends Actor with ActorLogging{
  val streams=new ArrayBuffer[Iri]
  val queries=new ArrayBuffer[QueryAlgebra]
  val execs=new ArrayBuffer[ActorRef]
  import rsp.engine.JenaAlgebra._
  //lazy val eval=new JenaEvaluator(queries.head.op )
  
  def receive ={
    case (iri:Iri,g:StreamGraph)=>
      execs.foreach{qe=>
        qe ! g
      }
      //log.info(iri+g.toString)
      //eval.evaluate(g.g )
    case RegisterQuery(q)=>
      queries+=q
      val qe=context.actorOf(Props(new QueryExecutor(q)), "q1" )
      execs.+=(qe)
  }
}

case class RegisterQuery(q:QueryAlgebra)
case class Tick(tick:Long)

class Engine {
  val sys=ActorSystem.create("rsp-engine")
  val sched=sys.actorOf(Props[Scheduler])
  val rec=new StreamReceiver(sched)(sys.dispatcher)
  def register(s:iRdfStream)=rec.register(s)
  def registerQuery(q:QueryAlgebra)={
    sched ! RegisterQuery(q)
  }
}

class StreamDispatcher extends Actor {
  def receive ={
    case g:StreamGraph=>
  }
}

class StreamReceiver(sched:ActorRef)(implicit ctx:ExecutionContext) {
  def register(s:iRdfStream)={
    val it=Iteratee.foreach[StreamGraph]{a=>
      //println(a)
      sched ! (s.name,a)
    }
    s.data(it)
  }
}

object EngineDemo{
  def main(args:Array[String]):Unit={
    import rsp.data.RdfTools._
    import scala.concurrent.ExecutionContext.Implicits.global 
    
    
    val st=new RandomRdfStream("s2")
    val st1=new RandomRdfStream("s1")
    val e=new Engine
     val t=TriplePattern("s","p","o")
    val bgp=new BgpOp(Seq(t))
    val win=TimeWindow(Period.millis(10000))
    val qa=QueryAlgebra(bgp,win)
    e.registerQuery(qa)
    //e.register(st)
    //e.register(st1)        
    
  }
}

class QueryExecutor(query:QueryAlgebra) extends Actor{
  import JenaAlgebra._
  lazy val eval=new JenaEvaluator(query.op )
  val model=ModelFactory.createDefaultModel.getGraph
  val windowContent=new ArrayBuffer[StreamGraph]()
  implicit val ctx=context.dispatcher
  val range =query.win.period.toStandardSeconds.getSeconds*1000
  context.system.scheduler.schedule(0 seconds, query.win.period.toStandardSeconds.getSeconds seconds, 
      self, Tick(System.currentTimeMillis))
  def graphs={
    val ds=GraphFactory.createDefaultGraph()     
    windowContent.map{sg=>
      sg.g.triples foreach{t=>
        ds.add(t)      
      }
    }
    ds
  }
  def receive ={
    case Tick(t)=>
      println("ticks "+t+" "+range)
      removeHead(t-range)
    case sg:StreamGraph=>
      windowContent+=sg
      //sg.g.triples.foreach{t=>        
      //  model.add(t)      }
      
      eval.evaluate(graphs)
      
  }
  
  def removeHead(limit:Long):Unit={
    if (windowContent.size >0 && windowContent.head.t > limit){
      
    println("remove "+windowContent .head.t )
      windowContent.remove(0)
    }
    if (windowContent.size >0 && windowContent.head.t > limit)      
      removeHead(limit)
  }

}
