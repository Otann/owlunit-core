package com.owlunit.core.ii.mutable

import org.specs2.mutable.Specification
import com.owlunit.core.ii.NotFoundException
import scala.sys.process._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class IiSpecs extends Specification {

  def getRandomIi: Ii = dao.load(dao.create.save.id)
  def createIi(name: String): Ii = dao.load(dao.create.setMeta("name", name).save.id)

  var dao: IiDao = null
  val dbPath = "target/neo4j_db"

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
      val saved = dao.create.setMeta("load-meta-test", "load-meta-test-value").save
      dao.load(saved.id).meta must havePair("load-meta-test" -> "load-meta-test-value")
    }
    "load items" in {
      val item1 = dao.load(dao.create.save.id)
      val item2 = dao.load(dao.create.save.id)

      val saved = dao.create.setItem(item1, 1.0).save
      val loaded = dao.load(saved.id).setItem(item2, 1.0).save
      dao.load(loaded.id).items must havePair(item2 -> 1.0)
    }
    "persist used Ii into non-empty ii" in {
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

  "Ii's meta index" should {
    "be able to load with indexing" in {
      val key = "key | not be able to load with indexing"
      val value = "value | not be able to load with indexing"

      val saved = dao.create.setMeta(key, value, isIndexedFulltext = true).save
      val loaded = dao.load(key, value)
      loaded must contain(saved)
    }
    "be able to load without indexing" in {
      val key = "key | be able to load without indexing"
      val value = "value | be able to load without indexing"

      val saved = dao.create.setMeta(key, value).save
      val loaded = dao.load(key, value)
      loaded must contain(saved)
    }
    "be able to search with indexing" in {
      val key = "key | be able to search with indexing"
      val value = "value | be able to search with indexing (alloda)"

      val saved = dao.create.setMeta(key, value, isIndexedFulltext = true).save
      val loaded = dao.search(key, value)
      loaded must contain(saved)
    }
    "not be able to search with indexing" in {
      val key = "key | not be able to search with indexing"
      val value = "value | not be able to search with indexing"

      val saved = dao.create.setMeta(key, value).save
      val loaded = dao.search(key, value)
      loaded must not contain(saved)
    }
  }

  "Recommender" should {
    "find indirect component" in {
      val leaf = dao.create.save
      val middle = dao.create.setItem(leaf, 1.0).save
      val root = dao.create.setItem(middle, 1.0).save
      dao.indirectComponents(root, 2) must not beEmpty
    }
    "give zero recommendations for empty ii" in {
      val ii = dao.create.save
      dao.getSimilar(ii, "any") must beEmpty
    }
    "give at least 1 recommendations for good case (common leaf, right meta)" in {
      val component = createIi("component").save
      val rootA = createIi("rootA").setMeta("test", "true").setItem(component, 1.0).save
      val rootB = createIi("rootB").setMeta("test", "true").setItem(component, 1.0).save

      dao.getSimilar(rootA, "test") must haveKey(rootB)
    }
  }


  step {
    dao.shutdown()
    Seq("rm", "-r", dbPath).!!
  }

}