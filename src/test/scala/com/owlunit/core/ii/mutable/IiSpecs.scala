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
    "persist meta" in {
      val saved = dao.create.setMeta("key1", "value").save
      val loaded = dao.load(saved.id).setMeta("key2", "value").save
      dao.load(loaded.id).meta must havePair("key2" -> "value")
    }
    "persist items" in {
      val item1 = dao.load(dao.create.save.id)
      val item2 = dao.load(dao.create.save.id)

      val saved = dao.create.setItem(item1, 1.0).save
      val loaded = dao.load(saved.id).setItem(item2, 1.0).save
      dao.load(loaded.id).items must havePair(item2 -> 1.0)
    }
    "persist used Ii into non-empty ii" in {
      // create items
      val a = createIi("a")
      val b = createIi("b")

      // make b used
      getRandomIi.setItem(dao.load(b.id), 1.0).save

      // make a non-empty
      dao.load(a.id).setItem(getRandomIi, 1.0).save

      // perform add
      dao.load(a.id).setItem(dao.load(b.id), 239.0).save

      dao.load(a.id).items.size mustEqual 2
    }

  }

  step {
    dao.shutdown()
    Seq("rm", "-r", dbPath).!!
  }

}