package com.owlunit.core.ii.mutable

import impl.{AkkaRecommender, NeoIiDao}
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.rest.graphdb.RestGraphDatabase

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait IiDao {

  def init(){}
  def shutdown(){}

  def create: Ii

  def load(id: Long): Ii
  def load(key: String, value: String): Seq[Ii]
  def search(key: String, queue: String): Seq[Ii]

  private[ii] def indirectComponents(item: Long, depth: Int): Map[Long, Double]
  private[ii] def within(item: Long, key: String): Map[Long, Double] // TODO: write tests for key

}