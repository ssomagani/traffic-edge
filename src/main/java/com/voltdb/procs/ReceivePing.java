package com.voltdb.procs;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

public class ReceivePing extends VoltCompoundProcedure {
	
	int id;
	double location, speed;
	String time;

	public VoltTable[] run(int id, double location, double speed, String time) {
		this.id = id;
		this.location = location;
		this.speed = speed;
		this.time = time;
		
		this.newStageList(this::recordPing)
		.then(this::evalDriver)
		.then(this::finish)
		.build();
		return null;
	}
	
	private void recordPing(ClientResponse[] resp) {
		queueProcedureCall("RecordPing", id, location, speed, time);
	}

	private void evalDriver(ClientResponse[] resp) {
		queueProcedureCall("EvalDriver", id, location, speed, time);
	}
	
	private void finish(ClientResponse[] resp) {
		System.out.println(resp[0].getStatusString());
		this.completeProcedure(0);
	}
}
