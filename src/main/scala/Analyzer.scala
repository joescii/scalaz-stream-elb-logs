package com.joescii.scalaz.stream

import scalaz.concurrent.Task
import scalaz.stream.io
import scalaz.stream.Process

object Analyzer {
  def apply(source:Process[Task, String]):Task[Unit] =
    source
      .map(LogEntry.apply)
      .collect { case Some(e) => e }
      .map(t => Time.logDateFormat.format(t.timestamp.getTime))
      .intersperse("\n")
      .to(io.stdOut)
      .run
}
