package rsp.engine

import akka.actor.ActorSystem
import akka.actor.Props

class RspSystem(name:String) {
  val sys=ActorSystem(name)
  //val cqels=new CqelsEngine
  def startStream(str:Props)={
  
    val ws=sys.actorOf(str, "trap")
 //   val ws=sys.actorOf(Props(new WebSocketStream(cqels,"ws://localhost:4040/primus",conf)))
   // cqels.registerQuery(qq, cqels.createListener(lissy))
    
    ws ! StartStream
  }
}