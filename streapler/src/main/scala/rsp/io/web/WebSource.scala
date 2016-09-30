package rsp.io.web

import play.api.libs.concurrent._
import play.api.libs.json._
import play.api.libs.ws._
import play.api.libs.ws.ning._
import rsp.io.rml.RmlEngine
import rsp.io.rml._
import rsp.data._
import rsp.data.RdfTools._
import rsp.data.Literal._
import rsp.engine.RspFeed
import com.ning.http.client.AsyncHttpClientConfig
import org.joda.time.format.ISODateTimeFormat

trait WebSource {
  def pullData:Any
}

class JsonWebStream(tmap:TriplesMap,props:Map[String,Any]) extends RdfStream {
  val uri= transform(Template(tmap.source.get.uri),props)
  override val name=Iri(uri)
  var lastTimestamp:Long=0
  var currentTimestamp=0l
  val timestamp=tmap.source.get.timestamp.getOrElse("timestamp")
  
  
  implicit val context =Execution.Implicits.defaultContext
  def wsClient={
    val clientConfig = new DefaultWSClientConfig()
    val secureDefaults = new NingAsyncHttpClientConfigBuilder(clientConfig).build()
    val builder = new AsyncHttpClientConfig.Builder(secureDefaults)
    builder.setCompressionEnforced(true)
    val secureDefaultsWithSpecificOptions = builder.build()
    new NingWSClient(secureDefaultsWithSpecificOptions)    
  }
  
  def data={
    pullData
  }
  
  def pullData={
    implicit val implicitClient = wsClient
    val resp=WS.clientUrl(uri).get.map{response=>
      val jsArray=tmap.source.get.dataPath.map{path=>      
        (response.json \ path)}.getOrElse(response.json).as[JsArray]
      jsArray.value.filter(isNew)
      .map(json => mkTriples(json))
    }
    
    resp.onComplete{a=> 
      lastTimestamp=currentTimestamp
      println("failed: "+a.isFailure+" "+lastTimestamp)
      if (a.isFailure)
        a.failed.get.printStackTrace()
      implicitClient.close    
    }
    resp    
  }

  def ts(js:JsValue)={
    val dt=(js \\ "created_at").head.as[String]
    val time=ISODateTimeFormat.dateTimeParser().parseDateTime(dt).getMillis

    (js,time)
  }
  
  def isNew(js:JsValue)={   
    //println(js)
    val dt=(js \\ timestamp).head.as[String]
    //println(dt)
    val time=ISODateTimeFormat.dateTimeParser().parseDateTime(dt).getMillis
    //println(time+"-"+lastTimestamp)
    if (time>currentTimestamp) 
      currentTimestamp=time
    time>lastTimestamp
    
  }
  
  def mkTriples(js:JsValue)={
    val subjUri=transform(tmap.sMap.value,js).toString      
    Graph(tmap.poMaps.map{po=>
      val p=transform(po.pMap.value,js).toString                 
      val oo=
        if (po.oMap.value==null)
          transform(po.oMap.parentTriplesMap.get.sMap.value,js)
        else
          transform(po.oMap.value,js)
      Triple(subjUri,p,lit(oo)) 
    }:_*)
  }
  
  def transform(template:Template,prop:Map[String,Any])={   
    var finalUri=template.template 
    template.vars foreach{v=>
      finalUri=finalUri.replace(s"{$v}",prop(v).toString)  
    }
    finalUri 
  }
  
  def transform(map:MapValue,js:JsValue):Any=map match {
    case t:Template=>
      val tt=transform(t,js)
      //println(tt)
      tt
    case r:Reference=>transform(r,js)
    case c:Constant=>transform(c)
    case _=>
      println("no idea what to do here "+map.getClass)
      throw new NotImplementedError
  }
  
  def transform(constant:Constant)={
    constant.const 
  }
  

  def transform(template:Template,js:JsValue)={
    var finalUri=template.template 
    template.vars foreach{v=>      
      (js \ v) match {
        case u:JsUndefined=>
          finalUri=finalUri.replace(s"{$v}",props(v).toString)
        case a:JsValue=>
          finalUri=finalUri.replace(s"{$v}",a.toString)    
      }              
    }
    finalUri 
  }

  def transform(ref:Reference,js:JsValue):Any={               
      (js \\ ref.ref ).head match {
        case u:JsUndefined=>
          null
        case i:JsNumber=>
          i.as[Long]
        case a:JsValue=>
          a.toString
          
      }              
  }

}

object Call{
  
  def bikesJson={
    val map=RmlEngine.readMappings("src/test/resources/citybikes.rml")
    val props=Map("sourceid"->"metropolradruhr-germany-dortmund")
    new JsonWebStream(map.head,props)
  }
   
  def trendsJson={
    val map=RmlEngine.readMappings("src/test/resources/trends.rml")
    val props:Map[String,String]=Map()
    new JsonWebStream(map.head,props)
  }
  
  def main(arr:Array[String]):Unit={
    import scala.concurrent.ExecutionContext.Implicits.global
   val js=trendsJson
    while (true){
            
      
      //js.updateLast(23)
      println("demonios "+js.lastTimestamp)
      js.pullData.map { x => x.foreach { println } }
    
    Thread.sleep(10000)
    }
  }
}


