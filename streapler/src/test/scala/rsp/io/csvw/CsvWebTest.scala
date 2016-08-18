package rsp.io.csvw

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.Lang

class CsvWebTest extends FlatSpec with Matchers  {
  private val logger= LoggerFactory.getLogger(this.getClass)
  
  val pref="src/test/resources/"
  
  //RDFDataMgr.loadModel(pref+"csvw/demo.json",Lang.JSONLD)
  
  "jsonld metadata" should "be parsed" in{
    //CsvWeb.readMetadata("src/test/resources/csvw/obs.csv-meta.jsonld")
    CsvWeb.generateRdf(pref+"data/opensense_small.csv", pref+"csvw/obs.csv-meta.jsonld")
  }
  
}