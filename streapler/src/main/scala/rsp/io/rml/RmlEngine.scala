package rsp.io.rml

import rsp.vocab.Rml
import rsp.vocab.R2rml
import rsp.util.JenaTools._
import scala.collection.JavaConversions._
import org.apache.jena.riot.RDFFormat
import org.apache.jena.riot.RDFLanguages
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Statement
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.resultset.RDFInput
import org.apache.jena.datatypes.xsd.impl.RDFLangString
import play.api.libs.json.JsValue
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.graph.NodeFactory
import org.apache.jena.datatypes.RDFDatatype
import org.apache.jena.datatypes.TypeMapper
import rsp.data.Iri
import scala.io.Source
import rsp.io.rml.vocab.SR
import rsp.data.Literal

class RmlEngine {
  
}

object RmlEngine {
  val typer=TypeMapper.getInstance
  
  def toType(node:RDFNode,fun:RDFNode=>Object)={
    
  }
  def getBoolean(rdf:RDFNode)=rdf.asLiteral.getBoolean
  def getString(rdf:RDFNode)=rdf.asLiteral.getString
  
  def objAny[T](res:Resource,fun:RDFNode=>T,prop:Property*):Option[T]={
    obj(res,prop:_*).map(fun)
  }
  def objBoolean(res:Resource,prop:Property*):Option[Boolean]={
    objAny(res,getBoolean,prop:_*)
  }
  def objString(res:Resource,prop:Property*):Option[String]={
    objAny(res,getString,prop:_*)
  }
  def obj(res:Resource,prop:Property*):Option[RDFNode]={
    val stm=res.getProperty(prop(0))
    if (stm==null) None
    else if (prop.size==1)
      Some(stm.getObject)
    else {
      obj(stm.getObject.asResource,prop.tail:_*)
    }
  }
  
  def readPOMaps(res:Resource):collection.Seq[PredicateObjectMap]={
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
    val child=obj(oMap.asResource,R2rml.child).map(_.asLiteral.getString)
    val dtype=obj(oMap.asResource,R2rml.datatype)
      .map(x => typer.getTypeByName(x.toString))
    
    if (parent.isDefined){
      val tMap=parent.get.getModel.listStatements(parent.get.asResource,null,null).next
      
      ObjectMap(null,dtype,Some(readTripleMap(tMap)),null,child.getOrElse(null))
    }
    else {
      ObjectMap(readTermMap(oMap),dtype,None,null,null)
    }
  }
  
  def readTermMap(tMap:RDFNode):MapValue=readTermMap(tMap.asResource)

  def readTermMap(tMap:Resource):MapValue={
    try{
    val template=obj(tMap,R2rml.template)
    if (template.isEmpty){
      val ref=obj(tMap,Rml.reference)
      if (ref.isEmpty){
        val cons=obj(tMap,R2rml.constant)
        if (cons.isEmpty)
          Column(obj(tMap,R2rml.column).get.toString)
        else
          Constant(cons.get.toString)
      }
      else 
        Reference(ref.get.toString)
    }
    else Template(template.get.toString)
    } catch {case t:Throwable=>throw new IllegalArgumentException("Invalid term map "+tMap.isAnon(),t)}
  }
  
  def readDataSource(logicalSource:Statement)={
    val subj=logicalSource.getObject.asResource
    val source=obj(subj,Rml.source)
    val query=objString(subj,Rml.query)
    val params=obj(subj,SR.queryParams)
    val paramList=params.map{ps=>
      ps.asResource.listProperties(SR.params).toSeq.map{p=>
        val param=p.getObject.asResource
        val parName=objString(param,SR.param).get
        val st=subj.getModel.listStatements(null,SR._var, parName).next
        val tmap=readTermMap(st.getSubject)
        val par=Param(parName,
            objString(param,R2rml.column).get,tmap)
        par
      }
    }.getOrElse(Seq())
    
    val tableName=objString(subj,R2rml.tableName)
    source.map{s=>
      val root=objBoolean(subj,Iri(Rml.iri+"root"))
      val cache=objBoolean(subj,Iri(Rml.iri+"cache"))
      val cached=cache.map{c=>
        Source.fromFile(source.get.toString).getLines.map{line=>
          val array=line.split(",")
          array.head -> array
        }.toMap
      }
        
      DataSource(s.toString,query,paramList,None,root.getOrElse(false),cached)      
    }
  }
  
  def readTripleMap(tmap:Statement)={
    
    val logsource=tmap.getSubject.getProperty(Rml.logicalSource)
    val datasource=
      if (logsource==null) None          
      else readDataSource(logsource)
    
    val subMap=obj(tmap.getSubject,R2rml.subjectMap).get
    val mapValue=readTermMap(subMap)      
    val sMap=SubjectMap(mapValue,None)
    val trip=TriplesMap(tmap.getSubject.getURI,datasource,
          sMap,readPOMaps(tmap.getSubject))
          //println("tig "+trip.uri+ " " +datasource)
          trip
    
  }
  
  def readMappings(fileUri:String)={
    val model=try RDFDataMgr.loadModel(fileUri,RDFLanguages.TURTLE)
      catch {case e:Throwable =>throw new IllegalArgumentException("Invalid mapping file.",e)}
    val rootMappings=model.listStatements(null, Rml.logicalSource, null)
    rootMappings.map{tmap=>
      readTripleMap(tmap)
    }.filter(_.source.get.isRoot).toSeq    
  }
}

object JsTransform {
  def convert(js:JsValue,map:MapValue)={
    map match{
      case t:Template=>
        
    }
  }
}
