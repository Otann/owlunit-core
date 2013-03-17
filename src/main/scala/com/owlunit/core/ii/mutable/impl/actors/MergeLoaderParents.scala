package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import com.owlunit.core.ii.mutable.IiDao
import akka.routing.RoundRobinRouter


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         Operated by LoadMergedParents message
 *         Initiates merger actor, which combines results from workers
 *         and reports back
 *         Returns results to parent as MergedParents
 */
class MergeLoaderParents(dao: IiDao) extends Actor with ActorLogging {

  val settings = Settings(context.system)

  val workers = context.actorOf(Props(new Loader(dao))
    .withRouter(RoundRobinRouter(nrOfInstances = settings.loadersParentsAmount)))

  protected def receive = {

    case LoadMergedParents(query, key) => {

      val totalWeight = query.foldLeft(0.0)(_ + _._2)
      val countdown = query.size

      // create merger
      val merger = context.actorOf(Props(new MapsMerger(totalWeight, countdown)))

      // feed workers with work, replace sender with merger
      for ((id, weight) <- query) workers.tell(LoadParents(id, weight, key), merger)
    }

    case Merged(map) => context.parent ! MergedParents(map)

  }
}