package org.openbw.tsbw.example;

import org.openbw.tsbw.Bot;
import org.openbw.tsbw.example.mining.DefaultMiningFactory;
import org.openbw.tsbw.example.scouting.DefaultScoutingFactory;
import org.openbw.tsbw.example.strategy.BuildOrderStrategy;
import org.openbw.tsbw.example.strategy.DummyStrategy;
import org.openbw.tsbw.strategy.MiningFactory;
import org.openbw.tsbw.strategy.ScoutingFactory;

public class ExampleBot extends Bot {

	public ExampleBot(MiningFactory miningFactory, ScoutingFactory scoutingFactory) {
		super(miningFactory, scoutingFactory);
	}
	
	public static void main(String[] args) throws Exception {
		
		/*
		 * To create a bot an let it play, we need to feed it factories for:
		 *  - mining
		 *  - scouting
		 *  - game strategy
		 *  
		 *  The default factories used here return simple default strategies.
		 *  Feel free to provide your own factories which produce more sophisticated strategies.
		 *  
		 *  The reason factories are used instead of just providing the strategies directly, is to enable switching of
		 *  strategies at runtime. This can be convenient when debugging, machine learning, or even as a in-game feature
		 *  to e.g. completely swap out a strategy depending on scouting information or game situation.
		 */
		MiningFactory miningFactory = new DefaultMiningFactory();
		ScoutingFactory scoutingFactory = new DefaultScoutingFactory();
		
		// try changing the Type from DUMMY to BUILD_ORDER to use a different strategy
		ExampleBot exampleBot = new ExampleBot(miningFactory, scoutingFactory);
		
		// run the bot. It will search for a game lobby to join.
		exampleBot.run();
	}

	@Override
	public void onStart() {

		this.strategyFactory.register("dummy", new DummyStrategy());
		this.strategyFactory.register("bo", new BuildOrderStrategy());
		this.gameStrategy = strategyFactory.getStrategy("dummy");
		
		this.interactionHandler.enableUserInput();
		this.interactionHandler.setLocalSpeed(20);
	}
}
