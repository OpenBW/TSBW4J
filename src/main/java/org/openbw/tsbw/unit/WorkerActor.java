package org.openbw.tsbw.unit;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.Constants;
import org.openbw.tsbw.analysis.PPF2;
import org.openbw.tsbw.building.ConstructionType;
import org.openbw.tsbw.micro.AttackUnitCommand;
import org.openbw.tsbw.micro.BuildMessage;
import org.openbw.tsbw.micro.Command;
import org.openbw.tsbw.micro.ConstructCommand;
import org.openbw.tsbw.micro.FrameUpdate;
import org.openbw.tsbw.micro.GatherGasCommand;
import org.openbw.tsbw.micro.GatherMineralsCommand;
import org.openbw.tsbw.micro.Message;
import org.openbw.tsbw.micro.MineGasMessage;
import org.openbw.tsbw.micro.MineMineralsMessage;
import org.openbw.tsbw.micro.MoveCommand;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.actors.MessageProcessor;
import co.paralleluniverse.fibers.SuspendExecution;

public class WorkerActor extends BasicActor<Message, Void> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger();
	
	private static int EXTRA_PIXELS_ENEMY_ATTACK_RANGE_DELAY = 2;
	
	private final double HALF_TURN_FRAMES;
	
	private SCV scv;
	private WorkerBoard publicBoard;
	List<MobileUnit> attackingEnemies;
	private boolean alive;
	private int wakeUp;
	private Command nextCommand;
	private int frame;
	private int minerals;
	private int gas;
	private int remainingLatencyFrames;
	
	private boolean gathering;
	private boolean available;
	private boolean lastCommandReturnValue;
	
	WorkerActor(WorkerBoard publicBoard) {
		
		this.publicBoard = publicBoard;
		this.wakeUp = 0;
		this.frame = 0;
		this.minerals = 0;
		this.gas = 0;
		this.remainingLatencyFrames = 0;
		this.nextCommand = null;
		this.gathering = false;
		this.available = true;
		this.lastCommandReturnValue = false;
		this.HALF_TURN_FRAMES = 180 / UnitType.Terran_SCV.turnRadius();
	}
	
	void setSCV(SCV scv) {
		
		this.scv = scv;
	}
	
	
	void gatherMinerals(MineralPatch patch) {
		
		patch.addScv();
		System.out.println("sending min mine msg to " + this.scv);
		this.sendOrInterrupt(new MineMineralsMessage(patch));
	}
	
	boolean isAvailable() {
		
		return this.available;
	}
	
	boolean isGathering() {
	
		return this.gathering;
	}
	
	void onFrame(FrameUpdate frameUpdate) {
		
		this.frame = frameUpdate.getFrame();
		
		if (frame >= this.wakeUp) {
			
			if (this.nextCommand != null) {
				
				this.lastCommandReturnValue = this.nextCommand.execute();
				logger.trace("frame {}: {} executed {} ({})", frame, this.scv, this.nextCommand, this.lastCommandReturnValue ? "success" : "failed");
				this.wakeUp = frame + this.nextCommand.getDelay();
				this.nextCommand = null;
			}
		}
		this.sendOrInterrupt(frameUpdate);
			
		
	}

	private boolean execute(Command command) throws InterruptedException, SuspendExecution {
		
		this.nextCommand = command;
		receive(m -> m instanceof FrameUpdate);
		return this.lastCommandReturnValue;
	}
	
	protected void defending() throws InterruptedException, SuspendExecution {
		
		logger.trace("frame {}: {} is defending.", frame, this.scv);
		
		this.nextCommand = new AttackUnitCommand(this.scv, this.attackingEnemies.get(0));
		while (!this.attackingEnemies.isEmpty()) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
			}
		}
	}
	
	protected void waitForResourcesBeforeTravel(TilePosition constructionSite, int requiredMinerals, int requiredGas) throws InterruptedException, SuspendExecution {
		
		int estimatedMining = 0;
		int estimatedGas = 0;
		
		double distance = this.publicBoard.getMapAnalyzer().getGroundDistance(this.scv.getTilePosition(), constructionSite);
		double travelTimetoConstructionSite = distance / Constants.AVERAGE_SCV_SPEED;
		estimatedMining = (int)PPF2.calculateEstimatedMining((int)travelTimetoConstructionSite, (int)this.publicBoard.getUnitInventory().getAvailableWorkers().count() - 1);
		estimatedGas = (int)(this.publicBoard.getUnitInventory().getRefineries().stream().mapToDouble(r -> r.getMiningRate()).sum() * travelTimetoConstructionSite);
		
		while (this.minerals + estimatedMining < requiredMinerals || this.gas + estimatedGas < requiredGas) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
			}
			
			alive &= this.scv.exists();
		}
	}

	protected void waitForResources(int requiredMinerals, int requiredGas) throws InterruptedException, SuspendExecution {
		
		logger.trace("frame {}: {} waiting for resources: {} minerals and {} gas (currently at {} and {}).", this.frame, this.scv, 
				requiredMinerals, requiredGas, this.minerals, this.gas);
		
		while (alive && (this.minerals < requiredMinerals || this.gas < requiredGas)) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
			}
			
			alive &= this.scv.exists();
		}
	}
	
	protected void constructing(TilePosition constructionSite, ConstructionType type) throws InterruptedException, SuspendExecution {
		
		this.available = false;
		
		logger.trace("frame {}: {} got assigned construction of {} at {}.", this.frame, this.scv, type, constructionSite);

		waitForResourcesBeforeTravel(constructionSite, type.getMineralPrice(), type.getGasPrice());
		logger.trace("frame {}: {} got {} minerals and {} gas. moving to construction site at {}...", frame, this.scv, this.minerals, this.gas, constructionSite);

		if (this.scv.getDistance(constructionSite.toPosition()) > this.scv.getSightRange() - 64) {
			
			moveTo(constructionSite.toPosition());
		}
		logger.trace("frame {}: {} arrived at construction site at {}.", this.frame, this.scv, constructionSite);
		
		waitForResources(type.getMineralPrice(), type.getGasPrice());
		logger.trace("frame {}: {} has enough resources to build {}.", this.frame, this.scv, type);
		
		boolean success = false;
		while(!success && alive) {
			
			success = execute(new ConstructCommand(this.scv, constructionSite, type));
			if (!success) {
				logger.error("frame {}: build command failed for {}.", this.frame, this.scv);
			}
			receive(m -> m instanceof FrameUpdate);
			alive &= this.scv.exists();
		}
		
		boolean done = false;
		while (!done && alive) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
				
				done = !this.scv.isConstructing();
				if (this.scv.getHitPoints() < 25) {
					
					// TODO call for help
					
				}
			}
			// TODO abandon building if low on health
			// TODO ask for help if being attacked
			
			alive &= this.scv.exists();
		}
		this.available = true;
		System.out.println("DONE");
	}
	
	protected void gathering(Refinery refinery) throws InterruptedException, SuspendExecution {
		
		logger.trace("frame {}: {} starting mining from {}.", this.frame, this.scv, refinery);
		this.nextCommand = new GatherGasCommand(this.scv, refinery);
		
		boolean done = false;
		while (!done && alive) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
				
				if (!this.attackingEnemies.isEmpty()) {
					defending();
				}
			}
			
			done &= refinery.getResources() > 0;
			alive &= this.scv.exists();
		}
	}
	
	protected void moveTo(Position position) throws InterruptedException, SuspendExecution {
		
		logger.trace("frame {}: {} moving to {}.", this.frame, this.scv, position);
		this.nextCommand = new MoveCommand(this.scv, position);
		while (this.scv.getDistance(position) > 64 && alive) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
			}
			
			alive &= this.scv.exists();
		}
	}
	
	protected void gathering(MineralPatch mineralPatch) throws InterruptedException, SuspendExecution {
		
		logger.trace("frame {}: {} starting mining from {}.", this.frame, this.scv, mineralPatch);
		
		MineralPatch myPatch = mineralPatch;
		this.gathering = true;
		if (!myPatch.isVisible()) {
			moveTo(myPatch.getPosition());
		}
		boolean success = execute(new GatherMineralsCommand(this.scv, myPatch));
		if (!success) {
			logger.error("frame {}: gather command failed for {}.", this.frame, this.scv);
		}
		boolean done = false;
		while (!done && alive) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
				
				if (!this.attackingEnemies.isEmpty()) {
					
					this.gathering = false;
					defending();
					this.gathering = true;
					this.nextCommand = new GatherMineralsCommand(this.scv, myPatch);
				}
			} else if (message instanceof MineMineralsMessage) {
				
//				myPatch.removeScv();
				myPatch = ((MineMineralsMessage) message).getMineralPatch();
				this.nextCommand = new GatherMineralsCommand(this.scv, myPatch);
			} else if (message instanceof BuildMessage) {
				
				this.gathering = false;
				BuildMessage bm = (BuildMessage) message;
				constructing(bm.getConstructionSite(), bm.getType());
				this.gathering = true;
				this.nextCommand = new GatherMineralsCommand(this.scv, myPatch);
			}
			
			done &= myPatch.getResources() > 0;
			alive &= this.scv.exists();
		}
		this.gathering = false;
		myPatch.removeScv();
	}
	
	private void update(FrameUpdate frameUpdate) {
		
		this.frame = frameUpdate.getFrame();
		this.minerals = frameUpdate.getMinerals();
		this.gas = frameUpdate.getGas();
		this.remainingLatencyFrames = frameUpdate.getRemainingLatencyFrames();
		
		this.attackingEnemies = frameUpdate.getEnemyUnits().stream().filter(
				e -> e.isAttacking() && isInHisAttackRange(e)).collect(Collectors.toList());
	}
	
	private Position getFuturePosition(MobileUnit unit, int frames) {
		
		Position currentPosition = unit.getPosition();
		
		double dx = unit.getVelocityX() * frames;
		double dy = unit.getVelocityY() * frames;
		
		return new Position((int)(currentPosition.getX() + dx), (int)(currentPosition.getY() + dy));
	}
	
	private boolean isInHisAttackRange(MobileUnit otherUnit) {
		
		WeaponType weapon = otherUnit.getGroundWeapon();
		
		int range = weapon.maxRange() 
				+ EXTRA_PIXELS_ENEMY_ATTACK_RANGE_DELAY * otherUnit.getGroundWeapon().damageAmount() * otherUnit.getMaxGroundHits();
		
		int latencyFrames = this.remainingLatencyFrames + (int)HALF_TURN_FRAMES;
		return this.scv.getDistance(getFuturePosition(otherUnit, latencyFrames)) <= range;
	}

	@Override
	protected Void doRun() throws InterruptedException, SuspendExecution {
		
		this.alive = true;
		
		while (alive) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
			} else if (message instanceof MineMineralsMessage) {
							
				gathering(((MineMineralsMessage) message).getMineralPatch());
			} else if (message instanceof MineGasMessage) {
				
				gathering(((MineGasMessage) message).getRefinery());
			} else if (message instanceof BuildMessage) {
				
				BuildMessage bm = (BuildMessage) message;
				constructing(bm.getConstructionSite(), bm.getType());
			}
			
			alive &= this.scv.exists();
		}
		
		return null;
	}
}
