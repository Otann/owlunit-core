package com.owlunit.core.ii.mutable

import impl.actors._
import impl.actors.LoadIndirect
import impl.actors.LoadParents
import impl.actors.MapsWithId
import impl.actors.Likeness
import impl.actors.LoadedMap
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import scala.sys.process._
import com.weiglewilczek.slf4s.Logging
import utils.{TestForwardParentRef, AkkaSpec, IiHelpers}
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import akka.actor.{ActorSystem, Props}
import org.specs2.specification.Scope

/**
* @author Anton Chebotaev
*         Owls Proprietary
*/


class ActorsSpecs
  extends AkkaSpec with Specification with Scope
  with Logging with NoTimeConversions with IiHelpers {

  sequential // forces all tests to be run sequentially

  val dbPath = "/tmp/neo4j_db"
  var dao: IiService = null

  step {
    dao = IiService.local(dbPath)
  }

  "Comparator" should {
    "return 100% on equal maps" in new AkkaSpec {
      val map = Map(1L -> 1.0)
      TestActorRef(Props[Comparator]) ! MapsWithId(1, map, map)

      expectMsg(Likeness(1, 100.0))
    }
  }

  "Loader" should {
    "load indirect" in new AkkaSpec {
      val child = getRandomIi
      val parent = dao.create.setItem(child, 1.0).save
      val actor = TestActorRef(Props(new Loader(dao)))
      actor ! LoadIndirect(parent.id, 1.0, 1)

      expectMsg(LoadedMap(parent.id, 1.0, Map(child.id -> 1.0)))
    }
    "load parents" in new AkkaSpec {
      val (key, value) = randomKeyValue
      val child = getRandomIi
      val parent = dao.create.setItem(child, 1.0).setMeta(key, value).save
      val actor = TestActorRef(Props(new Loader(dao)))
      actor ! LoadParents(child.id, 1.0, key)

      expectMsg(LoadedMap(child.id, 1.0, Map(parent.id -> 1.0)))
    }
  }

  "WeightMerger" should {
    "merge custom maps" in new AkkaSpec {
      val actor = TestForwardParentRef(Props(new MapsMerger(2, 2)))
      actor ! LoadedMap(1, 1, Map(1L -> 2))
      actor ! LoadedMap(1, 2, Map(1L -> 1))

      expectMsg(Merged(Map(1L -> 2.0))) //TODO: check arithmetic
    }
  }

  "IndirectMergeLoader" should {
    "merge indirect" in new AkkaSpec {
      val actor = TestForwardParentRef(Props(new MergeLoaderIndirect(dao)))

      val child = getRandomIi
      val parent1 = getRandomIi.setItem(child, 3).save
      val parent2 = getRandomIi.setItem(child, 1).save

      actor ! LoadMergedIndirect(Map(parent1.id -> 1, parent2.id -> 3), 3)

      expectMsg(MergedIndirect(Map(child.id -> 1.5))) //TODO: check arithmetic
    }
  }

  "IndirectParentsLoader" should {
    "merge parents" in new AkkaSpec {
      val (key, value) = randomKeyValue
      val actor = TestForwardParentRef(Props(new MergeLoaderParents(dao)))

      val child1 = getRandomIi
      val child2 = getRandomIi
      val parent = getRandomIi.setMeta(key, value)
        .setItem(child1, 3).setItem(child2, 3).save

      actor ! LoadMergedParents(Map(child1.id -> 1, child2.id -> 3), key)

      expectMsg(MergedParents(Map(parent.id -> 3.0))) //TODO: check arithmetic
    }
  }

  "SimilarFiner" should {
    "find similar" in new AkkaSpec {
      val (key, value) = randomKeyValue
      val actor = TestForwardParentRef(Props(new SimilarFinder(dao)))

      val child = getRandomIi
      val parent1 = getRandomIi.setMeta(key, value).setItem(child, 3).save
      val parent2 = getRandomIi.setMeta(key, value).setItem(child, 1).save

      actor ! FindSimilar(Map(parent1.id -> 1), key, 1)

      expectMsg(Similar(List(parent2.id -> 100.0))) //TODO: check arithmetic
    }
  }

  step {
    dao.shutdown()
    Seq("rm", "-rf", dbPath).!!
  }

}