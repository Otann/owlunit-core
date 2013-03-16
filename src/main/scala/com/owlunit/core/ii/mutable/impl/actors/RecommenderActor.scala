package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{Props, ActorRef, Actor}
import com.owlunit.core.ii.mutable.IiDao
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.{ask, pipe}


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         Returns List[(Long, Double)] to parent
 */
class RecommenderActor(dao: IiDao) extends Actor {

  val settings = Settings(context.system)
  implicit val timeout = Timeout(settings.timeout)

  val indirectLoader = context.actorOf(Props(new MergeLoaderIndirect(dao)))
  val parentsLoader = context.actorOf(Props(new MergeLoaderParents(dao)))

  protected def receive = {

    case FindSimilar(query, key, limit) => {

      // create merger

      // load indirect as future
      val indirectResult = (indirectLoader ? LoadMergedIndirect(query, settings.indirectDepth)).mapTo[Merged]
      val indirect = indirectResult.map(x => LoadMergedParents(x.map, key))

      // convert and pipe to parentsLoader
      val parentsFuture = (indirect pipeTo parentsLoader).mapTo[Merged]

      //TODO: pipe message to Sorter, receive Future[List[]]

      //TODO: wait for future and return

      sender ! Similar(List())
    }

//    case Merged(map) => {
//      if (sender == indirectLoader)
//
//
//    }




  }

}
