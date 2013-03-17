package com.owlunit.core.ii.mutable

import impl.{AkkaRecommender, NeoIiDao}
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.rest.graphdb.RestGraphDatabase

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
trait IiService extends IiDao with IiRecommender

object IiService {

  class RecommenderDao(graph: GraphDatabaseService, val name: String)
    extends NeoIiDao(graph) with AkkaRecommender with IiService

  def apply(graph: GraphDatabaseService, name: String): IiService = new RecommenderDao(graph, name)

  def local(path: String): IiService = apply(
    new EmbeddedGraphDatabase(path),
    alphanumericCamelCase("IiDaoLocal-%s" format path)
  )

  def remote(url: String, login: String, password: String): IiService = apply(
    new RestGraphDatabase(url, login, password),
    alphanumericCamelCase("IiDaoRemote-%s" format url)
  )

  def alphanumericCamelCase(s: String) = s.split("[^a-zA-Z0-9]").map(_.capitalize).mkString("")

}
