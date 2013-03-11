package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{ActorRef, Actor}
import com.owlunit.core.ii.mutable.IiDao
import collection.mutable.{Map => MutableMap}

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
class Recommender(dao: IiDao) extends Actor {

// Limits can be used if recommendations comes in priority order
//  val limits = MutableMap[ActorRef, Int]()

  protected def receive = {

    case FindSimilar(query, key, limit) => {

      val queryTotalWeight = query.foldLeft(0.0)(_ + _._2)
      if (queryTotalWeight == 0)
        sender ! List()
      else {

      }

    }



  }

}
