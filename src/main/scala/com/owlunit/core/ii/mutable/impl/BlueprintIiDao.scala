package com.owlunit.core.ii.mutable.impl

import com.owlunit.core.ii.mutable.{IiDao, Ii}
import collection.mutable.ListBuffer
import sys.ShutdownHookThread
import com.tinkerpop.blueprints.Direction
import com.owlunit.core.ii.mutable.Ii._
import scala.Some

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

private[mutable] class BlueprintIiDao(val graph: IiGraph) extends IiDao {

//  private def fulltextIndex = graph.index().forNodes(FulltextIndexName, FulltextIndexParams)
//  private def exactIndex = graph.index().forNodes(ExactIndexName, ExactIndexParams)

  // Implementations

  def create = new BlueprintIi(graph)

  def load(id: IdType): Option[Ii] = {
    try {
      graph.getVertex(id) match {
        case null => None
        case some => Some(new BlueprintIi(some, graph))
      }
    } catch {
      case ex: Throwable => None
    }
  }

  def load(key: String, value: String) = {
    val result = ListBuffer[Ii]()

    //TODO: implement indices
//    val exactIterator = exactIndex.get(key, value).iterator
//    while (exactIterator.hasNext) {
//      val node = exactIterator.next()
//      result += new BlueprintIi(node, graph)
//    }
//
//    val fulltextIterator = fulltextIndex.get(key, value).iterator
//    while (fulltextIterator.hasNext) {
//      val node = fulltextIterator.next()
//      result += new BlueprintIi(node, graph)
//    }

    result.toList
  }

  def search(key: String, queue: String) = {
    val result = ListBuffer[Ii]()

    //TODO: implement indices
//    val iterator = fulltextIndex.query(key, queue).iterator()
//    while (iterator.hasNext)
//      result += new BlueprintIi(iterator.next(), graph)

    result.toList
  }

  def indirectComponents(id: IdType, depth: Int) =
    getIndirectNodes(graph.getVertex(id), depth)
      .map{ case (n, w) => n.getId.toString -> w }

  //TODO: rewrite condition with Gremlin
  def within(item: IdType, key: String) =
    getNodes(graph.getVertex(item), Direction.IN, 1)
      .map{ case (n, w) if n.getPropertyKeys.contains(key) => n.getId.toString -> w }


}