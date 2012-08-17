package com.owlunit.core.ii.mutable

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         Since this will be firstly and mostly used in couple with lift-mongo-record
 *         this is designed mutable, and well, this is less resource consuming too
 *
 *         Simply enough this can be used as backend for immutable version before pure
 *         immutable realization will be used
 */


trait Ii {

  def id: Long

  def meta: Option[Map[String, String]]
  def items: Option[Map[Ii, Double]]

  def loadMeta: Ii
  def loadItems: Ii

  def save: Ii
  def delete()

  def setMeta(key: String, value: String): Ii
  def setItem(component: Ii, weight: Double): Ii

  def removeMeta(key: String): Ii
  def removeItem(component: Ii): Ii
}