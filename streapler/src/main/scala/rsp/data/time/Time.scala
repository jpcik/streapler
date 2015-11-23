package rsp.data.time

import org.joda.time.Minutes
import org.joda.time.Duration
import org.joda.time.Period
import org.joda.time.ReadablePeriod
import org.joda.time.Hours

/*
case class TimeUnit(val factor:Double,val name:String){
  override def toString=s"TimeUnit($factor)"
}

object TimeUnit {

  val m:ReadablePeriod=Minutes.minutes(3)
  
  val MILLISECOND =TimeUnit(0.001,"millisecond")
  val SECOND= TimeUnit(1,"second")
  val MINUTE= TimeUnit(60,"minute")
  val HOUR =TimeUnit(3600,"hour")
  val DAY =TimeUnit(3600*24,"day")
  val WEEK =TimeUnit(3600*24*7,"week")
  val MONTH =TimeUnit(3600*24*30,"month")
  val YEAR =TimeUnit(3600*24*365,"year")
  
  def convertToBase(value:Double,unit:TimeUnit)=value*unit.factor

  def convertToUnit(value:Double,unit:TimeUnit,targetUnit:TimeUnit)={
	val inBase = convertToBase(value, unit)
	inBase/targetUnit.factor
  }
}*/
