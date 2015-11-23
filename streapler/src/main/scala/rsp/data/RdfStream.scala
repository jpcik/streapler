package rsp.data

import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import rsp.data.Rdf._
import scala.util.Random

case class Timestamp(time:Long){
  override def toString=s"ts($time)"
  def > (l:Long)=time > l
}

case class StreamGraph(g:Graph, t:Timestamp)

object RdfStream{
  def ts(time:Long)=Timestamp(time)
  implicit def long2Timestamp(time:Long)=Timestamp(time)
  implicit def pair2StreamGraph(p:(Graph,Long))=StreamGraph(p._1,p._2)
  implicit def tri2Triple(t:(String,String,String))=Triple(t._1,t._2 ,t._3)
}

trait RdfStream {
  val name:Iri
  def data:Future[Seq[Graph]]
  
}

abstract class iRdfStream(val name:Iri) {
  val data:Enumerator[StreamGraph]
  protected def stream(sg:StreamGraph)(implicit exec:ExecutionContext)= Future(Some(sg))
  protected def now=System.currentTimeMillis
  def generate[E](e: =>Future[Option[E]])(implicit exec:ExecutionContext)=
    Enumerator.generateM(e)
}

class RandomRdfStream(iri:Iri)(implicit exec:ExecutionContext) extends iRdfStream(iri){
  import RdfStream._
  
  override val data=generate{
    Thread.sleep(2000)
    val g=Graph(("a","b","c"+Random.nextInt),("a","x","y"))
    stream(g,now)
  }
}

object Demo{
  def main(args:Array[String]):Unit={
    
  import scala.concurrent.ExecutionContext.Implicits.global 

  val e1=Enumerator.generateM{//Future{
    Thread.sleep(500)
    println("generating")
    Future(Some(("Papas","Copas")))
  //}
  }
  val e=Enumerator(45,1,4,5,6)
  val st=new RandomRdfStream("s2")
  
  //val it=Iteratee.foreach[Int](a=>println(a))
  val it=Iteratee.foreach[(String,String)](a=>println(a))
  val it2=Iteratee.foreach[StreamGraph](a=>println(a))

  val o=st.data.apply(it2)
  //val aa= e1 run (it)
  //val a2= e1 run (it)
  //aa.foreach(a=>println(a))

  Thread.sleep(2000)
  }
}