package com.owlunit.core.ii.mutable

import impl.{AkkaRecommender, BlueprintIiDao}
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.rest.graphdb.RestGraphDatabase

/**
 * Data access object provides access to Ii storage
 *
 * @author Anton Chebotaev
 */
trait IiDao {
  import Ii._

  def create: Ii

  def load(id: IdType): Option[Ii]
  def load(key: String, value: String): Seq[Ii]
  def search(key: String, queue: String): Seq[Ii]

  private[ii] def indirectComponents(item: IdType, depth: Int): Map[IdType, Double]
  private[ii] def within(item: IdType, key: String): Map[IdType, Double] // TODO: write tests for key

}