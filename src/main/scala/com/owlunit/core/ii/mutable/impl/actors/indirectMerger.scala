package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import com.owlunit.core.ii.mutable.IiDao
import akka.routing.RoundRobinRouter
import collection.mutable.{Map => MutableMap}


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         MapReduce like loader and merger of Ii's indirect components and parents
 *         Returns results to parent
 */
class IndirectMerger(dao: IiDao, depth: Int) extends Actor with ActorLogging {

  val loadersAmount = 10 //TODO: replace loadersAmount with file config

  var merger: ActorRef = null
  val workers = context.actorOf(Props(new Loader(dao)).withRouter(RoundRobinRouter(nrOfInstances = loadersAmount)))

  var result = collection.mutable.Map[Long, Double]()
  var totalWeight = 0.0
  var countdown = 0

  protected def receive = {

    // Map
    case MergeIndirect(query) => {
      log.debug("%s received task" format self)
      totalWeight = query.foldLeft(0.0)(_ + _._2)
      countdown = query.size

      // create merger
      merger = context.actorOf(Props(new WeightMerger(totalWeight, countdown)))

      // feed workers with work, replace sender with merger
      for ((id, weight) <- query) {
        val task = LoadIndirect(id, weight, depth)
        workers.tell(task, merger)
        log.debug("sent %s to %s" format (task, workers))
      }

    }

    // Reduce
    case msg: MergedMap => {
      log.debug("got result from merger %s" format msg)
      context.parent ! msg
      context.stop(self) // work is done, kill ourselves
    }

  }

}
