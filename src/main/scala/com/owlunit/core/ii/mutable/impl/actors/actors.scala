package com.owlunit.core.ii.mutable.impl

import akka.actor.{Extension, ExtendedActorSystem, ExtensionIdProvider, ExtensionId}
import com.typesafe.config.Config
import java.util.concurrent.TimeUnit
import com.owlunit.core.ii.mutable.Ii
import scala.concurrent.duration.{FiniteDuration, Duration}

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         package objects containing messages
 */

package object actors {

  case class WithId(message: Any)

  // For recommender interface
  case class FindSimilar(query: Map[Ii.IdType, Double], key: String, limit: Int)
  case class Similar(value: List[(Ii.IdType, Double)])

  // For loader and merger
  case class LoadIndirect(id: Ii.IdType, weight: Double, depth: Int)
  case class LoadParents(id: Ii.IdType, weight: Double, key: String)
  case class LoadedMap(id: Ii.IdType, weight: Double, map: Map[Ii.IdType, Double])
  case class Merged(map: Map[Ii.IdType, Double])

  // For loading by group of items
  case class LoadMergedIndirect(map: Map[Ii.IdType, Double], depth: Int)
  case class LoadMergedParents(map: Map[Ii.IdType, Double], key: String)
  case class MergedIndirect(map: Map[Ii.IdType, Double])
  case class MergedParents(map: Map[Ii.IdType, Double])

  // For comparing maps
  case class MapsWithId(id: Ii.IdType, a: Map[Ii.IdType, Double], b: Map[Ii.IdType, Double])
  case class Likeness(id: Ii.IdType, value: Double = 0.0)
  case class Sort(pattern: Map[Ii.IdType, Double], candidates: Map[Ii.IdType, Double])
  case class Sorted(map: Map[Ii.IdType, Double])

  // Settings
  class IiConfig(config: Config) extends Extension {

    val loadersIndirectAmount: Int = config.getInt("ii-core.workers.loaders-indirect")
    val loadersParentsAmount: Int = config.getInt("ii-core.workers.loaders-parents")
    val comparatorsAmount: Int = config.getInt("ii-core.workers.comparators")

    val indirectDepth: Int = config.getInt("ii-core.indirect-depth")

    val timeout: FiniteDuration = Duration(config.getMilliseconds("ii-core.timeout"), TimeUnit.MILLISECONDS)

  }
  object Settings extends ExtensionId[IiConfig] with ExtensionIdProvider {

    override def lookup() = Settings
    override def createExtension(system: ExtendedActorSystem) = new IiConfig(system.settings.config)

  }
}
