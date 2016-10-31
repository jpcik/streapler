package rsp.cep

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import com.espertech.esper.client.EPServiceProviderManager
import scala.collection.JavaConversions._

class EsperTest extends FlatSpec with Matchers  {
  private val logger= LoggerFactory.getLogger(this.getClass)
  
  "esper" should "run stream" in{

    val prov=EPServiceProviderManager.getDefaultProvider
    val admin=prov.getEPAdministrator
    val config=admin.getConfiguration

    SchemaMgr.defineStreams("src/test/resources/data/metadata/schemas.json", config)
    
    val runtime=prov.getEPRuntime
    
    val ev1=Map("time"->1,"sensor"->"s1","property"->"temp","value"->2.0)
    val ev2=Map("time"->2,"sensor"->"s2","property"->"temp","value"->5.2)
    
    
    runtime.sendEvent(ev1,"lsd")
    runtime.sendEvent(ev2,"lsd")
    
  }
  
}