package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{Props, Actor}
import akka.routing.RoundRobinRouter
import com.owlunit.core.ii.mutable.{Ii, IiDao}
import collection.mutable

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
class Sorter(dao: IiDao) extends Actor {

  val settings = Settings(context.system)

  val comparators = context.actorOf(Props[Comparator]
    .withRouter(RoundRobinRouter(nrOfInstances = settings.comparatorsAmount)))

  val loaders = context.actorOf(Props(new Loader(dao))
    .withRouter(RoundRobinRouter(nrOfInstances = settings.loadersIndirectAmount)))

  // State variables
  var countdown = 0
  var result = mutable.ListBuffer[(Ii.IdType, Double)]()

  var savedPattern: Map[Ii.IdType, Double] = null
  var savedCandidates: Map[Ii.IdType, Double] = null
  var candidatesTotal = 0.0

  def receive = {

    case Sort(pattern, candidates) => {

      // preserve some stuff
      countdown = candidates.size
      if (countdown == 0) {
        context.parent ! Similar(List())
      } else {
        savedPattern = pattern
        savedCandidates = candidates
        candidatesTotal = candidates.foldLeft(0.0)(_ + _._2)

        // feed loaders with work
        for ((id, weight) <- candidates) loaders ! LoadIndirect(id, weight, settings.indirectDepth)
      }
    }

      // when we have candidate's indirect, compare
    case LoadedMap(id, weight, map) => {
      comparators ! MapsWithId(id, map, savedPattern)
    }

      // add comparing result to map
    case Likeness(id, value) => {

      result += ((id, savedCandidates(id) / candidatesTotal * value))

      countdown -= 1
      if (countdown == 0) {
        context.parent ! Similar(result.sortBy(_._2).reverse.toList)
      }
    }
  }
}
