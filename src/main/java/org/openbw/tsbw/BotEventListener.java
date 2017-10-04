package org.openbw.tsbw;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.BWEventListener;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Unit;

/* default */ class BotEventListener implements BWEventListener {
	
	private static final Logger logger = LogManager.getLogger();

	private Bot bot;
	
	/* default */ BotEventListener(Bot bot) {
		
		this.bot = bot;
	}
	
	@Override
	public void onEnd(boolean win) {
		try {
			bot.onEnd(win);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onFrame() {
		try {
			bot.onFrame();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onNukeDetect(Position nukePosition) {
		try {
			bot.onNukeDetect(nukePosition);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onPlayerLeft(Player player) {
		try {
			bot.onPlayerLeft(player);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onReceiveText(Player player, String text) {
		try {
			bot.onReceiveText(player, text);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onSaveGame(String name) {
		try {
			bot.onSaveGame(name);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onSendText(String text) {
		try {
			bot.onSendText(text);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onStart() {
		try {
			bot.internalOnStart();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onUnitComplete(Unit unit) {
		try {
			bot.onUnitComplete(unit);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onUnitCreate(Unit unit) {
		try {
			bot.onUnitCreate(unit);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onUnitDestroy(Unit unit) {
		try {
			bot.onUnitDestroy(unit);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onUnitDiscover(Unit unit) {
		try {
			bot.onUnitDiscover(unit);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onUnitEvade(Unit unit) {
		try {
			bot.onUnitEvade(unit);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onUnitHide(Unit unit) {
		try {
			bot.onUnitHide(unit);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onUnitMorph(Unit unit) {
		try {
			bot.internalOnUnitMorph(unit);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onUnitRenegade(Unit unit) {
		try {
			bot.onUnitRenegade(unit);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void onUnitShow(Unit unit) {
		try {
			bot.onUnitShow(unit);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

}
