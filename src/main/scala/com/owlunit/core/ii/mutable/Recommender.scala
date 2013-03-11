package com.owlunit.core.ii.mutable

/**
  * @author Anton Chebotaev
  *         Owls Proprietary
  */

trait Recommender {

  def defaultLimit = 100

  //TODO: what is double?
  def compare(a: Ii, b: Ii): Double

  def getSimilar(a: Ii, key: String, limit: Int = defaultLimit): List[(Ii, Double)]
  def recommend(p: Map[Ii, Double], key: String, limit: Int = defaultLimit): List[(Ii, Double)]

}
