package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{Props, ActorRef, Actor}
import com.owlunit.core.ii.mutable.IiDao
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.ask


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         Returns List[Long, Double] to parent
 */
class RecommenderActor(dao: IiDao) extends Actor {

  val depth = 3
  val loadersAmount = 10

  val indirectLoader = context.actorOf(Props(new IndirectMerger(dao, depth)))

  implicit val timeout = Timeout(5.seconds)


  protected def receive = {

    case FindSimilar(query, key, limit) => {

      // initiate load and start waiting reply
      val indirectFuture = indirectLoader ? MergeIndirect(query)

      sender ! Similar(List())
    }




  }

}
