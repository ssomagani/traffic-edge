package com.voltdb.kafka;

import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.simple.JSONObject;

import com.voltdb.Road;

import akka.actor.typed.ActorSystem;

public class PingMessageHandlerImpl implements KafkaMessageHandler{
	
	ActorSystem<Road.Command> road;
	
	public PingMessageHandlerImpl(ActorSystem<Road.Command> road) {
		this.road = road;
	}

    /**
     * The Log.
     */
    static Logger log = Logger.getLogger(PingMessageHandlerImpl.class.getName());

    @Override
    public void processMessage(String topicName, 
    		ConsumerRecord<String, String> message) 
    throws Exception {
        String source = KafkaMessageHandlerImpl.class.getName();
        JSONObject obj = MessageHelper.getMessageLogEntryJSON(source, topicName,message.key(),message.value());
        HashMap<String, String> msg = (HashMap<String, String>) obj.get("message");
//        System.out.println(msg.get("id") + " : " + msg.get("sector") + " : " + msg.get("speed"));
        log.info(msg.get(msg.get("id") + " : " + "sector") + " : " + msg.get("speed"));
//        road.tell(new Road.Speed(0));
    }
}