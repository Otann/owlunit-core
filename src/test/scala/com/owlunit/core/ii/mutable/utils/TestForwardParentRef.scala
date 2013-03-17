package com.owlunit.core.ii.mutable.utils

import akka.actor.{ActorRef, ActorSystem, Props, Actor}
import akka.testkit.TestActorRef

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
object TestForwardParentRef {

  def apply(props: Props)(implicit system: ActorSystem, origin: ActorRef): ActorRef =
    system.actorOf(Props(new ForwardParentActor(props, origin)))

}
