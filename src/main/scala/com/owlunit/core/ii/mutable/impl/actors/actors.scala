package com.owlunit.core.ii.mutable.impl

import akka.actor.{ExtendedActorSystem, ExtensionIdProvider, ExtensionId}

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         package objects containing messages
 */

package object actors {

  // For recommender interface
  case class FindSimilar(query: Map[Long, Double], key: String, limit: Int)
  case class Similar(value: List[(Long, Double)])

  // For loader
  case class LoadIndirect(id: Long, weight: Double, depth: Int)
  case class LoadParents(id: Long, weight: Double, key: String)
  case class WeightedMap(map: Map[Long, Double], weight: Double)

  // For loading by group of items
  case class LoadMergedIndirect(map: Map[Long, Double], depth: Int)
  case class LoadMergedParents(map: Map[Long, Double], key: String)
  case class Merged(map: Map[Long, Double])

  // For comparing maps
  case class Sort(pattern: Map[Long, Double], candidates: Map[Long, Double])
  case class Maps(a: Map[Long, Double], b: Map[Long, Double])
  case class Likeness(value: Double = 0.0)

  // Settings
  object Settings extends ExtensionId[IiConfig] with ExtensionIdProvider {

    override def lookup() = Settings
    override def createExtension(system: ExtendedActorSystem) = new IiConfig(system.settings.config)

  }
}
