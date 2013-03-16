package com.owlunit.core.ii.mutable.utils

import akka.actor.{ActorRef, Props, Actor}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
class ForwardParentActor(props: Props, origin: ActorRef) extends Actor {

  val child = context.actorOf(props)
  def receive = {
    case x => if (sender == child) origin forward x else child forward x
  }


}
