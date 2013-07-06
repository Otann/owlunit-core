package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import com.owlunit.core.ii.mutable.{Ii, IiDao}


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         Operated by FindSimilar message
 *         Returns Similar to parent
 */
class SimilarFinder(dao: IiDao) extends Actor with ActorLogging {

  val settings = Settings(context.system)

  val indirectLoader = context.actorOf(Props(new MergeLoaderIndirect(dao)))
  val parentsLoader = context.actorOf(Props(new MergeLoaderParents(dao)))
  val sorter = context.actorOf(Props(new Sorter(dao)))

  var origin: ActorRef = null
  var message = FindSimilar(Map(), "", 0)
  var savedIndirect: Map[Ii.IdType, Double] = Map()

  def receive = {

    case msg @ FindSimilar(query, _, _) => {
      origin = sender
      message = msg
      indirectLoader ! LoadMergedIndirect(query, settings.indirectDepth)
    }

    case MergedIndirect(map) => {
      savedIndirect = map
      parentsLoader ! LoadMergedParents(map, message.key)
    }

    case MergedParents(map) => sorter ! Sort(savedIndirect, map -- message.query.keys)

    case Similar(list) => origin ! Similar(list)

  }

}
