package rsp.io

case class Template(template:String){
  lazy val vars={
    val reg="""(\{[a-zA-Z0-9]*\})""".r
    reg.findAllIn(template).map(s=>s.substring(1, s.size-1)).toSeq
  }
  def replace(data:Array[String])= ???
}
  