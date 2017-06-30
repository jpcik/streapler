package rsp.io.web

import rsp.data.RdfStream
import rsp.data._
import rsp.data.RdfTools._
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.Lang
import org.apache.jena.riot.system.StreamRDF
import org.apache.jena.rdf.model.ModelFactory
import scala.concurrent.Future


class ReplayRdfStream(override val name:Iri,rdfFile:String)  extends RdfStream {
  import concurrent.ExecutionContext.Implicits.global
  
  val rdf=ModelFactory.createDefaultModel
  RDFDataMgr.read(rdf, rdfFile, Lang.TTL)
  
  
  def data= {
    
    Future(Seq(rdf.getGraph))
    ???
    
  //  def s:Seq[Triple]={
    //  (1 to 1000).map(i=>Triple("a","p",Random.nextDouble.toString)).toSeq
    //}
    //val triples=(1 to 1).flatMap{i=>s}    
    //Future(Seq(Graph(s:_*)))
  }

  
  
}