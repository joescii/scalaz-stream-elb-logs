package com.joescii.scalaz.stream

import org.http4s.{Method, Request, Status, Uri}
import org.http4s.client.blaze.SimpleHttp1Client

import scalaz.{-\/, \/-}
import scalaz.concurrent.Task
import scalaz.stream.io
import scalaz.stream.Process

object Sources {
  lazy val fromFileSystem:Process[Task, String] = io.linesR("./logs/AWSLogs.log")

  private [this] val c = SimpleHttp1Client()
  private [this] val uri = Uri.uri("https://raw.githubusercontent.com/ozantunca/elb-log-analyzer/master/logs/AWSLogs.log")

  lazy val fromTehWebz:Process[Task, String] = {
    val t = c.fetch(Request(Method.GET, uri)){ res => res.status match {
      case Status.Ok => res
        .bodyAsText
        .runLog
    }}

    Process.await(t)(Process.emitAll)
  }
}
