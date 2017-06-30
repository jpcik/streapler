package rsp.vocab

import rsp.data.Iri
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.vocabulary.RDF
import collection.JavaConversions._
import org.apache.jena.vocabulary.OWL
import org.apache.jena.rdf.model.Statement
import org.apache.jena.vocabulary.RDFS

object VocabGen {  
  
  import treehugger.forest._
  import definitions._
  import treehuggerDSL._
  
  val IriType=TYPE_REF("Iri")
  
  case class VocabConf(name:String,uri:String,pack:String)
  case class VocabMeta(name:String,uri:String,
      classes:Iterator[String],properties:Iterator[String],individuals:Iterator[String])        
      
  def genTree(conf:VocabConf,classes:Seq[String],properties:Seq[String])={
    BLOCK(
      IMPORT("rsp.vocab._") ,
      IMPORT("rsp.data._") ,
      IMPORT("rsp.data.RdfTools._"),
      OBJECTDEF(conf.name) withParents("Vocab") := BLOCK(
        Seq(VAL("iri",IriType) withFlags(Flags.OVERRIDE) := LIT(conf.uri))++
        classes.map{r=>
          VAL(r) := REF("clazz") APPLY (LIT(r))
        } ++        
        properties.map{r=>
          VAL(r) := REF("prop") APPLY (LIT(r))
        }        
      ) 
    ) inPackage(conf.pack)
  }
  
  def readVocab(url:String)={
    val m=RDFDataMgr.loadModel(url)
    val shortName=url.split("/").last.toUpperCase.replace("-","_").replace("#","")
    val defPrefix=m.getNsPrefixURI("")
    val imports=m.listObjectsOfProperty(OWL.imports).toSeq.map(_.asResource.getURI)
    val rootPrefix=
      if (defPrefix==null) url
      else defPrefix
    
      
    def isLocal(s:Statement)={
      println(s.getSubject)
      s.getSubject.isURIResource && !imports.contains(s.getSubject.getNameSpace) 
      //s.getSubject.getURI.startsWith(rootPrefix)
      
    }
    def toLocalName(stms:Iterator[Statement])=
      stms.filter(isLocal).map(_.getSubject.getLocalName)
    
    val classNames=toLocalName(
      m.listStatements(null, RDF.`type`, OWL.Class)++
      m.listStatements(null, RDF.`type`, RDFS.Class)  )
      
    
    val propNames=toLocalName(
      m.listStatements(null,RDF.`type`,OWL.ObjectProperty)++
      m.listStatements(null,RDF.`type`,OWL.DatatypeProperty)++
      m.listStatements(null,RDF.`type`,RDF.Property))

    VocabMeta(shortName,rootPrefix,classNames,propNames,null)
  }
  
  
  def main(args:Array[String])={
    val meta=readVocab("http://purl.oclc.org/NET/ssnx/cf/cf-feature")
    val conf=VocabConf(meta.name,meta.uri,"rsp.vocab.pip")
    println(treeToString(genTree(conf,meta.classes.toSeq,meta.properties.toSeq)))
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