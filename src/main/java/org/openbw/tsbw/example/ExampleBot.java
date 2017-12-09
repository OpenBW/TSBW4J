package org.openbw.tsbw.example;

import org.openbw.tsbw.Bot;
import org.openbw.tsbw.example.scouting.DefaultScoutingFactory;
import org.openbw.tsbw.example.strategy.BuildOrderStrategy;
import org.openbw.tsbw.example.strategy.DummyStrategy;
import org.openbw.tsbw.strategy.ScoutingFactory;

public class ExampleBot extends Bot {

	public ExampleBot(ScoutingFactory scoutingFactory) {
		
		super(scoutingFactory);
	}
	
	public static void main(String[] args) throws Exception {
		
		ScoutingFactory scoutingFactory = new DefaultScoutingFactory();
		
		
		ExampleBot exampleBot = new ExampleBot(scoutingFactory);
		
		// run the bot. It will search for a game lobby to join.
		exampleBot.run();
	}

	@Override
	public void onStart() {

		this.strategyFactory.register("dummy", new DummyStrategy());
		this.strategyFactory.register("buildorder", new BuildOrderStrategy());
		
		// try changing the Type from "dummy" to "buildorder" to use a different strategy
		this.gameStrategy = strategyFactory.getStrategy("dummy");
		
		this.interactionHandler.enableUserInput();
		this.interactionHandler.setLocalSpeed(40);
	}
}
