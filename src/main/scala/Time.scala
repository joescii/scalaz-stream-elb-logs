package com.joescii.scalaz.stream

import java.text.{DateFormat, SimpleDateFormat}
import java.util.{Calendar, TimeZone}

object Time {
  val utc = TimeZone.getTimeZone("UTC")

  def dateFormat(pattern: String): DateFormat =
    new SimpleDateFormat(pattern) !! (_.setTimeZone(utc))

  def logDateFormat = Time.dateFormat("yyyy-MM-dd'T'HH:mm:ss")

  def roundTime(c: Calendar, bucketSize: Int): Calendar = {
    val second = c.get(Calendar.SECOND) / bucketSize * bucketSize
    c.clone().asInstanceOf[Calendar]
      .!!(_.set(Calendar.SECOND, second))
      .!!(_.set(Calendar.MILLISECOND, 0))
  }
}
