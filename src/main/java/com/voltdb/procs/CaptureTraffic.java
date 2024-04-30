package com.voltdb.procs;

import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.VoltTable;

public class CaptureTraffic extends VoltProcedure {

	private final SQLStmt SELECT_TRAFFIC_BY_SECTOR = new SQLStmt("select sector, traffic from cars_by_sector");
	private final SQLStmt INSERT_TRAFFIC_STREAM = new SQLStmt("insert into traffic values (?, ?)");
	
	public VoltTable[] run() {
		
		voltQueueSQL(SELECT_TRAFFIC_BY_SECTOR);
		VoltTable trafficTable = voltExecuteSQL()[0];
		while(trafficTable.advanceRow()) {
			int sector = (int) trafficTable.getLong(0);
			int traffic = (int) trafficTable.getLong(1);
			voltQueueSQL(INSERT_TRAFFIC_STREAM, sector, traffic);
		}
		return voltExecuteSQL(true);
	}
}
