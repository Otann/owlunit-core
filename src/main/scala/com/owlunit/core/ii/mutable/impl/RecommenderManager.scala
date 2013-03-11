package com.owlunit.core.ii.mutable.impl

import actors._
import collection.mutable.{Map => MutableMap}
import akka.actor.{Props, ActorSystem}
import com.owlunit.core.ii.mutable.{IiDao, Recommender, Ii}
import org.neo4j.graphdb.{GraphDatabaseService, Direction, Node}
import org.slf4j.LoggerFactory
import akka.util.duration._
import akka.dispatch.{Promise, ExecutionContext, Future, Await}
import akka.pattern.ask
import akka.util.Timeout
import akka.util
import akka.routing.RoundRobinRouter
import collection.mutable
import java.util.concurrent.Executors
import com.weiglewilczek.slf4s.Logging


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */


class RecommenderManager(dao: IiDao, name: String) extends Recommender with Logging {

  implicit val system = ActorSystem(name)

  def comparatorsAmount = 20
  val comparatorTimeout = 2.seconds
  val comparator = system.actorOf(Props[Comparator].withRouter(RoundRobinRouter(nrOfInstances = comparatorsAmount)))

  def loadersAmount = 20
  val loaderTimeout = 2.seconds
  val loader = system.actorOf(Props(new Loader(dao)).withRouter(RoundRobinRouter(nrOfInstances = loadersAmount)))

  def indirectDepth = 2

  def init(){}

  def shutdown() {
    system.shutdown()
  }

  implicit val timeout = Timeout(5.seconds)

  def recommend(query: Map[Ii, Double], key: String, limit: Int): List[(Ii, Double)] = {

    logger.debug("recommending for query %s" format query)

    // Check sum of weights
    val queryTotalWeight = query.foldLeft(0.0)(_ + _._2)
    if (queryTotalWeight == 0)
      return List()

    // load indirect nodes for query
    val indirectFutures = for ((ii, queueWeight) <- query) yield {
      // load indirect components to future
      val future = (loader ? LoadIndirect(ii.id, indirectDepth))

      // add original weight to result
      future.map( (_, queueWeight) )
    }

    // combine indirect components to single map
    val indirectFuture = Future.sequence(indirectFutures) map { list =>
      val indirect = WeightMap()
      for ((results, queryWeight) <- list)
        for ((id, weight) <- results)
          indirect(id) = (queryWeight / queryTotalWeight) * weight
      indirect
    }

    // load parents of indirect as candidates
    val candidatesFuture = indirectFuture map { indirect =>


      val parentsFutures = for ((id, indirectWeight) <- sortedIndirect) yield {
        // load parents to future
        val future = (loader ? LoadParents(id, key)).mapTo[Map[Long, Double]]

        // add indirect weight to result
        future.map( (_, indirectWeight) )
      }

      val indirectTotalWeight = indirect.foldLeft(0.0)(_ + _._2)
      val parentsFuture = Future.sequence(parentsFutures) map { list =>
        val components = WeightMap()
        for ((results, queryWeight) <- list)
          for ((id, weight) <- results)
      }
    }

//    val indirectFutures = for ((ii, weight) <- sortedQuery) yield {
//      (loader ? LoadIndirect(ii.id, indirectDepth)) onSuccess { case components: Map[Long, Double] =>
//
//        // when loaded, append to indirect with share multiplicator
//        for ((component, componentWeight) <- components) {
//          logger.debug("appending indirect %s" format ii.id)
//          indirect(component) += (weight / queryTotalWeight) * componentWeight
//        }
//      }
//    }
    Await.result(Future.sequence(indirectFutures), loaderTimeout)
    logger.debug("awaiting indirect load complete, %s" format indirect)

    // load candidates for indirect
    val indirectWeight = indirect.foldLeft(0.0)(_ + _._2)
    if (indirectWeight == 0)
      return List()
    val sortedIndirect = indirect.toList.sortBy(_._2)

    // get list of (future, weight)
    val candidatesFutures = for ((id, weight) <- sortedIndirect) yield {
      val future = (loader ? LoadParents(id)).mapTo[Map[Long, Double]]
      future.map( (_, weight) )
    }

    // fold results
    val candidatesFuture = Future.sequence(candidatesFutures) map { list =>
      val candidates = WeightMap()
      for ((results, queryWeight) <- list)
        for ((id, weight) <- results)
          candidatesFuture(id) = queryWeight / queryTotalWeight * weight
      candidates
    }


    Await.result(Future.sequence(candidatesFutures), loaderTimeout)
    logger.debug("awaiting candidate load complete, %s" format candidates)


    val sortedCandidates = candidates.toList.sortBy(_._2)
    val results = WeightMap()

    val resultFutures: List[Future[Double]] = for ((candidate, _) <- sortedCandidates) yield {
      logger.debug("trying to compare %s with %s" format (candidate, indirect))
      for {
        sample <- (loader ? LoadIndirect(candidate, indirectDepth)).mapTo[Map[Long, Double]]
        result <- (comparator ? Maps(indirect, sample)).mapTo[Double]
      } yield {
        logger.debug("comparation done for id %s - %s" format (candidate, result))
        results(candidate) += result
        result
      }
    }

    // val oddSum = Await.result(futureList.map(_.sum), 1 second).asInstanceOf[Int]
    Await.result(Future.sequence(resultFutures), comparatorTimeout)
    logger.debug("awaiting comparation complete, %s" format results)

    results.map{case (id, weight) => (dao.load(id), weight)}.toList.sortBy(_._2)

  }

  override def compare(a: Ii, b: Ii) = {
    0
  }

  override def getSimilar(a: Ii, key: String, limit: Int) = {
    List()
  }

}
