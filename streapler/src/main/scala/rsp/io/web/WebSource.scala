package rsp.io.web

import play.api.libs.ws._
import play.api.libs.ws.ning._
import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.json._
import rsp.io.rml.RmlEngine
import rsp.io.rml._
import play.api.libs.concurrent._
import rsp.data._
import rsp.engine.RspFeed

trait WebSource {
  def pullData:Any
}

class JsonWebFeed(stre:JsonWebStream,rate:Int) extends RspFeed(stre,rate){
  def produce={
    stre.data
  }
}

class JsonWebStream(tmap:TriplesMap,props:Map[String,Any]) extends RdfStream {
  //val uri="http://api.citybik.es/metropolradruhr-germany-dortmund.json"
  val uri= transform(Template(tmap.source.get.uri),props)
  override val name=Iri(uri)
 // "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_hour.geojson"
  val id="id"
  val timestamp="time"//"timestamp"
  implicit val context =Execution.Implicits.defaultContext
  //import play.api.Play.current
  def wsClient={
    val clientConfig = new DefaultWSClientConfig()
    val secureDefaults = new NingAsyncHttpClientConfigBuilder(clientConfig).build()
    val builder = new AsyncHttpClientConfig.Builder(secureDefaults)
    builder.setCompressionEnabled(true)
    val secureDefaultsWithSpecificOptions = builder.build()
    new NingWSClient(secureDefaultsWithSpecificOptions)    
  }
  
  def data={
    pullData
  }
  
  def pullData={
    //play.api.Play.current.    
    implicit val implicitClient = wsClient
    import rsp.data.RdfTools._
    import rsp.data.Literal._
//println("before web call "+uri)
    val resp=WS.clientUrl(uri).get.map{response=>
      val js=(response.json  ).as[JsArray]
      //println(js)
      val pip=(js).value.map{j=>
        //println("j "+j)
        val subjUri=transform(tmap.sMap.value,j)
        //println("the subj "+subjUri)
        Thread.sleep(200)
        Graph(tmap.poMaps.map{po=>
          //println("traba "+po.pMap )
          val p=transform(po.pMap.value,j) 
                //println("tigi"+p )
                
    val oo=
      if (po.oMap.value==null){
        transform(po.oMap.parentTriplesMap.get.sMap.value,j )
      }
      else
      transform(po.oMap.value,j)
          //println("rabab"+ oo)
          Triple(subjUri,p,lit(oo))
        }:_*)
        //println("dib "+subjUri)
      }
      pip
    }
    
    resp.onComplete{a=>
      
      println("failed: "+a.isFailure)
      //a.failed.get.printStackTrace()
      implicitClient.close
    
    }

    resp
    
  }

  def transform(template:Template,prop:Map[String,Any])={   
    var finalUri=template.template 
    template.vars foreach{v=>
      finalUri=finalUri.replace(s"{$v}",prop(v).toString)  
    }
    finalUri 
  }
  
  def transform(map:MapValue,js:JsValue):String=map match {
    case t:Template=>val tt=transform(t,js)
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
    //println(finalUri)
    finalUri 
  }

  def transform(ref:Reference,js:JsValue)={
               
      (js \ ref.ref ) match {
        case u:JsUndefined=>
          null
        case a:JsValue=>
          a.toString    
      }              
  }

}

object Call{
  def main(arr:Array[String]):Unit={
    
    val map=RmlEngine.readMappings("src/test/resources/citybikes.rml")
    val props=Map("sourceid"->"metropolradruhr-germany-dortmund")
    val json=new JsonWebStream(map.head,props)
    json.pullData
  }
}


