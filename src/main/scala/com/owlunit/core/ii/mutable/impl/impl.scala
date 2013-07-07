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

  private[impl] val WeightPropertyKey = "WEIGHT"

  //TODO: refactor to return multiple connections
  private[impl] def getEdge(a: Vertex, b: Vertex): Option[Edge] = {

    val aIterator = a.getEdges(Direction.OUT).iterator()
    val bIterator = b.getEdges(Direction.OUT).iterator()
    val result = new ArrayBuffer[Edge]

    while (aIterator.hasNext) {
      val edge = aIterator.next()
      if (edge.getVertex(Direction.IN) == b)
        return Some(edge)
    }

    while (bIterator.hasNext) {
      val edge = bIterator.next()
      if (edge.getVertex(Direction.IN) == a)
        return Some(edge)
    }

    None
  }

  implicit def iiToIiImpl(item: Ii): BlueprintIi = {
    item match {
      case ii: BlueprintIi => ii
      case _ => throw new IllegalArgumentException("Can't operate with this implementation %s" format item)
    }
  }

}
