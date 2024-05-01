# Traffic Edge

## Run instructions
Install Gradle and simply invoke `gradle run` in this directory

## Functionality

It's a simple application but how this differs from every other Volt demo application is that there's a feedback loop from the Volt back into the client so that we can demostrate the benefits of Volt's processing power using a completed loop.

### Design
The client application publishes different event streams and also consumes event streams to update its behavior. Specifically, the vehicles emit PING events (one event per second) about their location, speed, and other metadata. The vehicles can also consume events that give them the target speed to adjust themselves to. 

**Road** ---creates--> **Vehicles** ---emit events--> **Volt** ---aggregates, processes, emits events --> **Archive**
**Vehicles** ---emit events--> **Volt** ---emits events--> **Vehicles**

### Client 

The client uses Akka actors that not only generate data to publish to the server but can also consume messages to adapt their behavior

The **Road** actor creates the **Vehicle** actors
The **Vehicle** actors are instantiated with a Kafka producer so that they can publish their events. A timer sends a ping message to each vehicle which causes it to publish its pings.

### Server

Volt functions as both a stream processor and a stream persistence.
Volt serves as the broker for event streams published from the client, processes the events, (can be the transactional store for the events), aggregator of the events, and publisher of new events that the client subscribes to.

### UI

The D3 UI currently is just a mockup of cars moving on the road but will be an excellent way to show the feedback loop and visualize the optimization of a KPI like the average speed on the road.

## TODO

1. Integrate MQTT to convert this into a viable IoT demo
2. Implement toll charging functionality to show transactional consistency
3. Hook up the event streams to UI 
