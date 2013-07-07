package com.owlunit.core.ii.mutable.utils

import java.util.UUID
import com.owlunit.core.ii.mutable.{Ii, IiDao}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
trait IiHelpers {

  def getRandomIi(dao: IiDao): Ii = dao.load(dao.create.save.id).get
  def createIi(name: String, dao: IiDao): Ii = dao.load(dao.create.setMeta("name", name).save.id).get

  def randomString = UUID.randomUUID().toString
  def randomKeyValue: (String, String) = ("key-%s" format randomString, "value-%s" format randomString)

}