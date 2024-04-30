package com.voltdb.procs;

import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.VoltTable;

public class SectorHealthCheck extends VoltProcedure {

	private static final SQLStmt SELECT_TRAFFIC_BY_SECTOR = new SQLStmt("select sector, traffic from cars_by_sector where sector=?");
	private static final SQLStmt CHECK_MAX = new SQLStmt("select max(speed) from positions where sector=?");
	private static final SQLStmt GET_ACCIDENT_SPEED = new SQLStmt("select accident_speed from sectors where sector=?");
	private static final SQLStmt INSERT_STREAM = new SQLStmt("insert into sector_health values (?, ?, ?)");

	public long run() {
		for(int sector=0; sector<10; sector++) {
			
			voltQueueSQL(CHECK_MAX, sector);
			voltQueueSQL(GET_ACCIDENT_SPEED, sector);
			voltQueueSQL(SELECT_TRAFFIC_BY_SECTOR, sector);
			VoltTable[] results = voltExecuteSQL();
			VoltTable max_result = results[0];
			VoltTable accident_result = results[1];
			VoltTable traffic_result = results[2];

			int accidentFlag = 0;
			while(max_result.advanceRow()) {
				double maxSpeed = max_result.getDouble(0);
				if(accident_result.advanceRow()) {
					double accidentSpeed = accident_result.getDouble(0);
					if(maxSpeed < accidentSpeed) {
						accidentFlag = 1;
					}
				}
			}

			if(traffic_result.advanceRow()) {
				int traffic = (int) traffic_result.getLong(1);
				voltQueueSQL(INSERT_STREAM, sector, traffic, accidentFlag);
				voltExecuteSQL(true);
			}
		}
		return 0;
	}
}
