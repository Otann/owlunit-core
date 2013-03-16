package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{ExtendedActorSystem, ExtensionIdProvider, ExtensionId, Extension}
import com.typesafe.config.Config
import java.util.concurrent.TimeUnit
import akka.util.Duration

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
class IiConfig(config: Config) extends Extension {

  val loadersIndirectAmount: Int = config.getInt("ii-core.loaders.indirect")
  val loadersParentsAmount: Int = config.getInt("ii-core.loaders.parents")

  val indirectDepth: Int = config.getInt("ii-core.indirect-depth")

  val timeout: Duration = Duration(config.getMilliseconds("ii-core.timeout"), TimeUnit.MILLISECONDS)

}