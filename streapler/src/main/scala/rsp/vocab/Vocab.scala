package rsp.vocab

import rsp.data._

trait Vocab {
  val iri:Iri
  def apply(s:String)=Iri(iri+s)
  def clazz(s:String):Clazz=new Clazz(iri+s)
  def prop(s:String):Property=new Property(iri+s)
}

class Clazz(iri:String) extends Iri(iri)

class Property(iri:String) extends Iri(iri)