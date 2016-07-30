package com.joescii.scalaz.stream

import org.scalatest.{ShouldMatchers, WordSpec}

import scalaz.stream._
import StreamFunctions._

class SortLocallySpec extends WordSpec with ShouldMatchers {
  case class Foo(i: Int, s: String)
  implicit def orderingByInt[A <: Foo]: scala.math.Ordering[A] =
    scala.math.Ordering.by(f => f.i)

  "support reordering values over a specified range such that output is monotonically increasing" which {
    def ts(value: Int) = Foo(value, value.toString)
    val f = (f: Foo) => f.i.toLong

    val onTheSecond = Process.range(1, 10) map { x => ts(x * 1000) }
    val onTheQuarterPast = onTheSecond map { f => f.copy(i = f.i + 250) }

    "reorders when all out of order values lie within the range" in {
      val inOrder = onTheSecond interleave onTheQuarterPast
      val outOfOrder = onTheQuarterPast interleave onTheSecond
      val reordered = outOfOrder pipe sortLocally(f, 1000)
      reordered.toList shouldBe inOrder.toList
    }

    "drops values that appear outside the range" in {
      // Create mostly ordered data with clumps of values around each second that are unordered
      val events = Process.range(1, 10) flatMap { x =>
        val local = (-10 to 10).map { y => ts((x * 1000) + (y * 10)) }
        Process.emitAll(util.Random.shuffle(local))
      }
      val reordered200ms = events pipe sortLocally(f, 200)
      reordered200ms.toList shouldBe events.toList.sorted

      val reordered20ms = events pipe sortLocally(f, 20)
      reordered20ms.toList.size should be >= 10
    }

    "emits values with the same order value in insertion order" in {
      val onTheSecondBumped = onTheSecond map { f => f.copy(i = f.i + 1) }
      val inOrder = (onTheSecond interleave onTheQuarterPast) interleave (onTheSecondBumped interleave onTheQuarterPast)
      val outOfOrder = (onTheQuarterPast interleave onTheSecond) interleave (onTheQuarterPast interleave onTheSecondBumped)
      val reordered = outOfOrder pipe sortLocally(f, 1000)
      reordered.toList shouldBe inOrder.toList
    }
  }

}
