package rsp.io.rml

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import java.net.URI

class RmlTest extends FlatSpec with Matchers  {
  private val logger= LoggerFactory.getLogger(this.getClass)
  
  "rml" should "be parsed" in{
    val tmaps=RmlEngine.readMappings("citybikes.rml")
    tmaps.map{tmap=>
      println(tmap.uri)
      tmap.poMaps.size should be (4)
      val stationTmap= tmap.refPoMaps(0)
        .oMap.parentTriplesMap.get
      stationTmap.uri should endWith("StationMapping")
      stationTmap.refPoMaps(0).oMap.parentTriplesMap.get.uri should endWith("LocationMapping")
        
    }
  }
  
  val tmaps=RmlEngine.readMappings("lsd.rml")

  "lsd rml" should "be parsed" in{    
      tmaps.size should be (1)     
      tmaps(0).refPoMaps.size should be (2)
      val instantMap = tmaps(0).poMaps
        .filter(_.pMap.value.toString.endsWith("samplingTime"))
        .head.oMap.parentTriplesMap.get
      instantMap.uri should endWith ("InstantMap")
      instantMap.poMaps.size should be (2)
      instantMap.poMaps.foreach { po =>
        println(po.pMap.value)
      }
      instantMap.refPoMaps.size should be (0)
  }
  
  "it" should "produce triples" in{
     val csv=new RmlCsv(tmaps(0))
     val started=System.currentTimeMillis
     csv.generate 
     println(s"Elapsed time: ${System.currentTimeMillis-started}")
  }

  
  
}