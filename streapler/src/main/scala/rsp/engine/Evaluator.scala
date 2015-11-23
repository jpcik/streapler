package rsp.engine

import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import akka.actor._
import concurrent.duration._
import scala.util.Random
import org.apache.spark.streaming.receiver.ActorHelper
import org.apache.spark.SparkEnv
import scala.collection.mutable.ArrayBuffer
import rsp.query.algebra.Op
import rsp.data._
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import rsp.query.algebra.TriplePattern
import rsp.query.algebra.BgpOp
import com.hp.hpl.jena.sparql.algebra.Algebra
import collection.JavaConversions._
import org.apache.spark.mllib.clustering.StreamingKMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.io.File
import java.nio.file.StandardCopyOption
import org.apache.spark.mllib.linalg.Vector
import rsp.io.web.JsonWebFeed
import rsp.io.rml.RmlEngine
import rsp.io.web.JsonWebStream
import rsp.query.algebra._

object EvalTools{
  def str(recStr:String,ctx:StreamingContext)={
    val recProps=Props(new RspReceiver(recStr))
    val stream=ctx.actorStream[Graph](recProps,"rspfeed"+"Stream")
    stream
  }
}

class Evaluator {
  val prefix="akka://sparkDriver/user/"
    
  val strContext={
    val batchDuration=Seconds(1)
    val conf=new SparkConf().setAppName("rspql").setMaster("local[*]")
    new StreamingContext(conf,batchDuration)    
  }
  
  
  
  private lazy val sys=SparkEnv.get.actorSystem
  val streams=new ArrayBuffer[ReceiverInputDStream[Graph]]
  val feeds=new ArrayBuffer[ActorRef]

  def actor(props:Props)=sys.actorOf(props)

  def addFeed(props:Props,name:String)={
    val feed=sys.actorOf(props,name)
    feeds+=feed
    val stream=EvalTools.str(prefix+name, strContext)
    streams += stream
  }
   
  def start={
    feeds.foreach{f=> f ! Init}    
    strContext.start
    strContext.awaitTermination(20)
  }

  def end={
    feeds.foreach{f=> f ! End}
  }  
  
  def evaluate(op:Op):Bindings=null  
  
}

case class Dataload(d:Double)

class DataFeed extends Actor{
  val subscribed=new ArrayBuffer[ActorRef]()
  private implicit val ctx=context.dispatcher

  def receive={
    case s:Subscribe=>
      println("subscribed "+s.actor.path )
      subscribed+=s.actor
    case Init=>
      context.system.scheduler.scheduleOnce(5 seconds){
        subscribed.foreach{s=>
          
          val pal= (1 to 100).map(i=>Random.nextDouble).toArray
          s ! pal
        }
      }
      
  }
}

case class Bindings(values:Map[String,RdfTerm]){
  
  
}


object Eval{
  
  def main(args:Array[String])={
    trep
  }

  import JenaAlgebra._
    import JenaBindings._
    import rsp.query.algebra.Op._

    val t=TriplePattern("papa","p","o")
      val bgp=new BgpOp(Seq(t))
  val fil=FilterOp(BinaryXpr(OpEq,VarXpr("o"),ValueXpr("3")),bgp)
     val jev=new JenaEvaluator(fil)

  def jevv(g:Graph)=jev.evaluate(g)
  
  def trep={
    
    val map=RmlEngine.readMappings("src/test/resources/citybikes.rml")
    val props=Map("sourceid"->"metropolradruhr-germany-dortmund")
    val json=new JsonWebStream(map.head,props)
    val ev=new Evaluator  
    ev.addFeed(Props(new JsonWebFeed(json,2000)), "rspfeed")
    val rdd=ev.streams(0)

    val trips=rdd.window(Seconds(2)).flatMap{g=>
      val it=jevv(g)
      it.map(b2bind)toSeq
      
    }
    
    trips.print
    /*
    map{tip=>
      tip.triples.size
    }.print*/
    //d.print(10)
    
    ev.start
  }
  
  
  def exec={
    import JenaAlgebra._
    import JenaBindings._
    import rsp.query.algebra.Op._

    val t=TriplePattern("papa","p","o")
    val bgp=new BgpOp(Seq(t))
    val jev=new JenaEvaluator(bgp)
    val ev=new Evaluator  
    ev.addFeed(Props(new RandomRspFeed("dibi",10)), "rspfeed")
    //ev.addFeed(Props(new RandomRspFeed(10)), "rspfeed2")
    //ev.addFeed(Props(new RandomRspFeed(10)), "rspfeed3")
    val feed=SparkEnv.get.actorSystem.actorOf(Props[DataFeed],"data")
    //val gt=ev.actor(Props[Getter])
    
    /*val rdd2=ev.streams(1)
    val rdd3=ev.streams(2)*/
    val rdd=ev.streams(0)
    val prefix="akka://sparkDriver/user/"
    val props=Props(new RspReceiver(prefix +"data"))
    
    //val train=ev.strContext .actorStream[Vector](props,"rspbeep"+"Stream")
    //val train=EvalTools.str(ev.prefix +"data", ev.strContext )
    //val train=rdd.map(pip=>)
    
    val trips=rdd.window(Seconds(1)).flatMap{g=>
      val it=Algebra.exec(bgp, g)
      //it.map(b=>"bap").toSeq
      it.map{b=>
        val bb=b2bind(b)
        
        //println("dime "+bb.values("o").getClass())
        val pip=bb.values("o").toString.toDouble
        Vectors.dense(Array(pip))
      }.toSeq      
    }
  
    
    
    val mm=new StreamingKMeans()
      .setK(4)
      .setDecayFactor(1.0)     
      .setRandomCenters(1, 0.1,1)
   
    //mm.trainOn(train)
    
    //train.print
    val bip=mm.predictOn(trips)
    bip.print(100)
    //trips.print(1)
    /*trips.count.foreachRDD{c=>
      val bip=c.collect.sum      
      gt!bip
    }*/
    //rdd2.print(1)
    //rdd3.print(1)
           feed ! Init

     //ctx.start()             // Start the computation
     //ctx.awaitTermination()
    ev.start

    //Files.copy(new File("train.txt").toPath,new File("data/train.txt").toPath,StandardCopyOption.REPLACE_EXISTING)
  //  println("popa")
//Thread.sleep(100000)
//gt ! Show

//ev.end

//System.exit(0)

  
  }  
}

object Show

class Getter extends Actor{
  var count=0L
  def receive={
    case l:Long=> 
      println("received "+l)
      count+=l
    case Show=> println("this is it "+count)
  }
}
