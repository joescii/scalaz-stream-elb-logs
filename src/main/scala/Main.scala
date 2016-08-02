package com.joescii.scalaz.stream

import scalaz.concurrent.Task

object Main extends App {
  val program: Task[Unit] = Analyzer(Sources.fromFileSystem)

  println("I have a program, but I'm not ready to run it")

  program.unsafePerformSync
}
