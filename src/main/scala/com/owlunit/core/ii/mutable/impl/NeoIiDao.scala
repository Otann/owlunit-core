package com.owlunit.core.ii.mutable.impl

import com.owlunit.core.ii.mutable.{Ii, IiDao}
import com.owlunit.core.ii.NotFoundException
import collection.mutable.ListBuffer
import sys.ShutdownHookThread
import org.neo4j.graphdb.{Direction, GraphDatabaseService}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

private[mutable] class NeoIiDao(val graph: GraphDatabaseService) extends IiDao with Helpers with NeoRecommender {

  private def fulltextIndex = graph.index().forNodes(FulltextIndexName, FulltextIndexParams)
  private def exactIndex = graph.index().forNodes(ExactIndexName, ExactIndexParams)

  def init() { ShutdownHookThread { shutdown() } }
  def shutdown() { graph.shutdown() }

  def create = new NeoIi(graph)

  def load(id: Long) = {
    try {
      new NeoIi(graph.getNodeById(id), graph)
    } catch {
      case ex: org.neo4j.graphdb.NotFoundException => throw new NotFoundException(id, ex)
    }
  }

  def load(key: String, value: String) = {
    val result = ListBuffer[Ii]()

    val exactIterator = exactIndex.get(key, value).iterator
    while (exactIterator.hasNext) {
      val node = exactIterator.next()
      result += new NeoIi(node, graph)
    }

    val fulltextIterator = fulltextIndex.get(key, value).iterator
    while (fulltextIterator.hasNext) {
      val node = fulltextIterator.next()
      result += new NeoIi(node, graph)
    }

    result.toList
  }

  def search(key: String, queue: String) = {
    val result = ListBuffer[Ii]()
    val iterator = fulltextIndex.query(key, queue).iterator()
    while (iterator.hasNext)
      result += new NeoIi(iterator.next(), graph)

    result.toList
  }

  def indirectComponents(item: Ii, depth: Int) = item.node match {
    case None => Map()
    case Some(aNode) => getIndirectNodes(aNode, depth).map{case (n, w) => new NeoIi(n, graph) -> w}
  }

  def within(item: Ii) = item.node match {
    case None => Map()
    case Some(aNode) => getNodes(aNode, Direction.INCOMING, 1).map{case (n, w) => new NeoIi(n, graph) -> w}
  }

}