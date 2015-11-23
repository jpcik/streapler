package rsp.engine.cqels
import com.hp.hpl.jena.vocabulary.RDF
import rsp.vocab._
import rsp.data.Rdf._

class SsnStream(cqels:CqelsReasoner,uri:String,rate:Int) extends CqelsStream(cqels,uri,rate){
  override def produce()={
    val obs=Ssn("obs"+System.currentTimeMillis)
    val sens=rsp.data.Iri("http://oeg-upm.net/onto/sensordemo/sens1")

    stream(obs,Ssn.observedBy,sens)
    stream(sens,RDF.`type`.toString,"http://purl.oclc.org/NET/ssnx/meteo/aws#Thermistor")    
    //stream(obs,OmOwl.timestamp ,System.currentTimeMillis.toString)))))
  }
}