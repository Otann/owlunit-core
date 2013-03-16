package com.owlunit.core.ii.mutable

import impl.actors._
import impl.actors.LoadIndirect
import impl.actors.LoadParents
import impl.actors.Maps
import impl.actors.MapsLikeness
import impl.actors.WeightedMap
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

  var dao: RecoDao = null
  val dbPath = "/tmp/neo4j_db"

  step {
    dao = IiDao.local(dbPath)
  }

  "Comparator" should {
    "return 100% on equal maps" in new AkkaSpec {
      val map = Map(1L -> 1.0)
      TestActorRef(Props[Comparator]) ! Maps(map, map)

      expectMsg(MapsLikeness(100.0))
    }
  }

  "Loader" should {
    "load indirect" in new AkkaSpec {
      val child = getRandomIi
      val item = dao.create.setItem(child, 1.0).save
      val actor = TestActorRef(Props(new Loader(dao)))
      actor ! LoadIndirect(item.id, 1.0, 1)

      expectMsg(WeightedMap(Map(child.id -> 1.0), 1.0))
    }
    "load parents" in new AkkaSpec {
      val (key, value) = randomKeyValue
      val child = getRandomIi
      val item = dao.create.setItem(child, 1.0).setMeta(key, value).save
      val actor = TestActorRef(Props(new Loader(dao)))
      actor ! LoadParents(child.id, 1.0, key)

      expectMsg(WeightedMap(Map(item.id -> 1.0), 1.0))
    }
  }

  "WeightMerger" should {
    "merge custom maps" in new AkkaSpec {
      val actor = TestForwardParentRef(Props(new WeightMerger(2, 2)))
      actor ! WeightedMap(Map(1L -> 2), 1)
      actor ! WeightedMap(Map(1L -> 1), 2)

      expectMsg(MergedMap(Map(1L -> 2.0))) //TODO: check arithmetic
    }
  }

  "IndirectMergeLoader" should {
    "merge indirect" in new AkkaSpec {
      val actor = TestForwardParentRef(Props(new IndirectMergeLoader(dao, 2)))

      val child = getRandomIi
      val parent1 = getRandomIi.setItem(child, 3).save
      val parent2 = getRandomIi.setItem(child, 1).save

      actor ! LoadMerged(Map(parent1.id -> 1, parent2.id -> 3))

      expectMsg(MergedMap(Map(child.id -> 1.5))) //TODO: check arithmetic
    }
  }

  "IndirectParentsLoader" should {
    "merge parents" in new AkkaSpec {
      val (key, value) = randomKeyValue
      val actor = TestForwardParentRef(Props(new ParentsMergeLoader(dao, key)))

      val child1 = getRandomIi
      val child2 = getRandomIi
      val parent = getRandomIi.setMeta(key, value)
        .setItem(child1, 3).setItem(child2, 3).save

      actor ! LoadMerged(Map(child1.id -> 1, child2.id -> 3))

      expectMsg(MergedMap(Map(parent.id -> 3.0))) //TODO: check arithmetic
    }
  }

  step {
    dao.shutdown()
    Seq("rm", "-r", dbPath).!!
  }

}