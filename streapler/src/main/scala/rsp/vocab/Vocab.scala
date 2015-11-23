package rsp.vocab

import rsp.data._

trait Vocab {
  val iri:Iri
  def apply(s:String)=Iri(iri+s)
}