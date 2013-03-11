package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.Actor
import com.owlunit.core.ii.mutable.IiDao

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         Loads parents or indirect components, returns Map[Long, Double]
 */
class Loader(dao: IiDao) extends Actor {

  protected def receive = {

    case LoadIndirect(id, depth) =>
      sender ! dao.indirectComponents(id, depth)

    case Message(origin, LoadIndirect(id, depth)) =>
      sender ! Message(origin, dao.indirectComponents(id, depth))

    case LoadParents(id, key) =>
      sender ! dao.within(id, key) // TODO: write tests

    case Message(origin, LoadParents(id, key)) =>
      sender ! Message(origin, dao.within(id, key))

  }

}
