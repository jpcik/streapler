package rsp.io.rml.vocab

import rsp.vocab.Vocab
import rsp.data.Iri
import rsp.data.RdfTools._

object SR extends Vocab{
  override val iri:Iri="http://purl.oclc.org/rsp/srml#"
  val params=SR("params")  
  val _var=SR("var")  
  val param=SR("param")  
  val queryParams=SR("queryParams")
}