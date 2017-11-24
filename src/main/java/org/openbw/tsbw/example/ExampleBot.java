package org.openbw.tsbw.example;

import org.openbw.tsbw.Bot;
import org.openbw.tsbw.example.mining.DefaultMiningFactory;
import org.openbw.tsbw.example.scouting.DefaultScoutingFactory;
import org.openbw.tsbw.example.strategy.DefaultStrategyFactory;
import org.openbw.tsbw.example.strategy.DefaultStrategyFactory.Type;
import org.openbw.tsbw.strategy.MiningFactory;
import org.openbw.tsbw.strategy.ScoutingFactory;
import org.openbw.tsbw.strategy.StrategyFactory;

public class ExampleBot extends Bot {

	public ExampleBot(MiningFactory miningFactory, ScoutingFactory scoutingFactory, StrategyFactory strategyFactory) {
		super(miningFactory, scoutingFactory, strategyFactory);
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
		StrategyFactory strategyFactory = new DefaultStrategyFactory(Type.BUILD_ORDER);
		
		ExampleBot exampleBot = new ExampleBot(miningFactory, scoutingFactory, strategyFactory);
		
		// run the bot. It will search for a game lobby to join.
		exampleBot.run();
	}

	@Override
	public void onStart() {

		interactionHandler.enableUserInput();
		interactionHandler.setLocalSpeed(40);
	}
}
