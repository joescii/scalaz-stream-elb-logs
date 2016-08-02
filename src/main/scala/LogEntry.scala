package com.joescii.scalaz.stream

import java.util.Calendar

import scala.util.Try

case class LogEntry(timestamp:Calendar, ip:String, port:Int, status:Int)

object LogEntry {
  def apply(line:String):Option[LogEntry] = {
    val splitLine = line.split("""\s+""")

    for {
      timeStr <- splitLine.lift(0)
      time <- Try(Time.logDateFormat.parse(timeStr)).toOption
      ipPortStr <- splitLine.lift(2)
      ipPortSplit = ipPortStr.split(':')
      ip <- ipPortSplit.lift(0)
      portStr <- ipPortSplit.lift(1)
      port <- Try(portStr.toInt).toOption
      statusStr <- splitLine.lift(7)
      status <- Try(statusStr.toInt).toOption
    } yield LogEntry(time, ip, port, status)
  }
}
