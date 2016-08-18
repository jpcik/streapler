package rsp.rspql

import org.apache.jena.query.Query
import scala.collection.mutable.ArrayBuffer
import rsp.rspql.syntax.ElementNamedWindow
import collection.mutable.HashMap

class StreamQuery() extends Query {
  val streams = HashMap[String,ElementNamedWindow]()
  val modifiers = HashMap[String,Modifier]()
  lazy val r2s = modifiers("r2s") 
  def addModifier(k:String,str:String):Unit=modifiers.put(k,R2SModifier.parse(str))
}

trait Modifier
trait R2SModifier extends Modifier
object R2SModifier{
  def parse(r2sString:String):Modifier=r2sString.toLowerCase match {
    case "rstream"=>Rstream
    case "istream"=>Istream
    case "dstream"=>Dstream
  }
}

object Rstream extends R2SModifier
object Istream extends R2SModifier
object Dstream extends R2SModifier
