package com.voltdb;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

public class ReceiveJunction extends AbstractBehavior<Vehicle> {

    public ReceiveJunction(ActorContext<Vehicle> context) {
        super(context);
    }

    @Override
    public Receive<Vehicle> createReceive() {
        return newReceiveBuilder().onMessage(Vehicle.class, this::onReceive).build();
    }

    public Behavior<Vehicle> onReceive(Vehicle vehicle) {
        getContext().getLog().info("Received {}", vehicle);

        return this;
    }
}
