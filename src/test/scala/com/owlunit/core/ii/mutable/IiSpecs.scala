package com.owlunit.core.ii.mutable

import org.specs2.mutable.{Before, After, Specification}
import scala.sys.process._
import utils.IiHelpers
import com.typesafe.scalalogging.slf4j.Logging
import akka.actor.ActorSystem

/**
* @author Anton Chebotaev
*         Owls Proprietary
*/


class IiSpecs extends Specification with Logging with IiHelpers {
  sequential // forces all tests to be run sequentially

  val dbPath = "/tmp/titan_" + randomString
  var dao: IiService = null
  val akka = ActorSystem()

  step {
    dao = IiService.local(dbPath, akka)
  }

  "New Ii" should {
    "have empty string id before save" in {
      val ii = dao.create
      ii.id mustEqual ""
    }
    "not be loadable by id after delete" in {
      val saved = dao.create.save
      saved.delete()
      val loaded = dao.load(saved.id)
      loaded must beNone
    }
  }

  "Loaded Ii" should {
    "load meta" in {
      val (key, value) = randomKeyValue

      val saved = dao.create.setMeta(key, value).save
      dao.load(saved.id).get.meta must havePair(key -> value)
    }
    "load items" in {
      val item1 = dao.load(dao.create.save.id).get
      val item2 = dao.load(dao.create.save.id).get
      val weight = 1.0

      val saved = dao.create.setItem(item1, weight).save
      val loaded = dao.load(saved.id).get.setItem(item2, weight).save
      dao.load(loaded.id).get.items must havePair(item1 -> weight)
    }
    "keep having used Ii into non-empty ii" in {
      // create items
      val a = createIi("persist used Ii into non-empty ii a")
      val b = createIi("persist used Ii into non-empty ii b")

      // make b used
      getRandomIi.setItem(dao.load(b.id).get, 1.0).save

      // make a non-empty
      dao.load(a.id).get.setItem(getRandomIi, 1.0).save

      // perform add
      dao.load(a.id).get.setItem(dao.load(b.id).get, 239.0).save

      dao.load(a.id).get.items.size mustEqual 2
    }
  }

  "Ii's meta indexing" should {
    "be able to load with fulltext indexing" in {
      val (key, value) = randomKeyValue

      val saved = dao.create.setMeta(key, value, isFulltext = true).save
      val loaded = dao.load(key, value)
      loaded must contain(saved)
    }
    "be able to load without fulltext indexing" in {
      val (key, value) = randomKeyValue

      val saved = dao.create.setMeta(key, value).save
      val loaded = dao.load(key, value)
      loaded must contain(saved)
    }
    "be able to search with fulltext indexing" in {
      val (key, value) = randomKeyValue

      val saved = dao.create.setMeta(key, value, isFulltext = true).save
      val loaded = dao.search(key, value)
      loaded must contain(saved)
    }
    "not be able to search without fulltext indexing" in {
      val (key, value) = randomKeyValue

      val saved = dao.create.setMeta(key, value).save
      val loaded = dao.search(key, value)
      loaded must not contain saved
    }

    "remove fulltext index after new value set without it" in {
      val (key, value) = randomKeyValue

      var saved = dao.create.setMeta(key, value, isFulltext = true).save
      saved = dao.load(saved.id).get
      saved.setMeta(key, randomString, isFulltext = false).save

      val loaded = dao.search(key, value)
      loaded must not contain saved
    }
    "remove general index after new value set with fulltext" in {
      val (key, value) = randomKeyValue

      var saved = dao.create.setMeta(key, value, isFulltext = false).save
      saved = dao.load(saved.id).get
      saved.setMeta(key, randomString, isFulltext = true).save

      val loaded = dao.load(key, value)
      loaded must not contain saved
    }
  }

  "Recommender" should {
    "find indirect component, depth 1" in {
      val leaf = dao.create.save
      val root = dao.create.setItem(leaf, 1.0).save
      dao.indirectComponents(root.id, 1).size mustEqual 1
    }
    "find indirect component, depth 2" in {
      val leaf = dao.create.save
      val middle = dao.create.setItem(leaf, 1.0).save
      val root = dao.create.setItem(middle, 1.0).save
      dao.indirectComponents(root.id, 2).size mustEqual 2
    }
    "give zero recommendations for empty ii" in {
      val ii = dao.create.save
      val loaded = dao.load(ii.id).get
      dao.recommend(Map(loaded -> 1), "any") must beEmpty
    }
    "give at least 1 recommendations for common leaf, filled meta" in {
      val (key, value) = randomKeyValue

      val component = createIi("component").save
      val rootA = createIi("rootA").setMeta(key, value).setItem(component, 1.0).save
      val rootB = createIi("rootB").setMeta(key, value).setItem(component, 1.0).save

      dao.recommend(Map(rootA -> 1), key) must haveKey(rootB)
    }
    "give at least 1 recommendations for common leaf, filled meta with indexing" in {
      val (key, value) = randomKeyValue

      val component = createIi("component").save
      val rootA = createIi("rootA").setMeta(key, value, isFulltext = true).setItem(component, 1.0).save
      val rootB = createIi("rootB").setMeta(key, value, isFulltext = true).setItem(component, 1.0).save

      dao.recommend(Map(rootA -> 1), key) must haveKey(rootB)
    }
  }

  step {
    akka.shutdown()
    Seq("rm", "-rf", dbPath).!!
  }

}