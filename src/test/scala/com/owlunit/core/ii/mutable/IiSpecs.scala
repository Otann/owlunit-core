package com.owlunit.core.ii.mutable

import org.specs2.mutable.Specification
import com.owlunit.core.ii.NotFoundException
import java.io.File

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class IiSpecs extends Specification {

  var dao: IiDao = null
  val dbPath = "target/neo4j_db"

  step {
    (new File(dbPath)).delete()
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
    "persist another Ii" in {
      val id = dao.create.save.id

      dao.load(id).setItem(dao.create.save, 1).save
      dao.load(id).setItem(dao.create.save, 1).save
      dao.load(id).setItem(dao.create.save, 1).save

      dao.load(id).items.size mustEqual 3
    }
    "persist after 1000 items" in {
      val saved = dao.create.save

      var i = 1000
      while (i > 0) {
        i -= 1
        dao.create.save
      }

      val item = dao.load(dao.create.save.id)
      dao.load(saved.id).setItem(item, 1.0).save
      dao.load(saved.id).items must havePair(item -> 1.0)
    }
  }

  step {
    dao.shutdown()
    (new File(dbPath)).delete()
  }

}