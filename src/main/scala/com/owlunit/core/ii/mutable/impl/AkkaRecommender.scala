package com.owlunit.core.ii.mutable.impl

import actors._
import actors.FindSimilar
import actors.Likeness
import actors.LoadedMap
import actors.LoadIndirect
import actors.MapsWithId
import actors.Similar
import akka.actor.{Props, ActorSystem}
import com.owlunit.core.ii.mutable.{IiDao, Recommender, Ii}
import com.weiglewilczek.slf4s.Logging
import akka.util.duration._
import akka.dispatch.{Promise, ExecutionContext, Future, Await}
import akka.pattern.ask
import akka.util.{Duration, Timeout}
import java.util.concurrent.TimeoutException


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */


class AkkaRecommender(dao: IiDao, name: String) extends Recommender with Logging {

  implicit val system = ActorSystem(name)
  val config = Settings(system)
  implicit val timeout: Timeout = Timeout(config.timeout)

  def init(){}

  def shutdown() {
    system.shutdown()
  }


  def getSimilar(a: Ii, key: String, limit: Int): List[(Ii, Double)] = recommend(a.items, key, limit)

  def recommend(query: Map[Ii, Double], key: String, limit: Int): List[(Ii, Double)] = {

    logger.debug("recommending for query %s" format query)

    val actor = system.actorOf(Props(new SimilarFinder(dao)))

    val idQuery = query.map{case (ii, w) => (ii.id, w)}.toMap
    val future = actor ? FindSimilar(idQuery, key, limit)

    try {

      val idList = Await.result(future, config.timeout).asInstanceOf[Similar]
      idList.value.map{ case (id, w) => (dao.load(id), w)}

    } catch {

      case e: TimeoutException => {
        logger.error("Similar timed out", e)
        return List()
      }

    } finally {
      system.stop(actor)
    }

  }

  def compare(a: Ii, b: Ii): Double = {

    val depth = config.indirectDepth
    val loader = system.actorOf(Props(new Loader(dao)))
    val comparator = system.actorOf(Props[Comparator])

    val future = for {
      aIndirect <- (loader ? LoadIndirect(a.id, 1, depth)).mapTo[LoadedMap]
      bIndirect <- (loader ? LoadIndirect(a.id, 1, depth)).mapTo[LoadedMap]
      result <- (comparator ? MapsWithId(a.id, aIndirect.map, bIndirect.map)).mapTo[Likeness]
    } yield {
      result
    }

    try {

      Await.result(future, config.timeout).value

    } catch {

      case e: TimeoutException => {
        logger.error("Comparation timed out", e)
        return 0
      }

    } finally {
      system.stop(loader)
      system.stop(comparator)
    }
  }

}
