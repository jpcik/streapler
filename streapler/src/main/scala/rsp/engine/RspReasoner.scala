package rsp.engine

trait RspReasoner {
  var regQueries=0
  var inputCount=0
  import rsp.data.{Triple=>RdfTriple}
  
  def stop:Unit
  
  def consume(uri:String,t:RdfTriple):Unit  

  def registerQuery(q:String,listener:RspListener,reasoner:Boolean=false)
}


abstract class RspListener(val listener:Any){
  def count:Long                        
}
  