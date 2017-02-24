package org.openbw.tsbw.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openbw.tsbw.Constants;

public class PPF2 {

	private static final Logger logger = LogManager.getLogger();
	
	private static final int SCV_BUILD_TIME = 300;
	private static final int SCV_COST = 50;
	private static final int START_SCVS = 4;
	private static final int INITIAL_MINING_DELAY = 120;

	// roundtrip times (measured) lost temple 9 o'clock main
	//private static double[] PATCHES = new double[]{160, 178, 178, 182, 182, 182, 182, 205};
	// roundtrip times (measured) lost temple 9 oclock natural
	//private static double[] PATCHES = new double[]{177, 180, 183, 183, 185, 185, 190, 197}; 
	
// Fighting Spirit mains
	private static double[] PATCHES = new double[]{151.52, 163.93, 172.41, 175.44, 175.44, 178.57, 178.57, 181.82, 181.82};
//  x/y distances (11 o'clock / 8 o'clock and 2 o'clock / 5 o'clock)
	// patches = {-192/-96, -224/-64, -192/-32, -224/0, -192/32, -224/64, -192/96, -192/128, -160/160}
	// patches = {192/-96, 224/-64, 192/-32, 224/0, 192/32, 224/64, 192/96, 192/128, 160/160}
	
// Lost Temple 9 o'clock main
//	int[] patches = new int[]{102, 121, 121, 121, 134, 134, 134, 138}; // distances main
//	int[] patches = new int[]{102, 102, 109, 109, 122, 134, 134, 134}; // distances natural
	
	private static double calculateMiningRate(int scvs) {
		
		double[] assignedScvs = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		
		for (int scv = 1; scv <= scvs; scv++) {
			int patch = 0;
			double maxFactor = 0.0;
			for (int i = 0; i < PATCHES.length; i++) {
				
				// the rate includes a penalty of 8 frames for each additional SCV assigned to a patch due to "friction": random waiting in queue
				double friction = assignedScvs[i]* (Math.pow(scv, 1.215) - 8.5);
				double rate = Math.min((assignedScvs[i]+1) * 8.0/(PATCHES[i] + friction), 8/Constants.MINING_TIME);
				double factor = rate / (assignedScvs[i]+1);
				if (factor > maxFactor) {
					maxFactor = factor;
					patch = i;
				}
			}
			assignedScvs[patch] += 1;
		}
		
		double miningRate = 0;
		for (int i = 0; i < assignedScvs.length; i++) {
			double friction = assignedScvs[i]*(Math.pow(scvs, 1.215) - 8.5);
			miningRate += Math.min(assignedScvs[i] * 8.0/(PATCHES[i] + friction), 8/Constants.MINING_TIME);
		}
		return miningRate;
	}

	public static int calculateIncome(int time, int maxScvs) {
		
		return calculateIncome(time, maxScvs, new int[0][0]);
	}
	
	public static int calculateIncome(int time, int maxScvs, int[][] waypoints) {
		
		if (time < 15 && maxScvs == START_SCVS) {
			return Constants.INCOME_0;
		} else if (time < 15 && maxScvs > START_SCVS) {
			return 0;
		}
		
		int gatheredMinerals = calculateIncome(time, Constants.INCOME_0, START_SCVS, maxScvs, waypoints);
		
		return gatheredMinerals;
	}
	
	public static int calculateIncome(int time, int initialMinerals, int startScvs, int endScvs) {
		
		return calculateIncome(time, initialMinerals, startScvs, endScvs, new int[0][0]);
	}
	
	public static int calculateIncome(int time, int initialMinerals, int startScvs, int endScvs, int[][] waypoints) {
		
		double gatheredMinerals =  initialMinerals;
		double elapsedTime = 0.0;
		double previousTime = INITIAL_MINING_DELAY;
		int currentScvs;
		
		for (currentScvs = startScvs; currentScvs < endScvs && elapsedTime <= time; currentScvs++) {
		
			//System.out.println("It is now time " + elapsedTime + " and I have gathered " + gatheredMinerals + " so far.");
			
			double miningRate = calculateMiningRate(currentScvs);
			double miningTime;
			
			miningTime = Math.max(0, (int)((SCV_COST - gatheredMinerals) / miningRate / 8) * 8); // that's how long I need to gather 50 minerals
			//if (miningTime > 0.0) {
				//System.out.println("I don't have the required minerals just yet, gotta mine for an additional " + miningTime + " frames.");
			//}
			gatheredMinerals -= SCV_COST; // pay the SCV cost
				
			gatheredMinerals += miningRate * miningTime;
			double newScvTime = elapsedTime + miningTime;
			//System.out.println("I could start producing the SCV at time " + newScvTime);
			
			elapsedTime = newScvTime + SCV_BUILD_TIME;
			gatheredMinerals += miningRate * (elapsedTime - previousTime);
			previousTime = elapsedTime + 40;
			for (int i = 0; i < waypoints.length; i++) {
				if (currentScvs == waypoints[i][0] - 1) {
					miningTime = Math.max(0, (waypoints[i][1] - gatheredMinerals) / miningRate);
					gatheredMinerals -= waypoints[i][1];
					gatheredMinerals += miningRate * miningTime;
					elapsedTime += miningTime;
				}
			}
		}
		gatheredMinerals += calculateMiningRate(currentScvs) * (time - elapsedTime);
		
		//substract all lost mineral time
		for (int i = 0; i < waypoints.length; i++) {
			double miningRate = calculateMiningRate(waypoints[i][0]);
			gatheredMinerals -=  miningRate * waypoints[i][2] / waypoints[i][0]; 
		}
		
		return (int)gatheredMinerals;
	}
	
	public static int calculateMaxIncome(int time, boolean net) {
		return calculateMaxIncome(time, new int[0][0], net);
	}
	
	/**
	 * Calculates the maximum possible income at given time without assuming constant SCV production.
	 * @param time
	 * @param net
	 * @return
	 */
	public static int calculateMaxIncome(int time, int[][] waypoints, boolean net) {
		
		double delay = time > 6*SCV_BUILD_TIME ? 20 : 0;
		int maxScvs = (int)(START_SCVS + (time - delay) / SCV_BUILD_TIME);
		
		int income = 0;
		int scvsAtMax = 4;
		for (int i = 4; i < maxScvs; i++) {
			int currentIncome = calculateIncome(time, i, waypoints);
			if (currentIncome > income) {
				income = currentIncome;
				scvsAtMax = i;
			}
		}
		logger.debug("max at {} / {} SCVs", scvsAtMax, maxScvs);
		return income;
	}
	
	public static double calculateEstimatedMining(int time, int scvs) {
		return time * calculateMiningRate(scvs);
	}
	
	private static void log(int time, int scvCap, int income1, int income2) {
		logger.error("calc: income at {} frames with SCV cap at {}: {}", time, scvCap, income1);
		logger.error("test: income at {} frames with SCV cap at {}: {}", time, scvCap, income2);
		if (income1 != income2) {
			System.out.println("test failed.");
		}
	}
	private static void log(int time, int scvFrom, int scvTo, int income1, int income2) {
		logger.error("calc: income during {} frames with SCVs {} to {}: {}", time, scvFrom, scvTo, income1);
		logger.error("test: income during {} frames with SCVs {} to {}: {}", time, scvFrom, scvTo, income2);
		if (income1 != income2) {
			System.out.println("test failed.");
		}
	}
	
	private static void runTestCase1() {
		
		int time = 300;
		int income1 = PPF2.calculateIncome(time, 4);
		int income2 = Constants.INCOME_0 + (int)(calculateMiningRate(4) * (time - 15));
		log(time, 4, income1, income2);
		
		time = 300;
		income1 = PPF2.calculateIncome(time, 5);
		income2 = Constants.INCOME_0 + (int)(calculateMiningRate(4) * (time - 15));
		log(time, 5, income1, income2);
		
		time = 400;
		income1 = PPF2.calculateIncome(time, 5);
		income2 = Constants.INCOME_0 + (int)(calculateMiningRate(4) * 285 + calculateMiningRate(5) * 100);
		log(time, 5, income1, income2);
		
		time = 676;
		income1 = PPF2.calculateIncome(time, 5);
		income2 = Constants.INCOME_0 + (int)(calculateMiningRate(4) * 285 + calculateMiningRate(5) * (299 + 77) - 50);
		log(time, 6, income1, income2);
		
		time = 677;
		income1 = PPF2.calculateIncome(time, 6);
		income2 = Constants.INCOME_0 + (int)(calculateMiningRate(4) * 285 + calculateMiningRate(5) * (300 + 77) - 100);
		log(time, 6, income1, income2);
		
		time = 678;
		income1 = PPF2.calculateIncome(time, 6);
		income2 = Constants.INCOME_0 + (int)(calculateMiningRate(4) * 285 + calculateMiningRate(5) * (301 + 77) - 100);
		log(time, 6, income1, income2);
		
		time = 1845;
		income1 = PPF2.calculateIncome(time, 10);
		income2 = 270;
		log(time, 10, income1, income2);
		
		time = 1845;
		income1 = PPF2.calculateIncome(time, 10);
		income2 = 570;
		log(time, 10, income1, income2);
		
		time = 2145;
		income1 = PPF2.calculateIncome(time, 11);
		income2 = 351;
		log(time, 10, income1, income2);
		
		time = 400;
		income1 = PPF2.calculateIncome(time, 0, 6, 6);
		income2 = (int)(calculateMiningRate(6) * time);
		log(time, 6, 6, income1, income2);
		
		time = 400;
		income1 = PPF2.calculateIncome(time, 50, 6, 7);
		income2 = (int)(calculateMiningRate(6) * (time - 100) + calculateMiningRate(7) * 100) + 50;
		log(time, 6, 7, income1, income2);
		
		System.out.println("test cases: done");
	}
	
	private static void log(int time, int scvCap, int income, int[][] waypoints) {
		logger.error("income during {} frames, cap at {} workers, barracks at {} supply: {}", time, scvCap, waypoints[0][0], income);
	}
	
	private static void runTestCase2() {
		
		int income1;
		int time = 3000;
		int[][] waypoints = new int[1][3];
		waypoints[0][1] = 150;
		waypoints[0][2] = 1200 + 144;
		
		income1 = PPF2.calculateIncome(time, 10, new int[0][0]);
		
		waypoints[0][0] = 4;
		income1 = PPF2.calculateIncome(time, 10, waypoints);
		log(time, 10, income1, waypoints);
		
		waypoints[0][0] = 5;
		income1 = PPF2.calculateIncome(time, 10, waypoints);
		log(time, 10, income1, waypoints);
		
		waypoints[0][0] = 6;
		income1 = PPF2.calculateIncome(time, 10, waypoints);
		log(time, 10, income1, waypoints);
		
		waypoints[0][0] = 7;
		income1 = PPF2.calculateIncome(time, 10, waypoints);
		log(time, 10, income1, waypoints);
		
		waypoints[0][0] = 8;
		income1 = PPF2.calculateIncome(time, 10, waypoints);
		log(time, 10, income1, waypoints);
		
		waypoints[0][0] = 9;
		income1 = PPF2.calculateIncome(time, 10, waypoints);
		log(time, 10, income1, waypoints);
		
		waypoints[0][0] = 10;
		income1 = PPF2.calculateIncome(time, 10, waypoints);
		log(time, 10, income1, waypoints);
		
		System.out.println("test cases: done");
	}
	
	public static void main(String[] args) throws Exception {
		
		System.out.println(calculateMiningRate(18));
		runTestCase1();
		
		runTestCase2();
	}
}
