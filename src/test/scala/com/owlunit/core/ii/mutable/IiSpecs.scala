package com.owlunit.core.ii.mutable

import org.specs2.mutable.Specification
import com.owlunit.core.ii.NotFoundException

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class IiSpecs extends Specification {

  var dao: IiDao = null
  step { dao = IiDao.local("target/db") }

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
    "have lost meta after removeMeta" in {
      val ii = dao.create.setMeta("key", "value").removeMeta("key")
      ii.meta must not haveKey("key")
    }
    "have lost item after removeItem" in {
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
    "have persisted meta" in {
      val saved = dao.create.setMeta("key", "value").save
      val loaded = dao.load(saved.id)
      loaded.meta must havePair("key" -> "value")
    }
    "have persisted item" in {
      val component = dao.create.save
      val saved = dao.create.setItem(component, 1.0).save
      val loaded = dao.load(saved.id)
      loaded.items must havePair(component -> 1.0)
    }
    "have persisted meta lost after removeMeta" in {
      val saved = dao.create.setMeta("key", "value").save
      saved.removeMeta("key").save
      val loaded = dao.load(saved.id)
      loaded.meta must not haveKey("key")
    }
    "have persisted item lost after removeItem" in {
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

  step { dao.shutdown() }

}