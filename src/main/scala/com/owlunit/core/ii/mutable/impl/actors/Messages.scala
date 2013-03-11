package com.owlunit.core.ii.mutable.impl.actors

import com.owlunit.core.ii.mutable.Ii
import akka.actor.ActorRef

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */

case class Message(initiator: ActorRef, message: Any)

// For comparing maps
case class Maps(a: WeightMap, b: WeightMap)

// For recommender interface
case class FindSimilar(query: Map[Long, Double], key: String, limit: Int)

case class LoadParents(id: Long, key: String)
case class Parents(map: Map[Long, Double])

case class LoadIndirect(id: Long, depth: Int)

sealed trait MergeLoad
case class MergeLoadParents(map: WeightMap) extends MergeLoad
case class MergeLoadIndirect(map: WeightMap, depth: Int) extends MergeLoad

