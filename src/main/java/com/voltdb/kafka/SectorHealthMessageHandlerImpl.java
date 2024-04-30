package com.voltdb.kafka;

import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.simple.JSONObject;

import com.voltdb.Road;
import com.voltdb.Road.Speed;

import akka.actor.typed.ActorSystem;

public class SectorHealthMessageHandlerImpl implements KafkaMessageHandler {
	
	ActorSystem<Road.Command> road;
	
	public static final double ACCIDENT_SPEED = 30;
	public static final double REGULAR_SPEED = 30;
	
	public SectorHealthMessageHandlerImpl(ActorSystem<Road.Command> road) {
		this.road = road;
	}

    /**
     * The Log.
     */
    static Logger log = Logger.getLogger(TrafficMessageHandlerImpl.class.getName());

    @Override
    public void processMessage(String topicName, 
    		ConsumerRecord<String, String> message) 
    throws Exception {
        String source = KafkaMessageHandlerImpl.class.getName();
        JSONObject obj = MessageHelper.getMessageLogEntryJSON(source, topicName,message.key(),message.value());
        HashMap<String, String> msg = (HashMap<String, String>) obj.get("message");
        
        int sector = Integer.parseInt(msg.get("sector"));
        int traffic = Integer.parseInt(msg.get("traffic"));
        int accident = Integer.parseInt(msg.get("accient"));
        
        if(accident == 1) {
        	road.tell(new Road.Speed(ACCIDENT_SPEED, sector));
        } else {
//        	road.tell(new Road.Speed(REGULAR_SPEED, sector));
        }
        System.out.println(msg.get("sector") + " : " + msg.get("traffic"));
        log.info(msg.get("sector") + " : " + msg.get("traffic"));
//        road.tell(new Road.Speed(0));
    }
}
