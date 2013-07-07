package com.owlunit.core.ii.mutable

import impl.actors._
import impl.actors.LoadIndirect
import impl.actors.LoadParents
import impl.actors.MapsWithId
import impl.actors.Likeness
import impl.actors.LoadedMap
import com.owlunit.core.ii.mutable.utils.{TestForwardParentRef, AkkaSpec, IiHelpers}
import akka.testkit.TestActorRef
import akka.actor.{ActorSystem, Props}
import com.typesafe.scalalogging.slf4j.Logging
import java.util.UUID
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import org.specs2.specification.Scope

/**
* @author Anton Chebotaev
*/
class ActorsSpecs extends Specification
  with NoTimeConversions with Scope
  with Logging with IiHelpers {

  // Generate new system for each test
  def system = ActorSystem("IiTestSpec" + UUID.randomUUID().toString)

  sequential

  "Comparator" should {
    "return 100% on equal maps" in new AkkaSpec(system) {
      val map = Map("1" -> 1.0)
      TestActorRef(Props[Comparator]) ! MapsWithId("1", map, map)

      expectMsg(Likeness("1", 100.0))
    }
  }

  "Loader" should {
    "load indirect" in new AkkaSpec(system) {
      val child = getRandomIi(dao)
      val parent = dao.create.setItem(child, 1.0).save
      val actor = TestActorRef(Props(new Loader(dao)))
      actor ! LoadIndirect(parent.id, 1.0, 1)

      expectMsg(LoadedMap(parent.id, 1.0, Map(child.id -> 1.0)))
    }
    "load parents" in new AkkaSpec(system) {
      val (key, value) = randomKeyValue
      val child = getRandomIi(dao)
      val parent = dao.create.setItem(child, 1.0).setMeta(key, value).save
      val actor = TestActorRef(Props(new Loader(dao)))
      actor ! LoadParents(child.id, 1.0, key)

      expectMsg(LoadedMap(child.id, 1.0, Map(parent.id -> 1.0)))
    }
  }

    "WeightMerger" should {
      "merge custom maps" in new AkkaSpec(system) {
        val actor = TestForwardParentRef(Props(new MapsMerger(2, 2)))
        actor ! LoadedMap("1", 1, Map("1" -> 2))
        actor ! LoadedMap("1", 2, Map("1" -> 1))

        expectMsg(Merged(Map("1" -> 2.0))) //TODO: check arithmetic
      }
    }

    "IndirectMergeLoader" should {
      "merge indirect" in new AkkaSpec(system) {
        val actor = TestForwardParentRef(Props(new MergeLoaderIndirect(dao)))
  
        val child = getRandomIi(dao)
        val parent1 = getRandomIi(dao).setItem(child, 3).save
        val parent2 = getRandomIi(dao).setItem(child, 1).save
  
        actor ! LoadMergedIndirect(Map(parent1.id -> 1, parent2.id -> 3), 3)
  
        expectMsg(MergedIndirect(Map(child.id -> 1.5))) //TODO: check arithmetic
      }
    }
  
    "IndirectParentsLoader" should {
      "merge parents" in new AkkaSpec(system) {
        val (key, value) = randomKeyValue
        val actor = TestForwardParentRef(Props(new MergeLoaderParents(dao)))
  
        val child1 = getRandomIi(dao)
        val child2 = getRandomIi(dao)
        val parent = getRandomIi(dao).setMeta(key, value)
          .setItem(child1, 3).setItem(child2, 3).save
  
        actor ! LoadMergedParents(Map(child1.id -> 1, child2.id -> 3), key)
  
        expectMsg(MergedParents(Map(parent.id -> 3.0))) //TODO: check arithmetic
      }
    }
  
    "SimilarFiner" should {
      "find similar" in new AkkaSpec(system) {
        val (key, value) = randomKeyValue
        val actor = TestForwardParentRef(Props(new SimilarFinder(dao)))
  
        val child = getRandomIi(dao)
        val parent1 = getRandomIi(dao).setMeta(key, value).setItem(child, 3).save
        val parent2 = getRandomIi(dao).setMeta(key, value).setItem(child, 1).save
  
        actor ! FindSimilar(Map(parent1.id -> 1), key, 1)
  
        expectMsg(Similar(List(parent2.id -> 100.0))) //TODO: check arithmetic
      }
    }

}