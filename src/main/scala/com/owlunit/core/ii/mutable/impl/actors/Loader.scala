package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{ActorLogging, ActorRef, Actor}
import com.owlunit.core.ii.mutable.IiDao

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         Loads parents or indirect components, returns Map[Long, Double] to sender
 */
class Loader(dao: IiDao) extends Actor with ActorLogging {

  protected def receive = {

    case LoadIndirect(id, weight, depth) =>
      sender ! LoadedMap(id, weight, dao.indirectComponents(id, depth))

    case LoadParents(id, weight, key) =>
      sender ! LoadedMap(id, weight, dao.within(id, key))

  }

}
