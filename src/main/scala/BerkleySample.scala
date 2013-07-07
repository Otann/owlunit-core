import com.owlunit.core.ii.mutable.Ii
import com.thinkaurelius.titan.core.TitanFactory
import com.tinkerpop.gremlin.scala._

//TODO(anton): fill description
/**
 * DESCRIPTION PLACEHOLDER 
 *
 * @author anton
 */
object BerkleySample extends App {

  val label = "w"
  val property = "weight"

  def addNode(name: String) = {
    val vertex = graph.addV()
    vertex.setProperty("name", name)
    graph.commit()
    vertex
  }

  val graph: Ii.IiGraph = TitanFactory.open("/tmp/graph")

  try {
    val v1 = addNode("1")
    val v2 = addNode("2")
    val v3 = addNode("3")
    val v4 = addNode("4")

    graph.addE(v1, v2, label).setProperty(property, 12)
    graph.addE(v1, v3, label).setProperty(property, 13)
    graph.addE(v1, v4, label).setProperty(property, 14)

    graph.addE(v3, v4, label).setProperty(property, 34)

  } finally {
    graph.shutdown()
  }


}
