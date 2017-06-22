package com.yidian.crawler4s.sprocessor.utils

import scala.util.{Failure, Success, Try}

object LangUtils {

  /** one implementation for 'Loan' pattern, please note that there should be
    * no lazy computation in doWork function
    *
    * @param resource
    * @param cleanup
    * @param doWork
    * @tparam A
    * @tparam B
    * @return
    */
  def cleanly[A, B](resource: A)(cleanup: A => Unit)(doWork: A => B): Try[B] = {
    try {
      Success(doWork(resource))
    } catch {
      case e: Exception => Failure(e)
    }
    finally {
      try {
        if (resource != null) {
          cleanup(resource)
        }
      } catch {
        case e: Exception => println(e) // should be logged
      }
    }
  }

  /**
    * one implementation for 'Loan' pattern, please note that there should be
    * no lazy computation in doWork function
    *
    * @param resource
    * @param cleanup
    * @param doWork
    * @tparam A
    * @tparam B
    * @return
    */
  def cleanly2[A, B](resource: A)(cleanup: A => Unit)(doWork: A => B): B = {
    try {
      doWork(resource)
    }
    finally {
      try {
        if (resource != null) {
          cleanup(resource)
        }
      } catch {
        case e: Exception => println(e) // should be logged
      }
    }
  }


}
