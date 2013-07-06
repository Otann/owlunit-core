package com.owlunit.core.ii.mutable

import impl.{AkkaRecommender, BlueprintIiDao}
import com.thinkaurelius.titan.core.TitanFactory
import org.apache.commons.configuration.BaseConfiguration
import akka.actor.{ActorSystem, ActorContext}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
trait IiService extends IiDao with IiRecommender

object IiService {

  class RecommenderDao(graph: Ii.IiGraph, val system: ActorSystem)
    extends BlueprintIiDao(graph) with AkkaRecommender with IiService

  def apply(graph: Ii.IiGraph, context: ActorSystem): IiService = new RecommenderDao(graph, context)

  def local(path: String, context: ActorSystem): IiService = {
    val conf = new BaseConfiguration()
    conf.setProperty("storage.directory", path)
    conf.setProperty("storage.backend", "berkeleyje")
    val graph = TitanFactory.open(conf)
    apply(graph, context)
  }

  //TODO: fix
  def remote(url: String, context: ActorSystem): IiService = {
    val conf = new BaseConfiguration()
    conf.setProperty("storage.backend", "cassandra")
    conf.setProperty("storage.hostname", url)
    val graph = TitanFactory.open(conf)
    apply(graph, context)
  }

}
