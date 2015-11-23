package rsp.engine.cqels
import com.hp.hpl.jena.graph.Triple
import akka.actor.ActorSystem
import akka.actor.Props
import org.deri.cqels.engine.ConstructListener


object Experiments {
  type TripleList = java.util.List[Triple]
  val exStreams="http://example.org/streams/"
  
    def main(args:Array[String]):Unit={
    val cqels=new CqelsReasoner
    val system=ActorSystem.create("cqelsSystem")
    val ssw=system.actorOf(Props(new SsnStream(cqels,exStreams+"s1",1)))

    val listener=new ConstructListener(cqels.engine ){
      var count=0                 
      def update(triples:TripleList):Unit={
        count+=triples.size
      } 
    }
    ssw ! StartStream
    //cqels.registerQuery(ssnQuery2, listener,false)
    Thread.sleep(20000)
    println("input: "+cqels.inputCount )
    println("output: "+listener.count)
    cqels.stop
  }
}
