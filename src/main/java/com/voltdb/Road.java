package com.voltdb;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Road extends AbstractBehavior<Road.Command> {
    
    private final int length;

    public interface Command {}
    
    public static class Open implements Command {
        public final String name;

        public Open(String name) {
            this.name = name;
        }
    }

    private final ActorRef<Vehicle.Command> vehicle;
    
    public static Behavior<Command> create(int length) {
        return Behaviors.setup(context -> new Road(context, length));
    }
    
    private Road(ActorContext<Command> context, int length) {
        super(context);
        this.length = length;
        vehicle = context.spawn(Vehicle.create(55), "vehicle");
    }
    
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Command.class, this::onOpen)
                .build();
    }
    
    private Behavior<Command> onOpen(Command road) {
        getContext().getLog().info("Opening road {}!", ((Open)road).name);
        return this;
    }
}
