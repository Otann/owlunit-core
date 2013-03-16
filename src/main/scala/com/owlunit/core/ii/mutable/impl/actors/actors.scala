package com.owlunit.core.ii.mutable.impl

import akka.actor.{Extension, ExtendedActorSystem, ExtensionIdProvider, ExtensionId}
import com.typesafe.config.Config
import akka.util.Duration
import java.util.concurrent.TimeUnit

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         package objects containing messages
 */

package object actors {

  case class WithId(message: Any)

  // For recommender interface
  case class FindSimilar(query: Map[Long, Double], key: String, limit: Int)
  case class Similar(value: List[(Long, Double)])

  // For loader and merger
  case class LoadIndirect(id: Long, weight: Double, depth: Int)
  case class LoadParents(id: Long, weight: Double, key: String)
  case class LoadedMap(id: Long, weight: Double, map: Map[Long, Double])
  case class Merged(map: Map[Long, Double])

  // For loading by group of items
  case class LoadMergedIndirect(map: Map[Long, Double], depth: Int)
  case class LoadMergedParents(map: Map[Long, Double], key: String)
  case class MergedIndirect(map: Map[Long, Double])
  case class MergedParents(map: Map[Long, Double])

  // For comparing maps
  case class MapsWithId(id: Long, a: Map[Long, Double], b: Map[Long, Double])
  case class Likeness(id: Long, value: Double = 0.0)
  case class Sort(pattern: Map[Long, Double], candidates: Map[Long, Double])
  case class Sorted(map: Map[Long, Double])

  // Settings
  class IiConfig(config: Config) extends Extension {

    val loadersIndirectAmount: Int = config.getInt("ii-core.workers.loaders-indirect")
    val loadersParentsAmount: Int = config.getInt("ii-core.workers.loaders-parents")
    val comparatorsAmount: Int = config.getInt("ii-core.workers.comparators")

    val indirectDepth: Int = config.getInt("ii-core.indirect-depth")

    val timeout: Duration = Duration(config.getMilliseconds("ii-core.timeout"), TimeUnit.MILLISECONDS)

  }
  object Settings extends ExtensionId[IiConfig] with ExtensionIdProvider {

    override def lookup() = Settings
    override def createExtension(system: ExtendedActorSystem) = new IiConfig(system.settings.config)

  }
}
