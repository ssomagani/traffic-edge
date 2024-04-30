package com.voltdb.infra;

import com.voltdb.Start;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

public class Traffic extends AbstractBehavior<Start> {

    public Traffic(ActorContext<Start> context) {
        super(context);
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
