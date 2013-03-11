package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{ActorRef, Props, Actor}
import com.owlunit.core.ii.mutable.IiDao
import akka.event.Logging
import akka.routing.RoundRobinRouter
import collection.mutable.{Map => MutableMap}


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
class ParentsMerger(dao: IiDao, loadersAmount: Int) extends Actor {

  val loader = context.actorOf(Props(new Loader(dao)).withRouter(RoundRobinRouter(nrOfInstances = loadersAmount)))

  val countdowns = MutableMap[ActorRef, Int]()

  protected def receive = {

    case map: Map[Long, Double] => {

      countdowns(sender) = map.size

    }

  }


}
