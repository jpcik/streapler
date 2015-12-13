package rsp.engine.cqels
import com.hp.hpl.jena.vocabulary.RDF
import rsp.vocab._
import rsp.data.RdfTools._
import rsp.data._
import rsp.data.{Triple=>RspTriple}
import com.typesafe.config.Config
import collection.JavaConversions._
import rsp.engine.RspReasoner

class SsnStream(cqels:RspReasoner,uri:String,conf:Config) extends RspStream(cqels,uri,conf){
  val met="http://purl.org/env/meteo#"
  val obs="http://purl.org/env/sensing/"
  val aws="http://purl.oclc.org/NET/ssnx/meteo/aws#"
  val cff="http://purl.oclc.org/NET/ssnx/cf/cf-feature#"
  val qu="http://purl.oclc.org/NET/ssnx/qu/quantity#"
  val dim="http://purl.oclc.org/NET/ssnx/qu/dim#"
    
  val items=conf.getConfigList("items").map{c=>
    (c.getString("id"),c.getInt("size"))
  }  
  
    
  override def produce()={
    items.foreach{item=>
      (1 to item._2 ) foreach{i=>
        streams(item._1).foreach(stream)    
      }
    }
  }
  
  def tri=RspTriple.apply _

  def currentobs:Iri=s"${obs}obs${System.currentTimeMillis}"

  val sensor1=Iri(s"${obs}sensor1")
  val loc1=Iri(s"${obs}loc1")
  
  val TemperatureObservation:Iri=s"${met}TemperatureObservation"
  val AirTemperatureObservation:Iri=s"${met}AirTemperatureObservation"
  val PrecipitationObservation:Iri=s"${met}PrecipitationObservation"
  val HumidityObservation:Iri=s"${met}HumidityObservation"
    val RelativeHumidityObservation:Iri=s"${met}RelativeHumidityObservation"

  val TemperatureSensor:Iri=s"${aws}TemperatureSensor"
  val Thermistor:Iri=s"${aws}Thermistor"
  val CapacitiveBead:Iri=s"${aws}CapacitiveBead"
  val Temperature:Iri=s"${dim}Temperature"
  val temperature:Iri=s"${qu}temperature"
  val AirMedium:Iri=s"${met}AirMedium"
  val air:Iri=s"${cff}air"
  val locatedIn:Iri=s"${met}locatedIn"
  
  def item1={
    val cobs=currentobs
    "tempObs"->Seq(
    tri(cobs,Rdf.a,TemperatureObservation)
    //,tri(cobs,locatedIn,loc1)
    
  
  )}
  def item2="obsByTherm"->Seq(
    tri(currentobs,Ssn.observedBy,sensor1),
    tri(sensor1,Rdf.a,Thermistor)            
  )

  def item3={
   val cobs=currentobs  
    "precObs"->Seq(
  
    tri(cobs,Rdf.a,PrecipitationObservation)            
    )}
def item33={
   val cobs=currentobs  
    "relhumObs"->Seq(
  
    tri(cobs,Rdf.a,RelativeHumidityObservation)            
    )}
  def item4="obsByBead"->Seq(
    tri(currentobs,Ssn.observedBy,sensor1),
    tri(sensor1,Rdf.a,CapacitiveBead)            
  )

  def item5="featAirTempObs"->Seq(
    tri(currentobs,Ssn.featureOfInterest ,air),            
    tri(currentobs,Rdf.a ,TemperatureObservation)            
  )

  def item6="featAirObsTemp"->Seq(
    tri(currentobs,Ssn.featureOfInterest ,air),            
    tri(currentobs,Ssn.observedProperty ,temperature)            
  )

  def item7="featAirMedTempObs"->Seq(
    tri(currentobs,Ssn.featureOfInterest ,met+"_air"),            
    tri(met+"_air",Rdf.a ,AirMedium),            
    tri(currentobs,Rdf.a ,TemperatureObservation)            
  )

  def item8="featAirObsByTempSensor"->Seq(
    tri(currentobs,Ssn.featureOfInterest ,air),            
    tri(currentobs,Ssn.observedBy ,sensor1),            
    tri(sensor1,Rdf.a ,TemperatureSensor)            
  )

  def item9="featAirMediumObsPropTemp"->Seq(
    tri(currentobs,Ssn.featureOfInterest ,met+"_air"),            
    tri(met+"_air",Rdf.a ,AirMedium),            
    tri(currentobs,Ssn.observedProperty ,temperature)            
  )

  def item10="featAirMediumObsByTempSensor"->Seq(
    tri(currentobs,Ssn.featureOfInterest ,met+"_air"),            
    tri(met+"_air",Rdf.a ,AirMedium),            
    tri(currentobs,Ssn.observedBy ,sensor1),            
    tri(sensor1,Rdf.a ,TemperatureSensor)            
  )
  
  def item11="featAirMediumObsPropTempQ"->Seq(
    tri(currentobs,Ssn.featureOfInterest ,met+"_air"),            
    tri(met+"_air",Rdf.a ,AirMedium),            
    tri(currentobs,Ssn.observedProperty ,met+"_temp"),            
    tri(met+"_temp",Rdf.a ,Temperature)            
  )

  def item12="featAirMediumObsObyByTherm"->Seq(
    tri(currentobs,Ssn.featureOfInterest ,met+"_air"),            
    tri(met+"_air",Rdf.a ,AirMedium),            
    tri(currentobs,Ssn.observedBy ,sensor1),            
    tri(sensor1,Rdf.a ,Thermistor )            
  )

  def streams=Map(item1,item2,item3,item4,item5,item6,item7,item8,item9,item10,item11,item12,item33)
  
}


