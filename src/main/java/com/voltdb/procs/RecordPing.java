package com.voltdb.procs;

import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.VoltTable;

public class RecordPing extends VoltProcedure {
	
	private static final SQLStmt INSERT_PING = new SQLStmt("insert into pings values (?, ?, ?, ?, ?)");
	private static final SQLStmt UPSERT_POS = new SQLStmt("upsert into positions values (?, ?, ?, ?, ?)");

	public VoltTable run(int id, double location, double speed, String time) {
		
		int sector = (int) (location/10);

		voltQueueSQL(INSERT_PING, id, location, sector, speed, time);
		voltQueueSQL(UPSERT_POS, id, location, sector, speed, time);
		voltExecuteSQL();
		return null;
	}
}
