package com.owlunit.core.ii.mutable.impl.actors

import com.owlunit.core.ii.mutable.Ii
import akka.actor.ActorRef

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */

// For recommender interface
case class FindSimilar(query: Map[Long, Double], key: String, limit: Int)
case class Similar(value: List[(Long, Double)])

// For comparing maps
case class Maps(a: Map[Long, Double], b: Map[Long, Double])
case class MapsLikeness(value: Double = 0.0)

// For loader
case class LoadIndirect(id: Long, weight: Double, depth: Int)
case class LoadParents(id: Long, weight: Double, key: String)
case class WeightedMap(map: Map[Long, Double], weight: Double)

// For loading parents of group of items
case class LoadMerged(map: Map[Long, Double])
case class MergedMap(map: Map[Long, Double])

////////////////////////////////////////////////////////////////////


case class MergeParents(map: Map[Long, Double])

// Results of operations
case class Indirect(map: Map[Long, Double])
case class Parents(map: Map[Long, Double], weight: Double)


