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
    "have meta after setMeta" in {
      val ii = dao.create.setMeta("key", "value")
      ii.meta must havePair("key" -> "value")
    }
    "have item after setItem" in {
      val item = dao.create.save
      val ii = dao.create.setItem(item, 1.0)
      ii.items must havePair(item -> 1.0)
    }
    "loose meta after removeMeta" in {
      val ii = dao.create.setMeta("key", "value").removeMeta("key")
      ii.meta must not haveKey("key")
    }
    "loose item after removeItem" in {
      val item = dao.create.save
      val ii = dao.create.setItem(item, 1.0).removeItem(item)
      ii.items must not haveKey(item)
    }
  }

  "Saved Ii" should {
    "have not 0 id" in {
      val ii = dao.create.save
      ii.id mustNotEqual 0
    }
    "persist meta" in {
      val saved = dao.create.setMeta("key", "value").save
      val loaded = dao.load(saved.id)
      loaded.meta must havePair("key" -> "value")
    }
    "persist item" in {
      val component = dao.create.save
      val saved = dao.create.setItem(component, 1.0).save
      val loaded = dao.load(saved.id)
      loaded.items must havePair(component -> 1.0)
    }
    "loose meta after removeMeta" in {
      val saved = dao.create.setMeta("key", "value").save
      saved.removeMeta("key").save
      val loaded = dao.load(saved.id)
      loaded.meta must not haveKey("key")
    }
    "loose item after removeItem" in {
      val component = dao.create.save
      val saved = dao.create.setItem(component, 1.0).save
      saved.removeItem(component).save
      val loaded = dao.load(saved.id)
      loaded.items must not haveKey(component)
    }
    "be loadable by meta" in {
      val saved = dao.create.setMeta("key", "value").save
      val loaded = dao.load("key", "value")
      loaded must contain(saved)
    }
    "be loadable by id" in {
      val saved = dao.create.save
      val loaded = dao.load(saved.id)
      saved.id mustEqual loaded.id
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
    "persist another Ii's component" in {
      val item1 = dao.create.save
      val item2 = dao.create.save

      val saved = dao.create.setItem(item1, 1.0).save
      val loaded = dao.load(saved.id).setItem(item2, 1.0).save
      dao.load(loaded.id).items must havePair(item2 -> 1.0)
    }
    "persist after 100000 items" in {
      val saved = dao.create.save

      var i = 100000
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