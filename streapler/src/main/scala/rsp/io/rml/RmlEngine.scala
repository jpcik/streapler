package rsp.io.rml

import com.hp.hpl.jena.sparql.resultset.RDFInput
import com.hp.hpl.jena.rdf.model.RDFReader
import org.apache.jena.riot.RDFDataMgr
import rsp.vocab.Rml
import rsp.util.JenaTools._
import scala.collection.JavaConversions._
import org.apache.jena.riot.RDFFormat
import com.hp.hpl.jena.datatypes.xsd.impl.RDFLangString
import org.apache.jena.riot.RDFLanguages
import rsp.vocab.R2rml
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.Statement
import play.api.libs.json.JsValue

class RmlEngine {
  
}

object RmlEngine {
  
  def obj(res:Resource,prop:Property*):Option[RDFNode]={
    val stm=res.getProperty(prop(0))
    if (stm==null) None
    else if (prop.size==1)
      Some(stm.getObject)
    else {
      obj(stm.getObject.asResource,prop.tail:_*)
    }
  }
  
  def readPOMaps(res:Resource):Seq[PredicateObjectMap]={
    res.listProperties(R2rml.predicateObjectMap ).map{stm=>      
      val poMap=stm.getObject.asResource
      val pred=obj(poMap,R2rml.predicate)
      val objMap=obj(poMap,R2rml.objectMap).get
      PredicateObjectMap(PredicateMap(Constant(pred.get.toString)),
          readObjectMap(objMap))      
    }.toSeq
  }

  def readObjectMap(oMap:RDFNode):ObjectMap={
    val parent=obj(oMap.asResource,R2rml.parentTriplesMap)
    if (parent.isDefined){
      val tMap=parent.get.getModel.listStatements(parent.get.asResource,null,null).next
      ObjectMap(null,Some(readTripleMap(tMap)))
    }
    else {
      ObjectMap(readTermMap(oMap),None)
    }
  }
  
  def readTermMap(tMap:RDFNode):MapValue=readTermMap(tMap.asResource)

  def readTermMap(tMap:Resource):MapValue={
    val template=obj(tMap,R2rml.template)
    if (template.isEmpty){
      val ref=obj(tMap,Rml.reference)
      if (ref.isEmpty)
        Constant(obj(tMap,R2rml.constant).get.toString)
      else 
        Reference(ref.get.toString)
    }
    else Template(template.get.toString)        
  }
  
  def readTripleMap(tmap:Statement)={
      val source=obj(tmap.getObject.asResource,Rml.source)
      val datasource=source.map(src=>DataSource(source.get.toString,None))

      val subMap=obj(tmap.getSubject,R2rml.subjectMap).get
      val mapValue=readTermMap(subMap)      
      val sMap=SubjectMap(mapValue,None,None)            
      TriplesMap(tmap.getSubject.getURI,datasource,
          sMap,readPOMaps(tmap.getSubject))
    
  }
  
  def readMappings(fileUri:String)={
    val model=RDFDataMgr.loadModel(fileUri,RDFLanguages.TURTLE)
    val rootMappings=model.listStatements(null, Rml.logicalSource, null)
    rootMappings.map{tmap=>
      readTripleMap(tmap)
    }.toSeq    
  }
}

object JsTransform {
  def convert(js:JsValue,map:MapValue)={
    map match{
      case t:Template=>
        
    }
  }
}
