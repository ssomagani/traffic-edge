package com.voltdb;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Stack;

import com.voltdb.kafka.SimpleProducer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    static SimpleProducer PRODUCER;
    static ActorRef<Road.Command> ROAD;
    private final int id;

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
    
    public static Behavior<Command> create(int id, double entrySpeed, double entryLoc, 
    		SimpleProducer producer, ActorRef<Road.Command> road) {
    	Vehicle.PRODUCER = producer;
    	Vehicle.ROAD = road;
        return Behaviors.withTimers(
                timers -> {
//                	int speedDiff = ThreadLocalRandom.current().nextInt(20) - 10;
//                 timers.startTimerAtFixedRate(new Speed(speedDiff), Duration.ofSeconds(5));
                 timers.startTimerAtFixedRate(PING.INSTANCE, Duration.ofMillis(1000));
                 return Behaviors.setup(context -> new Vehicle(context, id, entrySpeed, entryLoc));
                });
    }
    
    private Vehicle(ActorContext<Vehicle.Command> context, int id, 
    		double entrySpeed, double entryLoc) {
        super(context);
        this.id = id;
        vehiclePings.push(new VehiclePing(entrySpeed, entryLoc, LocalDateTime.now()));
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
        double nextSpeed = speed.milesPerHour;

        VehiclePing nextSlt = new VehiclePing(nextSpeed, nextLoc, nextTime);
        vehiclePings.push(nextSlt);
        return this;
    }

    private Behavior<Command> onPing(Command ping) throws Exception {
        VehiclePing prevSlt = vehiclePings.peek();

        LocalDateTime prevTime = prevSlt.time;
        LocalDateTime curTime = LocalDateTime.now();
        double curSpeed = prevSlt.speed;

        Duration duration = Duration.between(prevTime, curTime);
        double durationHours = duration.getSeconds()/SECS_HOUR;
        double dist = durationHours * curSpeed;
        double curLoc = prevSlt.loc + dist;
        
        PRODUCER.send(PRODUCER.topicName, "" + id, id + "," + curLoc + "," + curSpeed + "," 
        + DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSS").format(LocalDateTime.now()));

        int prevSeg = (int) prevSlt.loc;
        int curSeg = (int) curLoc;
        
        if(prevSeg != curSeg) {
        	
        }
//        getContext().getLog().info("{}",
//        		id + "," + curLoc + "," + curSpeed + "," 
//        		        + DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSS").format(LocalDateTime.now())
//                );
        return this;
    }

    private static final double SECS_HOUR = 3600.0;
}
