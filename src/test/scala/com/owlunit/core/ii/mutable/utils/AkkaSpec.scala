package com.owlunit.core.ii.mutable.utils

import akka.testkit.{TestKit, ImplicitSender}
import org.specs2.mutable.After
import akka.actor.ActorSystem

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         A tiny class that can be used as a Specs2 'context'.
 */
abstract class AkkaSpec extends TestKit(ActorSystem()) with After with ImplicitSender {

  // make sure we shut down the actor system after all tests have run
  def after {
    system.shutdown()
  }

}