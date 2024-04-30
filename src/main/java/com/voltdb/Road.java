package com.voltdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import com.voltdb.kafka.SimpleProducer;

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
    
    public static class Speed implements Command {
        public final double perHour;
        public final int segment;
        public Speed(double perHour, int segment) {
            this.perHour = perHour;
            this.segment = segment;
        }
    }
    
    public static class SegUpdate implements Command {
    	public final int segment;
    	public final int vehicleId;
    	public SegUpdate(int segment, int vehicleId) {
    		this.segment = segment;
    		this.vehicleId = vehicleId;
    	}
    }

    private static final ArrayList<HashMap<Integer, ActorRef<Vehicle.Command>>> vehicles = new ArrayList<>();
    static {
    	vehicles.add(new HashMap<Integer, ActorRef<Vehicle.Command>>());
    	vehicles.add(new HashMap<Integer, ActorRef<Vehicle.Command>>());
    	vehicles.add(new HashMap<Integer, ActorRef<Vehicle.Command>>());
    	vehicles.add(new HashMap<Integer, ActorRef<Vehicle.Command>>());
    	vehicles.add(new HashMap<Integer, ActorRef<Vehicle.Command>>());
    	vehicles.add(new HashMap<Integer, ActorRef<Vehicle.Command>>());
    	vehicles.add(new HashMap<Integer, ActorRef<Vehicle.Command>>());
    	vehicles.add(new HashMap<Integer, ActorRef<Vehicle.Command>>());
    	vehicles.add(new HashMap<Integer, ActorRef<Vehicle.Command>>());
    	vehicles.add(new HashMap<Integer, ActorRef<Vehicle.Command>>());
    }
    
    public static Behavior<Command> create(int length) {
        return Behaviors.setup(context -> new Road(context, length));
    }
    
    private Road(ActorContext<Command> context, int length) throws Exception {
        super(context);
        this.length = length;
        SimpleProducer producer = new SimpleProducer("pings");
        IntStream.range(0, 100000).forEach(
        		(i) -> {
        			int speed = ThreadLocalRandom.current().nextInt(50) + 65;
        			double entryLoc = ThreadLocalRandom.current().nextDouble(10.0);
        			int entrySeg = (int) entryLoc;
        			HashMap<Integer, ActorRef<Vehicle.Command>> segVehicles = vehicles.get(entrySeg);
        			segVehicles.put(i, context.spawn(Vehicle.create(i, speed, entryLoc, producer, this.getContext().getSelf()), "vehicle"+i));
        		}
        		);
    }
    
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Open.class, this::onOpen)
                .onMessage(Speed.class, this::onSpeed)
                .onMessage(SegUpdate.class, this::onSegUpdate)
                .build();
    }
    
    private Behavior<Command> onSegUpdate(SegUpdate update) {
    	int oldSeg = update.segment-1;
    	ActorRef<Vehicle.Command> vehicle = vehicles.get(oldSeg).remove(update.vehicleId);
    	vehicles.get(update.segment).put(update.vehicleId, vehicle);
    	return this;
    }
    
    private Behavior<Command> onOpen(Command road) {
        getContext().getLog().info("Opening road {}!", ((Open)road).name);
        return this;
    }
    
    private Behavior<Command> onSpeed(Speed speed) {
    	Vehicle.Speed speedToSet = new Vehicle.Speed(speed.perHour);
    	vehicles.get(speed.segment).forEach((id, vehicle) -> vehicle.tell(speedToSet));
        return this;
    }
}
