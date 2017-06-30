package rsp.vocab


import rsp.vocab._

import rsp.data._

import rsp.data.RdfTools._

object RDF_SCHEMA extends Vocab {
  override val iri: Iri = "http://www.w3.org/2000/01/rdf-schema#"
  val Resource = clazz("Resource")
  val ContainerMembershipProperty = clazz("ContainerMembershipProperty")
  val Datatype = clazz("Datatype")
  val Class = clazz("Class")
  val Container = clazz("Container")
  val Literal = clazz("Literal")
  val subPropertyOf = prop("subPropertyOf")
  val isDefinedBy = prop("isDefinedBy")
  val range = prop("range")
  val subClassOf = prop("subClassOf")
  val seeAlso = prop("seeAlso")
  val member = prop("member")
  val comment = prop("comment")
  val domain = prop("domain")
  val label = prop("label")
}
