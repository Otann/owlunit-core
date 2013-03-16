package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import com.owlunit.core.ii.mutable.IiDao
import akka.routing.RoundRobinRouter


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         MapReduce like loader and merger of Ii's indirect components or parents
 *         Returns results to parent
 */
abstract class MergeLoader(dao: IiDao) extends Actor with ActorLogging {

  val settings = Settings(context.system)
  def loadersAmount: Int

  var merger: ActorRef = null //TODO
  val workers = context.actorOf(Props(new Loader(dao)).withRouter(RoundRobinRouter(nrOfInstances = loadersAmount)))

  var result = collection.mutable.Map[Long, Double]()
  var totalWeight = 0.0
  var countdown = 0

}