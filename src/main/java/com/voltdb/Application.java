package com.voltdb;

import com.voltdb.kafka.SectorHealthMessageHandlerImpl;
import com.voltdb.kafka.SimpleConsumer;
import com.voltdb.kafka.TrafficMessageHandlerImpl;

import akka.actor.typed.ActorSystem;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Application {
	
    public static void main(String[] args) throws Exception {

        final ActorSystem<Road.Command> road = ActorSystem.create(Road.create(2000), "traffic");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            try {
				new SimpleConsumer().runAlways("traffic", new TrafficMessageHandlerImpl(road));
				new SimpleConsumer().runAlways("sector_health", new SectorHealthMessageHandlerImpl(road));
			} catch (Exception e) {
				e.printStackTrace();
			}
        });
        
        road.tell(new Road.Open("Rt2"));

        try {
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (IOException ignored) {
        } finally {
            road.terminate();
            executor.shutdown();
        }
    }
}
