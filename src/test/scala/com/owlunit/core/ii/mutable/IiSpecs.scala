package com.owlunit.core.ii.mutable

import org.specs2.mutable.Specification
import com.owlunit.core.ii.NotFoundException
import scala.sys.process._
import java.util.UUID
import com.weiglewilczek.slf4s.Logging

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class IiSpecs extends Specification with Logging {

  def getRandomIi: Ii = dao.load(dao.create.save.id)
  def createIi(name: String): Ii = dao.load(dao.create.setMeta("name", name).save.id)

  def randomString = UUID.randomUUID().toString
  def randomKeyValue: (String, String) = ("key-%s" format randomString, "value-%s" format randomString)

  var dao: IiDao = null
  val dbPath = "/tmp/neo4j_db"

  step {
    dao = IiDao.local(dbPath)
  }

  "New Ii" should {
    "have 0 id" in {
      val ii = dao.create
      ii.id mustEqual 0
    }
    "not be loadable by id after delete" in {
      val saved = dao.create.save
      saved.delete()
      dao.load(saved.id) must throwA[NotFoundException]
    }
  }

  "Loaded Ii" should {
    "load meta" in {
      val (key, value) = randomKeyValue

      val saved = dao.create.setMeta(key, value).save
      dao.load(saved.id).meta must havePair(key -> value)
    }
    "load items" in {
      val item1 = dao.load(dao.create.save.id)
      val item2 = dao.load(dao.create.save.id)

      val saved = dao.create.setItem(item1, 1.0).save
      val loaded = dao.load(saved.id).setItem(item2, 1.0).save
      dao.load(loaded.id).items must havePair(item2 -> 1.0)
    }
    "keep having used Ii into non-empty ii" in {
      // create items
      val a = createIi("persist used Ii into non-empty ii a")
      val b = createIi("persist used Ii into non-empty ii b")

      // make b used
      getRandomIi.setItem(dao.load(b.id), 1.0).save

      // make a non-empty
      dao.load(a.id).setItem(getRandomIi, 1.0).save

      // perform add
      dao.load(a.id).setItem(dao.load(b.id), 239.0).save

      dao.load(a.id).items.size mustEqual 2
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
      loaded must not contain(saved)
    }

    "remove fulltext index after new value set without it" in {
      val (key, value) = randomKeyValue

      var saved = dao.create.setMeta(key, value, isFulltext = true).save
      saved = dao.load(saved.id)
      saved.setMeta(key, randomString, isFulltext = false).save

      val loaded = dao.search(key, value)
      loaded must not contain(saved)
    }
    "remove general index after new value set with fulltext" in {
      val (key, value) = randomKeyValue

      var saved = dao.create.setMeta(key, value, isFulltext = false).save
      saved = dao.load(saved.id)
      saved.setMeta(key, randomString, isFulltext = true).save

      val loaded = dao.load(key, value)
      loaded must not contain(saved)
    }
  }

  "Recommender" should {
    "find indirect component, depth 1" in {
      val leaf = dao.create.save
      val root = dao.create.setItem(leaf, 1.0).save
      dao.indirectComponents(root, 1).size mustEqual 1
    }
    "find indirect component, depth 2" in {
      val leaf = dao.create.save
      val middle = dao.create.setItem(leaf, 1.0).save
      val root = dao.create.setItem(middle, 1.0).save
      dao.indirectComponents(root, 2).size mustEqual 2
    }
    "give zero recommendations for empty ii" in {
      val ii = dao.create.save
      dao.recommend(Map(ii -> 1), "any") must beEmpty
    }
    "give at least 1 recommendations for common leaf, filled meta" in {
      val component = createIi("component").save
      val rootA = createIi("rootA").setMeta("test", "true").setItem(component, 1.0).save
      val rootB = createIi("rootB").setMeta("test", "true").setItem(component, 1.0).save

      dao.recommend(Map(rootA -> 1), "test") must haveKey(rootB)
    }
    "give at least 1 recommendations for common leaf, filled meta with indexing" in {
      val component = createIi("component").save
      val rootA = createIi("rootA").setMeta("test", "true", isFulltext = true).setItem(component, 1.0).save
      val rootB = createIi("rootB").setMeta("test", "true", isFulltext = true).setItem(component, 1.0).save

      dao.recommend(Map(rootA -> 1), "test") must haveKey(rootB)
    }
  }

  step {
    dao.shutdown()
    Seq("rm", "-r", dbPath).!!
  }

}