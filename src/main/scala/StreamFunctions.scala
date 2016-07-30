package com.joescii.scalaz.stream

import scalaz.Ordering.{EQ, GT, LT}
import scalaz.\&/.{Both, That, This}
import scalaz.{Order, \&/}
import scalaz.stream.Process._
import scalaz.stream._

object StreamFunctions {

  // Complements of http://stackoverflow.com/questions/19103388/scalaz-stream-group-sorted-database-results#answer-19104133
  def chunkWhen[I](f: (I, I) => Boolean): Process1[I, Vector[I]] = {
    def go(acc: Vector[I]): Process1[I,Vector[I]] =
      receive1Or[I, Vector[I]](emit(acc)){ i =>
        acc.lastOption match {
          case Some(last) if ! f(last, i) => emit(acc) ++ go(Vector(i))
          case _ => go(acc :+ i)
        }
      }
    go(Vector())
  }

  // Complements of https://twitter.com/mpilquist
  // https://github.com/scodec/scodec-protocols/blob/v0.13.0/src/main/scala/scodec/protocols/TimeStamped.scala#L242-L258
  def sortLocally[I](f: I => Long, range: Long): Process1[I, I] = { // TODO: Can we replace Long with something like A: Order ?
    import scala.collection.immutable.SortedMap

    def emitMapValues(m: SortedMap[Long, Vector[I]]) =
      emitAll(m.foldLeft(Vector.empty[I]) { case (acc, (_, tss)) => acc ++ tss })

    def go(buffered: SortedMap[Long, Vector[I]]): Process1[I, I] = {
      receive1Or[I, I](emitMapValues(buffered)) { t =>
        val until = f(t) - range
        val (toEmit, toBuffer) = buffered span { case (x, _) => x <= until }
        val updatedBuffer = toBuffer + (f(t) -> (toBuffer.getOrElse(f(t), Vector.empty[I]) :+ t))
        emitMapValues(toEmit) ++ go(updatedBuffer)
      }
    }
    go(SortedMap.empty)
  }

  /**
    * Merge two source processes, `leftSource` and `rightSource`, that are ALREADY SORTED by distinct key `A`,
    * and produce an output process, which will also be sorted by `A`.
    *
    * If a key is only present in the `leftSource`, the output for this key will be a `This` containing the value from
    *
    * the `leftSource`. If a key is only present in the 'rightSource`, the output for this key will be a `That`
    * containing the value from the `rightSource`. If a key is present in both, the output for this key will be a
    * `Both`, containing values from both sources.
    *
    * Again thanks to the fine work of https://twitter.com/mpilquist
    * https://github.com/functional-streams-for-scala/fs2/pull/543/
    *
    * @param sourceLeft
    * @param sourceRight
    * @param fa
    * @param fb
    */
  def mergeSorted[F[_], L, R, A: Order](sourceLeft: Process[F, L], fa: L => A)(sourceRight: Process[F, R], fb: R => A): Process[F, L \&/ R] = {
    def next(l: L, r: R): Tee[L, R, L \&/ R] =
      Order[A].order(fa(l), fb(r)) match {
        case EQ => Process.emit(Both(l, r)) ++ nextLR
        case LT => Process.emit(This(l)) ++ nextL(r)
        case GT => Process.emit(That(r)) ++ nextR(l)
      }

    def passL: Tee[L, R, L \&/ R] = tee.passL map This.apply
    def passR: Tee[L, R, L \&/ R] = tee.passR map That.apply

    def nextL(r: R): Tee[L, R, L \&/ R] =
      tee.receiveLOr[L, R, L \&/ R](Process.emit(That(r)) ++ passR)(next(_, r))
    def nextR(l: L): Tee[L, R, L \&/ R] =
      tee.receiveROr[L, R, L \&/ R](Process.emit(This(l)) ++ passL)(next(l, _))
    def nextLR: Tee[L, R, L \&/ R] =
      tee.receiveLOr(passR)(nextR)

    sourceLeft.tee(sourceRight)(nextLR)
  }

  def mergeSorted[F[_], I, A: Order](sourceLeft: Process[F, I], sourceRight: Process[F, I])(f: I => A): Process[F, I] = {
    mergeSorted(sourceLeft, f)(sourceRight, f).flatMap {
      case Both(a, b) => Process.emit(a) ++ Process.emit(b)
      case This(a) => Process.emit(a)
      case That(b) => Process.emit(b)
    }
  }


}
