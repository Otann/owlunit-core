package com.owlunit.core.ii.mutable.impl

import com.owlunit.core.ii.mutable.{IiDao, Ii}
import com.tinkerpop.blueprints.{Edge, Vertex, Direction}
import com.owlunit.core.ii.mutable.Ii._
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.scala._


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

private[mutable] class BlueprintIiDao(val graph: IiGraph) extends IiDao {

  val depthDefaultQuantifier = 1

  // Implementations

  def ensureIndex(key: String) {
    val klass = classOf[Vertex]
    val indices = graph.getIndexedKeys(klass)
    if (!indices.contains(key)) {
      graph.createKeyIndex(key, klass)
    }
  }

  def ensureIndices(keys: String*) {
    val klass = classOf[Vertex]
    val indices = graph.getIndexedKeys(klass)
    for (key <- keys if !indices.contains(key)) {
      graph.createKeyIndex(key, klass)
    }
  }

  def create = new BlueprintIi(graph)

  def load(id: String): Option[Ii] = {
    try {
      graph.getVertex(id) match {
        case null => None
        case some => Some(new BlueprintIi(some, graph))
      }
    } catch {
      case ex: Throwable => None
    }
  }

  def load(key: String, value: String) =
    graph.getVertices(key, value).map(new BlueprintIi(_, graph)).toSeq

  def indirectComponents(id: String, depth: Int) = {
    val start = graph.getVertex(id)
    val result = indirectVertices(1, start)

    for {
      exactDepth   <- 2 to depth
      (id, weight) <- indirectVertices(exactDepth, start)
    } {
      result(id) = result.getOrElse(id, 0.0) + weight
    }

    result.toMap
  }

  def indirectVertices(exactDepth: Int, start: Vertex) = {
    val result = collection.mutable.Map.empty[String, Double]

    var pipeline = start.outE().inV()
    for (i <- 2 to exactDepth) pipeline = pipeline.outE().inV()

    for {
      pipe <- pipeline.path.toList
    } {
      // Collector for weight of last element in pipe
      var weight = 0.0

      // With each level, weight is divided by 2^depth
      var depthQuantifier = depthDefaultQuantifier

      for (element <- pipe) {
        element match {

          // For each edge, calculate proportional weight and add to total
          case edge: Edge => {
            depthQuantifier <<= 1
            val edgeWeight = edge.getProperty(WeightPropertyKey).toString.toDouble
            weight += edgeWeight / depthQuantifier
          }

          // Ignore other elements of path
          case _ =>
        }
      }

      val lastId = pipe.last.asInstanceOf[Vertex].getId.toString
      result(lastId) = result.getOrElse(lastId, 0.0) + weight
    }

    result
  }

  def within(id: String, key: String) = {
    graph.getVertex(id).outE().toList.map {
      edge => (edge.getVertex(Direction.OUT), edge.getProperty(WeightPropertyKey).toString.toDouble)
    }.map{
      case (n, w) => n.getId.toString -> w
    }.toMap
  }


}