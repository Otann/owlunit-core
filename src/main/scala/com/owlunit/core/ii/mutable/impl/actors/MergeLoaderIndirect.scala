package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import com.owlunit.core.ii.mutable.IiDao
import akka.routing.RoundRobinRouter


/**
 * Operated by LoadMergedIndirect message
 * Initiates merger actor, which combines results from workers
 * and reports back
 * Returns results to parent as MergedIndirect
 *
 * @author Anton Chebotaev
 */
class MergeLoaderIndirect(dao: IiDao) extends Actor {

  val settings = Settings(context.system)

  val workers = context.actorOf(Props(new Loader(dao))
    .withRouter(RoundRobinRouter(nrOfInstances = settings.loadersIndirectAmount)))

  def receive = {

    case LoadMergedIndirect(query, depth) => {

      val totalWeight = query.foldLeft(0.0)(_ + _._2)
      val countdown = query.size

      // create merger
      val merger = context.actorOf(Props(new MapsMerger(totalWeight, countdown)))

      // feed workers with work, replace sender with merger
      for ((id, weight) <- query) workers.tell(LoadIndirect(id, weight, depth), merger)
    }

    case Merged(map) => context.parent ! MergedIndirect(map)

  }
}