package com.owlunit.core.ii.mutable

import impl.{RecommenderManager, NeoIiDao, NeoRecommender}
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.rest.graphdb.RestGraphDatabase

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait IiDao {

  def init()
  def shutdown()

  def create: Ii

  def load(id: Long): Ii
  def load(key: String, value: String): Seq[Ii]
  def search(key: String, queue: String): Seq[Ii]

  private[ii] def indirectComponents(item: Long, depth: Int): Map[Long, Double]
  private[ii] def within(item: Long, key: String): Map[Long, Double] // TODO: write tests for key

}

trait RecoDao extends IiDao with Recommender

private class NeoDaoImpl(graph: GraphDatabaseService)
  extends NeoIiDao(graph) with RecoDao with NeoRecommender

private class ActorDaoImpl(graph: GraphDatabaseService, name: String)
  extends NeoIiDao(graph) with RecoDao with Recommender {
  val recommender = new RecommenderManager(this, name)
  def compare(a: Ii, b: Ii) = recommender.compare(a, b)
  def getSimilar(a: Ii, key: String, limit: Int) = recommender.getSimilar(a, key, limit)
  def recommend(p: Map[Ii, Double], key: String, limit: Int) = recommender.recommend(p, key, limit)
}

object IiDao {

  def apply(graph: GraphDatabaseService, name: String): RecoDao = {
    val result: RecoDao = new NeoDaoImpl(graph)
//    val result: RecoDao = new ActorDaoImpl(graph, name)
    result
  }

  def local(path: String): RecoDao = apply(
    new EmbeddedGraphDatabase(path),
    alphanumericCamelCase("IiDaoLocal-%s" format path)
  )

  def remote(url: String, login: String, password: String): RecoDao = apply(
    new RestGraphDatabase(url, login, password),
    alphanumericCamelCase("IiDaoRemote-%s" format url)
  )

  def alphanumericCamelCase(s: String) = s.split("[^a-zA-Z0-9]").map(_.capitalize).mkString("")
}
