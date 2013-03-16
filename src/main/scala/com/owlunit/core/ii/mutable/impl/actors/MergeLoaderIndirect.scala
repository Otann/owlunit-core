package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import com.owlunit.core.ii.mutable.IiDao
import akka.routing.RoundRobinRouter


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         MapReduce like loader and merger of Ii's parents
 *         Returns results to parent
 */
class MergeLoaderIndirect(dao: IiDao) extends MergeLoader(dao) {

  def loadersAmount: Int = settings.loadersIndirectAmount

  protected def receive = {

    // Map
    case LoadMergedIndirect(query, depth) => {
      totalWeight = query.foldLeft(0.0)(_ + _._2)
      countdown = query.size

      // create merger
      merger = context.actorOf(Props(new MapsMerger(totalWeight, countdown)))

      // feed workers with work, replace sender with merger
      for ((id, weight) <- query) workers.tell(LoadIndirect(id, weight, depth), merger)
    }

    // Reduce
    case msg: Merged => context.parent ! msg

  }
}