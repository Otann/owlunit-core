package com.owlunit.core.ii.mutable

import com.tinkerpop.blueprints.{KeyIndexableGraph, TransactionalGraph, IndexableGraph, Graph}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 * Since this will be firstly and mostly used in couple with lift-mongo-record this is designed mutable
 * As implementation is simple, this can be used as backend for immutable version
 */


trait Ii {
  import Ii._

  def id: IdType

  def save: Ii
  def delete()

  // Q: If this is mutable ii why collections are immutable?
  // A: Not to mess up with references to these collections

  def meta: Map[String, String]
  def items: Map[Ii, Double]

  def setMeta(key: String, value: String): Ii
  def setItem(component: Ii, weight: Double): Ii

  def removeMeta(key: String): Ii
  def removeItem(component: Ii): Ii

}

object Ii {

  type IdType = String
  type IiGraph = Graph with KeyIndexableGraph with TransactionalGraph

}