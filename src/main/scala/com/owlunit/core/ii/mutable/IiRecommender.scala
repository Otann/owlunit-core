package com.owlunit.core.ii.mutable

/**
  * @author Anton Chebotaev
  *         Owls Proprietary
  */

trait IiRecommender {

  def defaultLimit = 100

  def compare(a: Ii, b: Ii): Double //TODO: what is double?

  def getSimilar(a: Ii, key: String, limit: Int = defaultLimit): List[(Ii, Double)]
  def recommend(p: Map[Ii, Double], key: String, limit: Int = defaultLimit): List[(Ii, Double)]

}