package com.joescii.scalaz.stream

import org.scalatest.{ShouldMatchers, WordSpec}

import scalaz.stream.Process
import StreamFunctions._

class ChunkWhenSpec extends WordSpec with ShouldMatchers {
  "chunkWhen" should {
    "work" in {
      val p = Process(1,2,3,5,6,7,10,11)
      val c = p |> chunkWhen((a, b) => b - a == 1)

      c.toList shouldEqual List(Vector(1,2,3), Vector(5,6,7), Vector(10,11))
    }
  }
}
