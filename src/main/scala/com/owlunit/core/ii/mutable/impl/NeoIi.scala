package com.owlunit.core.ii.mutable.impl

import com.owlunit.core.ii.mutable.Ii
import org.neo4j.graphdb._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

private[impl] class NeoIi(var node: Option[Node], graph: GraphDatabaseService) extends Ii with Helpers {

  def this(graph: GraphDatabaseService) = this(None, graph)
  def this(node: Node, graph: GraphDatabaseService) = this(Some(node), graph)

  private def fulltextIndex = graph.index().forNodes(FulltextIndexName, FulltextIndexParams)
  private def exactIndex = graph.index().forNodes(ExactIndexName, ExactIndexParams)

  private var itemsOption: Option[Map[Ii, Double]] = None
  private var metaOption: Option[Map[String, String]] = None
  private var indexedMeta: Set[String] = Set()

  def id = node.map(_.getId).getOrElse(0)

  def meta: Map[String, String] = metaOption match {

    case Some(existing) => existing

    case None => {
      val initialized = node match {
        case Some(n) => getMeta(n)
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
        case Some(n) => getItems(n)
        case None    => Map[Ii, Double]()
      }
      itemsOption = Some(initialized)
      initialized
    }
  }

  def save:Ii = {

    val tx = graph.beginTx()
    try {

      // create backed node is there is need for one
      if (this.node.isEmpty) {
        this.node = Some(graph.createNode())
      }

      // remove obsolete properties
      // pass this part if no meta referenced
      for {
        n       <- this.node
        metaMap <- metaOption
      } {
        val properties = n.getPropertyKeys.iterator()
        while (properties.hasNext) {
          val key = properties.next()
          if (!metaMap.contains(key)) {
            n.removeProperty(key)
            fulltextIndex.remove(n, key)
            exactIndex.remove(n, key)
          }
        }
      }

      // persist all meta to properties
      // pass this part if no meta referenced
      for {
        n            <- this.node
        metaMap      <- metaOption
        (key, value) <- metaMap
      } {
        n.setProperty(key, value)
        val index = if (indexedMeta.contains(key)) fulltextIndex else exactIndex
        index.add(n, key, value)
      }

      // remove obsolete connections
      // pass this part if no items referenced
      for {
        thisNode <- this.node
        itemsMap <- itemsOption
      } {
        val itemsNodes = itemsMap.keys.map(_.node).flatten.toSet

        val relationships = thisNode.getRelationships.iterator()
        while (relationships.hasNext) {
          val relationship = relationships.next()

          if (!itemsNodes.contains(relationship.getOtherNode(thisNode))) {
            relationship.delete()
          }
        }
      }

      // persist all connections to relations
      // pass this part if no items referenced
      // NB: only for those which have been persisted once
      for {
        thisNode              <- this.node
        itemsMap              <- this.itemsOption
        (item: NeoIi, weight) <- itemsMap
        thatNode              <- item.node
      } {

        val rel = getRelation(thisNode, thatNode) match {
          case Some(relation) => relation
          case None => thisNode.createRelationshipTo(thatNode, RelType)
        }
        rel.setProperty(WeightPropertyName, weight)
      }

      tx.success()

      this
    } finally {
      tx.finish()
    }

  }

  def delete() {

    val tx = graph.beginTx()
    try {

      if (this.node.isEmpty) {
        return
      }

      for {
        n <- this.node
      } {
        fulltextIndex.remove(n)
        val relationships = n.getRelationships.iterator()
        while (relationships.hasNext) {
          relationships.next().delete()
        }

        n.delete()
      }

      tx.success()

      this
    } finally {
      tx.finish()
    }

  }

  private def getMeta(node: Node): Map[String, String] = {
    val newMeta = collection.mutable.Map[String, String]()
    val iterator = node.getPropertyKeys.iterator()

    while (iterator.hasNext) {
      val key = iterator.next()
      newMeta.put(key, node.getProperty(key).toString)
    }
    newMeta.toMap
  }

  private def getItems(node: Node): Map[Ii, Double] = {
    val nodes = getNodes(node, Direction.OUTGOING, 1)
    nodes.map {case (n, w) => new NeoIi(Some(n), graph) -> w}
  }

  def setMeta(key: String, value: String, isIndexedFulltext: Boolean) = {
    metaOption = Some(meta + (key -> value))
    if (isIndexedFulltext) {
      indexedMeta += key
    } else {
      indexedMeta -= key
    }
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

  override def hashCode() = this.node.map(_.hashCode()).getOrElse(0)
  override def equals(p: Any) = p.isInstanceOf[NeoIi] && node == p.asInstanceOf[NeoIi].node
  override def toString = "Ii(%d)" format id

}
