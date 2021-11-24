package com.voltdb;

import akka.actor.typed.ActorSystem;
import java.io.IOException;

public class Application {

    public static void main(String[] args) {

        final ActorSystem<Road.Command> road = ActorSystem.create(Road.create(2000), "traffic");

        road.tell(new Road.Open("Rt2"));

        try {
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (IOException ignored) {
        } finally {
            road.terminate();
        }
    }
}

