package com.joescii.scalaz

import java.util.{Calendar, Date, GregorianCalendar}

package object stream {
  /**
    * For chaining mutating methods like setters when dealing with Java APIs
    *
    * @param a: Something with a mutating method
    * @tparam A: Anything really
    */
  implicit class MutationChainer[A](val a: A) extends AnyVal {
    /**
      * Performs the function (assumed to be side-effecting) and returns the object it was invoked on.
      *
      * @param f any function which accepts the wrapped object
      * @return the object that was passed in
      */
    def withMutation(f: A => Unit): A = {
      f(a)
      a
    }

    def !!(f: A => Unit): A = withMutation(f)
  }

  implicit def toCal(d: Date): Calendar = new GregorianCalendar(Time.utc) !! (_ setTime d)
}
