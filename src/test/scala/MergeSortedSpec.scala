package com.joescii.scalaz.stream

import org.scalacheck.Arbitrary._
import org.scalacheck.{Gen, Arbitrary, Properties}
import org.scalacheck.Prop._
import scalaz.\&/
import scalaz.std.anyVal._
import scalaz.syntax.std.list._
import scalaz.stream._

class MergeSortedSpec extends Properties("mergeSorted") {

  case class Foo(i: Int, j: Int, s: String)
  case class Bar(i: Int, j: Int, s: String)

  implicit val arbitraryFoo = Arbitrary {
    for {
      (i, j, s) <- arbitrary[(Int, Int, String)]
    } yield Foo(i, j, s)
  }
  implicit val arbitraryBar = Arbitrary {
    for {
      (i, j, s) <- arbitrary[(Int, Int, String)]
    } yield Bar(i, j, s)
  }

  implicit val arbitraryFooInt: Arbitrary[Foo => Int] = Arbitrary { Gen.oneOf((_: Foo).i, (_: Foo).j) }
  implicit val arbitraryBarInt: Arbitrary[Bar => Int] = Arbitrary { Gen.oneOf((_: Bar).i, (_: Bar).j) }

  private def distinctOn[A, B](on: A => B): List[A] => List[A] = as => as.groupBy1(on).values.map(_.head).toList

  property("basic") = forAll { (unsortedFoos: List[Foo], unsortedBars: List[Bar], f: Foo => Int, g: Bar => Int) =>
    val foos = distinctOn(f)(unsortedFoos).sortBy(f)
    val bars = distinctOn(g)(unsortedBars).sortBy(g)

    val merged: Process0[Foo \&/ Bar] = StreamFunctions.mergeSorted(Process.emitAll(foos), f)(Process.emitAll(bars), g)

    val result = merged.toSource.runLog.unsafePerformSync.toList

    ("Output stream contains all values from left source" |: result.collect(Function.unlift(_.a)) == foos)  &&
      ("Output stream contain all values from right source" |: result.collect(Function.unlift(_.b)) == bars)  &&
      ("Values from both side with the same key are matched" |: result.collect(Function.unlift(_.onlyBoth)).forall(t => f(t._1) == g(t._2))) &&
      ("Output stream is sorted" |: result.map(_.fold(f, g, (a, _) => f(a))) == (foos.map(f) ++ bars.map(g)).distinct.sorted)
  }

  property("same type") = forAll { (unsortedFoos1: List[Foo], unsortedFoos2: List[Foo], f: Foo => Int) =>
    val foos1 = distinctOn(f)(unsortedFoos1).sortBy(f)
    val foos2 = distinctOn(f)(unsortedFoos2).sortBy(f)

    val merged: Process0[Foo] = StreamFunctions.mergeSorted(Process.emitAll(foos1), Process.emitAll(foos2))(f)

    val result = merged.toSource.runLog.unsafePerformSync.toList

    ("Output stream contains all values from left source" |: foos1.forall(result.contains)) &&
      ("Output stream contain all values from right source" |: foos2.forall(result.contains)) &&
      ("Output stream is sorted" |: result.map(f).distinct == (foos1.map(f) ++ foos2.map(f)).distinct.sorted)
  }
}