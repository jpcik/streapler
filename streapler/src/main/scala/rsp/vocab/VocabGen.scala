package rsp.vocab

import rsp.data.Iri
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.vocabulary.RDF
import collection.JavaConversions._
import org.apache.jena.vocabulary.OWL
import org.apache.jena.rdf.model.Statement

object VocabGen {  
  
  import treehugger.forest._
  import definitions._
  import treehuggerDSL._
  
  val IriType=TYPE_REF("Iri")
  
  case class VocabConf(name:String,uri:String,pack:String)
  case class VocabMeta(name:String,uri:String,localnames:Iterator[String])
  
  def genTree(conf:VocabConf,resources:Seq[String])={
    BLOCK(
      IMPORT("rsp.vocab._") ,
      IMPORT("rsp.data._") ,
      IMPORT("rsp.data.RdfTools._"),
      OBJECTDEF(conf.name) withParents("Vocab") := BLOCK(
        Seq(VAL("iri",IriType) withFlags(Flags.OVERRIDE) := LIT(conf.uri))++
        resources.map{r=>
          VAL(r) := REF(conf.name) APPLY (LIT(r))
        }        
      ) 
    ) inPackage(conf.pack)
  }
  
  def readVocab(url:String)={
    val m=RDFDataMgr.loadModel(url)
    val shortName=url.split("/").last.toUpperCase
    val defPrefix=m.getNsPrefixURI("")
    
    def isLocal(s:Statement)={
      s.getSubject.isURIResource && s.getSubject.getURI.startsWith(defPrefix)
    }
    def toLocalName(stms:Iterator[Statement])=
      stms.filter(isLocal).map(_.getSubject.getLocalName)
    
    val localnames=toLocalName(
      m.listStatements(null,RDF.`type`,OWL.ObjectProperty)++
      m.listStatements(null,RDF.`type`,OWL.DatatypeProperty)++
      m.listStatements(null, RDF.`type`, OWL.Class))
    
    VocabMeta(shortName,defPrefix,localnames)
  }
  
  
  def main(args:Array[String])={
    val meta=readVocab("http://purl.oclc.org/NET/ssnx/ssn")
    val conf=VocabConf(meta.name,meta.uri,"rsp.vocab.pip")
    println(treeToString(genTree(conf,meta.localnames.toSeq)))
  }
}


/*
object OmOwl extends Vocab{
  override val iri:Iri="http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#"
  val procedure=iri+"procedure"
  val observedProperty=iri+"observedProperty"
  val floatValue=iri+"floatValue"
  val timestamp=iri+"timestamp"    
}


*/