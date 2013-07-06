import com.thinkaurelius.titan.core.{TitanGraph, TitanFactory}
import com.typesafe.scalalogging.slf4j.Logging
import org.apache.commons.configuration.BaseConfiguration
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.scala._

//TODO(anton): fill description
/**
 * DESCRIPTION PLACEHOLDER 
 *
 * @author anton
 */
object BerkleySample extends App {

  // Default
  val graph = TitanFactory.open("/tmp/graph")
  val v1 = graph.addV()
  graph.commit()
  println(s"created $v1")

  graph.removeVertex(v1)
  graph.commit()
  println(s"removed")

  val v2 = graph.getVertex(v1.getId)
  println(s"loaded $v2")

  graph.shutdown()
}
