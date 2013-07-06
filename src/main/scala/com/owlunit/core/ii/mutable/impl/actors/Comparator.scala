package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.Actor
import com.owlunit.core.ii.mutable.Ii

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         Compares maps, returns double to sender
 */
class Comparator extends Actor {

  def receive = {
    case MapsWithId(id, a, b) => sender ! Likeness(id, compareMaps(a, b))
  }

  def compareMaps(a: Map[Ii.IdType, Double], b: Map[Ii.IdType, Double]): Double = {

    val union = Set.empty[Ii.IdType] ++ a.keys ++ b.keys

    if (union.size == 0)
      return 0.0

    val aOverall = a.values.foldLeft(0.0)(_ + _)
    val bOverall = b.values.foldLeft(0.0)(_ + _)

    var min = 0.0

    for (item <- union) {
      val aWeight = a.getOrElse(item, 0.0) / aOverall
      val bWeight = b.getOrElse(item, 0.0) / bOverall

      min += aWeight min bWeight
    }

    min * 100
  }

}
