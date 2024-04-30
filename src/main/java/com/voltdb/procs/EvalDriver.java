package com.voltdb.procs;

import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.VoltTable;

public class EvalDriver extends VoltProcedure {

	private static final SQLStmt SELECT_AVG_SPEED = new SQLStmt("select avg(speed) from pings where id=? and sector=?");
	private static final SQLStmt SELECT_SECTOR = new SQLStmt("select speed_limit, toll_rate from sectors where sector=?");
	private static final SQLStmt INSERT_CONTROL = new SQLStmt("insert into control values (?, ?)");
	
	public VoltTable[] run(int id, int sector) {
		voltQueueSQL(SELECT_AVG_SPEED, id, sector);
		voltQueueSQL(SELECT_SECTOR, sector);
		
		VoltTable[] responses = voltExecuteSQL();
		VoltTable avgTable = responses[0];
		if(avgTable.advanceRow()) {
			double avgSpeed = avgTable.getDouble(0);
			VoltTable speedTable = responses[1];
			if(speedTable.advanceRow()) {
				double speedLimit = speedTable.getDouble(0);
				double tollRate = speedTable.getDouble(1);
				double speedDiff = avgSpeed - speedLimit;
				if(speedDiff > 0) {
					voltQueueSQL(INSERT_CONTROL, id, speedLimit-speedDiff);
					voltExecuteSQL();
				}
			}
			
		}
		return null;
	}
}
