package org.openbw.tsbw.example;

import org.openbw.tsbw.Bot;
import org.openbw.tsbw.example.strategy.BuildOrderStrategy;
import org.openbw.tsbw.example.strategy.DummyStrategy;

public class ExampleBot extends Bot {

	public static void main(String[] args) throws Exception {
		
		ExampleBot exampleBot = new ExampleBot();
		
		// run the bot. It will search for a game lobby to join.
		exampleBot.run();
	}

	@Override
	public void onStart() {

		this.strategyFactory.register("dummy", new DummyStrategy());
		this.strategyFactory.register("buildorder", new BuildOrderStrategy());
		
		// try changing the Type from "dummy" to "buildorder" to use a different strategy
		this.gameStrategy = strategyFactory.getStrategy("buildorder");
		
		this.interactionHandler.enableUserInput();
		this.interactionHandler.setLocalSpeed(20);
	}
}
