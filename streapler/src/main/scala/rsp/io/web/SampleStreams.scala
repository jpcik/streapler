package rsp.io.web

import scala.util.Random
import scala.concurrent.Future
import rsp.engine.RspFeed
import rsp.data._
import rsp.data.RdfTools._


class RandomRdfStream(override val name:Iri) extends RdfStream{
  import concurrent.ExecutionContext.Implicits.global 
  def data= {
    def s:Seq[Triple]={
      (1 to 1000).map(i=>Triple("a","p",Random.nextDouble.toString)).toSeq
    }
    //val triples=(1 to 1).flatMap{i=>s}    
    Future(Seq(Graph(s:_*)))
  }
}


class ConstantRdfStream(override val name:Iri) extends RdfStream{
  import concurrent.ExecutionContext.Implicits.global 
  def data={
    def s:Seq[Triple]={
      (1 to 10).map(i=>Triple("a","p","4")).toSeq
    }
    //val triples=(1 to 1).flatMap{i=>s}    
    Future(Seq(Graph(s:_*)))
  }
}
