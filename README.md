# Traffic Edge

## Run instructions
Install Gradle and simply invoke `gradle run` in this directory

## Functionality

It's a simple application but how this differs from every other Volt demo application is that there's a feedback loop from the Volt back into the client so that we can demostrate the benefits of Volt's processing power using a completed loop.

## Client 

The client uses Akka actors that not only generate data to publish to the server but can also consume messages to adapt their behavior

## Server

Volt functions as both a stream processor and a stream persistence.
