package com.voltdb;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import javafx.util.Pair;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Period;
import java.util.Stack;
import java.time.LocalDateTime;

/**
 * At creation, Vehicle starts moving from location:0 at the defined initial speed
 * Vehicle continues to move at the same speed until interrupted
 * Vehicle will update its status (loc,speed,time) at regular intervals
 * New location is calculated in terms of miles travelled since the last time
 * Vehicle will ping its status (loc,speed,time) at regular intervals
 * Vehicle will update its speed in response to SPEED command
 * Vehicle terminates in response to SHUTDOWN command
 */
public class Vehicle extends AbstractBehavior<Vehicle.Command> {
    
    private final Stack<VehiclePing> vehiclePings = new Stack<>();

    interface Command {}

    public static enum SHUTDOWN implements Command {
        INSTANCE
    }

    public static enum PING implements Command {
        INSTANCE
    }

    public static class Speed implements Command {
        public final double milesPerHour;
        public Speed(double milesPerHour) {
            this.milesPerHour = milesPerHour;
        }
    }
    
    private static class VehiclePing {
        public final double speed, loc;
        public final LocalDateTime time;
        public VehiclePing(double speed, double loc, LocalDateTime time) {
            this.speed = speed;
            this.loc = loc;
            this.time = time;
        }
    }
    
    public static Behavior<Command> create(double entrySpeed) {
        return Behaviors.withTimers(
                timers -> {
                 timers.startTimerAtFixedRate(new Speed(1.1), Duration.ofSeconds(1));
                 timers.startTimerAtFixedRate(PING.INSTANCE, Duration.ofMillis(100));
                 return Behaviors.setup(context -> new Vehicle(context, entrySpeed));
                });
    }
    
    private Vehicle(ActorContext<Vehicle.Command> context, double entrySpeed) {
        super(context);
        vehiclePings.push(vehiclePings.push(new VehiclePing(entrySpeed, 0, LocalDateTime.now())));
    }
    
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Speed.class, this::onReceive)
                .onMessage(SHUTDOWN.class, unUsed -> {return Behaviors.stopped();})
                .onMessage(PING.class, this::onPing)
                .build();
    }
    
    private Behavior<Command> onReceive(Speed speed) {
        VehiclePing curSlt = vehiclePings.peek();
        LocalDateTime curTime = curSlt.time;
        LocalDateTime nextTime = LocalDateTime.now();

        Duration duration = Duration.between(curTime, nextTime);
        double dist = duration.getSeconds()/SECS_HOUR * curSlt.speed;
        double nextLoc = curSlt.loc + dist;
        double nextSpeed = curSlt.speed+speed.milesPerHour;

        VehiclePing nextSlt = new VehiclePing(nextSpeed, nextLoc, nextTime);
        vehiclePings.push(nextSlt);
        getContext().getLog().info("{} -> {}",
                new DecimalFormat("#0.00").format(curSlt.speed),
                new DecimalFormat("#0.00").format(nextSlt.speed));
        return this;
    }

    private Behavior<Command> onPing(Command ping) {
        VehiclePing curSlt = vehiclePings.peek();
        LocalDateTime curTime = curSlt.time;
        LocalDateTime nextTime = LocalDateTime.now();
        double curSpeed = curSlt.speed;

        Duration duration = Duration.between(curTime, nextTime);
        double durationHours = duration.getSeconds()/SECS_HOUR;
        double dist = durationHours * curSpeed;
        double nextLoc = curSlt.loc + dist;

        getContext().getLog().info("{} : {}",
                dist, durationHours
//                new DecimalFormat("#0.00").format(nextLoc),
//                new DecimalFormat("#0.00").format(curSpeed)
                );
        return this;
    }

    private static final double SECS_HOUR = 3600.0;
}
