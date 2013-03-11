package com.owlunit.core.ii.mutable.impl.actors

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
class WeightMap extends collection.mutable.HashMap[Long, Double] with collection.mutable.SynchronizedMap[Long, Double] {

  override def default(key: Long) = 0.0

}

object WeightMap {

  def apply(): WeightMap = new WeightMap()

  implicit def fromImmutable(original: Map[Long, Double]): WeightMap = {
    val result = new WeightMap()
    original.foreach{ case (key, value) => result(key) = value }
    result
  }

}


