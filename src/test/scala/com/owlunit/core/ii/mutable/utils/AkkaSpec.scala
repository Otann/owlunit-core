package com.owlunit.core.ii.mutable.utils

import akka.testkit.{TestKit, ImplicitSender}
import org.specs2.mutable.{Before, After}
import akka.actor.{Scope, ActorSystem}
import com.owlunit.core.ii.mutable.{Ii, IiService}
import com.thinkaurelius.titan.core.TitanFactory
import org.specs2.time.NoTimeConversions

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         A tiny class that can be used as a Specs2 'system'.
 */
abstract class AkkaSpec(_system: ActorSystem)
  extends TestKit(_system)
  with After with Before
  with ImplicitSender with IiHelpers {

  var dao: IiService = null
  var graph: Ii.IiGraph = null

  def before {
    val dbPath = "/tmp/titan_" + randomString
    graph = TitanFactory.open(dbPath)
    dao = IiService(graph, system)
  }

  def after {
    graph.shutdown()
    system.shutdown()
  }

}