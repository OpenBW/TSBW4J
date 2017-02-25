package org.openbw.tsbw.example;

import org.openbw.tsbw.Main;
import org.openbw.tsbw.example.mining.DefaultMiningFactory;
import org.openbw.tsbw.example.scouting.DefaultScoutingFactory;
import org.openbw.tsbw.example.strategy.DefaultStrategyFactory;
import org.openbw.tsbw.strategy.MiningFactory;
import org.openbw.tsbw.strategy.ScoutingFactory;
import org.openbw.tsbw.strategy.StrategyFactory;

public class ExampleBot extends Main {

	public ExampleBot(MiningFactory miningFactory, ScoutingFactory scoutingFactory, StrategyFactory strategyFactory) {
		super(miningFactory, scoutingFactory, strategyFactory);
	}
	public static void main(String[] args) throws Exception {
		
		MiningFactory miningFactory = new DefaultMiningFactory();
		ScoutingFactory scoutingFactory = new DefaultScoutingFactory();
		StrategyFactory strategyFactory = new DefaultStrategyFactory();
		
		ExampleBot exampleBot = new ExampleBot(miningFactory, scoutingFactory, strategyFactory);
		
		exampleBot.initialize();
		exampleBot.run();
	}
}
