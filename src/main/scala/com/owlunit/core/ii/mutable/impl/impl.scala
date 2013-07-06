package com.owlunit.core.ii.mutable

import com.tinkerpop.blueprints._
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.java.GremlinPipeline
import com.tinkerpop.gremlin.scala._
import scala.Some
import scala.Some

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
package object impl {

//  private[impl] object RelType extends RelationshipType {
//    def name() = "CONNECTED"
//  }
//
  private[impl] val EdgeTitle = "CONNECTED"


  private[impl] val FulltextIndexName = "FULLTEXT_ITEMS_INDEX"
//  private[impl] val FulltextIndexParams = MapUtil.stringMap(
//    IndexManager.PROVIDER, "lucene",
//    "type", "fulltext",
//    "to_lower_case", "true"
//  )
//
  private[impl] val ExactIndexName = "EXACT_ITEMS_INDEX"
//  private[impl] val ExactIndexParams = MapUtil.stringMap(
//    "type", "exact",
//    "to_lower_case", "true"
//  )

  private[impl] val WeightPropertyName = "WEIGHT"

  private[impl] def getNodes(start: Vertex, direction: Direction, depth: Int): Map[Vertex, Double] = {

    if (depth == 1) {

      val edges = start.getEdges(direction).toList
      edges.map(edge =>
        (edge.getVertex(direction), edge.getProperty(WeightPropertyName).toString.toDouble)
      ).toMap

    } else {

      val pipe = new GremlinPipeline[Vertex, Vertex]()
      pipe.start(start).out().path().toList

      var pipe2 = start.out
      for(i <- 1 to depth - 1){ pipe2 = pipe2.out }

      // TODO: how the hell to get path??
//      for(path <- pipe.path.toList) {
//        val t: List[_] = path
//      }

      Map.empty[Vertex, Double]

//
//      val traverserIterator = Traversal.description()
//        .breadthFirst()
//        .relationships(RelType, direction)
//        .uniqueness(Uniqueness.NODE_PATH)
//        .evaluator(Evaluators.excludeStartPosition())
//        .evaluator(Evaluators.toDepth(depth))
//        .traverse(start)
//        .iterator()
//
//      while (traverserIterator.hasNext) {
//        val path = traverserIterator.next()
//
//        var weight = 0.0
//        var qualifier = 1
//
//        val relIterator = path.relationships().iterator()
//        while (relIterator.hasNext) {
//          val rel = relIterator.next()
//          val w = rel.getProperty(WeightPropertyName).asInstanceOf[Double]
//          weight += w / qualifier
//          qualifier <<= 1
//        }
//
//        val node = path.endNode()
//        nodes get node match {
//          case Some(x) => nodes(node) = x + weight
//          case None => nodes(node) = weight
//        }
//
//      }
    }
  }

  private[impl] def getIndirectNodes(vertex: Vertex, depth: Int): Map[Vertex,  Double] = getNodes(vertex, Direction.OUT, depth)

  //TODO: refactor to return multiple connections
  private[impl] def getEdge(a: Vertex, b: Vertex): Option[Edge] = {

    val aIterator = a.getEdges(Direction.OUT).iterator()
    val bIterator = b.getEdges(Direction.OUT).iterator()
    val result = new ArrayBuffer[Edge]

    while (aIterator.hasNext) {
      val edge = aIterator.next()
      if (edge.getVertex(Direction.OUT) == b)
        return Some(edge)
    }

    while (bIterator.hasNext) {
      val edge = bIterator.next()
      if (edge.getVertex(Direction.OUT) == a)
        return Some(edge)
    }

    return None
  }

  implicit def iiToIiImpl(item: Ii): BlueprintIi = {
    item match {
      case ii: BlueprintIi => ii
      case _ => throw new IllegalArgumentException("Can't operate with this implementation %s" format item)
    }
  }

}
