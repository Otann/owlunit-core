package com.owlunit.core.ii.mutable.impl

import com.owlunit.core.ii.mutable.Ii
import com.typesafe.scalalogging.slf4j.Logging
import com.tinkerpop.blueprints._
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.scala._
import scala.Some

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

private[impl] class BlueprintIi( var node: Option[Vertex], val graph: Ii.IiGraph) extends Ii with Logging {

  def this(graph: Ii.IiGraph) = this(None, graph)
  def this(node: Vertex, graph: Ii.IiGraph) = this(Some(node), graph)

  private var itemsOption: Option[Map[Ii, Double]] = None
  private var metaOption: Option[Map[String, String]] = None

  def id = node match {
    case Some(n) if n.getId != null => n.getId.toString
    case _ => ""
  }

  def meta: Map[String, String] = metaOption match {

    case Some(existing) => existing

    case None => {
      val initialized = node match {
        case Some(n) => getNodeMeta(n)
        case None    => Map[String, String]()
      }
      metaOption = Some(initialized)
      initialized
    }
  }

  def items: Map[Ii, Double] = itemsOption match {

    case Some(existing) => existing

    case None => {
      val initialized = node match {
        case Some(n) => getNodeItems(n)
        case None    => Map[Ii, Double]()
      }
      itemsOption = Some(initialized)
      initialized
    }
  }

  def setMeta(key: String, value: String) = {
    metaOption = Some(meta + (key -> value))
    this
  }

  def setItem(component: Ii, weight: Double) = {
    itemsOption = Some(items + (component -> weight))
    this
  }

  def removeMeta(key: String) = {
    metaOption = Some(meta - key)
    this
  }

  def removeItem(component: Ii) = {
    itemsOption = Some(items - component)
    this
  }

  def save:Ii = {

    // create backed node if there is need for one
    if (this.node.isEmpty) {
      this.node = Some(graph.addVertex())
    }

    // remove obsolete data in indexes
    // pass this part if no meta referenced
    for {
      vertex  <- this.node
      metaMap <- metaOption
    } {
      vertex.getPropertyKeys
        .filter(!metaMap.contains(_))
        .foreach(vertex.removeProperty(_))
    }

    // persist all meta to properties
    // pass this part if no meta referenced
    for {
      vertex       <- this.node
      metaMap      <- metaOption
      (key, value) <- metaMap
    } {
      vertex.setProperty(key, value)
    }

    // remove obsolete connections
    // pass this part if no items referenced
    for {
      thisNode   <- this.node
      itemsMap   <- itemsOption
      itemsNodes = itemsMap.keys.map(_.node).flatten.toSet
      edge       <- thisNode.getEdges(Direction.OUT) if !itemsNodes.contains(edge.getVertex(Direction.IN))
    } {
      graph.removeEdge(edge)
    }

    // persist all connections to relations
    // pass this part if no items referenced
    // NB: only for those which have been persisted once
    for {
      thisNode <- this.node
      itemsMap <- this.itemsOption
      (item: BlueprintIi, weight) <- itemsMap
      thatNode <- item.node
    } {

      val edge = getEdge(thisNode, thatNode) match {
        case Some(value) => value
        case None => graph.addE(thisNode, thatNode, EdgeTitle)
      }
      edge.setProperty(WeightPropertyKey, weight)
    }

    graph.commit()
    this
  }

  def delete() {
    for {
      vertex <- this.node
    } {
      vertex.getEdges(Direction.BOTH).map(graph.removeEdge(_))
      graph.removeVertex(vertex)
      graph.commit()
    }
  }



  private def getNodeMeta(node: Vertex): Map[String, String] = {
    node.getPropertyKeys.map(key => key -> node.getProperty(key).toString).toMap
  }

  private def getNodeItems(node: Vertex): Map[Ii, Double] = {
    node.outE().toList.map(
      edge => (edge.getVertex(Direction.IN), edge.getProperty(WeightPropertyKey).toString.toDouble)
    ).map{
      case (n, w) => new BlueprintIi(Some(n), graph) -> w
    }.toMap
  }

  override def hashCode() = this.node.map(_.hashCode()).getOrElse(0)
  override def equals(p: Any) = p.isInstanceOf[BlueprintIi] && node == p.asInstanceOf[BlueprintIi].node
  override def toString = s"Ii($id)"

}
