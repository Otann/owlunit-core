package com.owlunit.core.ii.mutable.impl

import com.owlunit.core.ii.mutable.{IiDao, Ii}
import com.owlunit.core.ii.NotFoundException
import collection.mutable.ListBuffer
import sys.ShutdownHookThread
import org.neo4j.graphdb.{Direction, GraphDatabaseService}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

private[mutable] class NeoIiDao(val graph: GraphDatabaseService) extends IiDao {

  private def fulltextIndex = graph.index().forNodes(FulltextIndexName, FulltextIndexParams)
  private def exactIndex = graph.index().forNodes(ExactIndexName, ExactIndexParams)

  override def init() {
    ShutdownHookThread { shutdown() }
    super.init()
  }
  override def shutdown() {
    graph.shutdown()
    super.shutdown()
  }

  // Implementations

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

  def indirectComponents(item: Long, depth: Int) =
    getIndirectNodes(graph.getNodeById(item), depth).map{ case (n, w) => n.getId -> w }

  def within(item: Long, key: String) =
    getNodes(graph.getNodeById(item), Direction.INCOMING, 1).map{ case (n, w) if n.hasProperty(key) => n.getId -> w }


}