package org.openbw.tsbw.mining;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.micro.AttackUnitCommand;
import org.openbw.tsbw.micro.Command;
import org.openbw.tsbw.micro.FrameUpdate;
import org.openbw.tsbw.micro.Message;
import org.openbw.tsbw.micro.MineCommand;
import org.openbw.tsbw.micro.NewOrder;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.SCV;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;

public class WorkerActor extends BasicActor<Message, Void> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger();
	
	private static int EXTRA_PIXELS_ENEMY_ATTACK_RANGE_DELAY = 4;
	private static int EXTRA_REMAINING_LATENCY_FRAMES = 7;
	
	private final double HALF_TURN_FRAMES;
	
	private SCV scv;
	private WorkerBoard publicBoard;
	private Group<MineralPatch> mineralPatches;
	private Group<Refinery> refineries;
	List<MobileUnit> attackingEnemies;
	private boolean alive;
	private int wakeUp;
	private Command currentOrder;
	private Command nextCommand;
	private int frame;
	private int remainingLatencyFrames;
	
	WorkerActor(SCV scv, WorkerBoard publicBoard) {
		
		this.scv = scv;
		this.publicBoard = publicBoard;
		this.wakeUp = 0;
		this.frame = 0;
		this.remainingLatencyFrames = 0;
		this.HALF_TURN_FRAMES = 180 / scv.getTurnRadius();
		this.currentOrder = null;
		this.nextCommand = null;
		this.spawn();
	}
	
	void initialize(Group<MineralPatch> mineralPatches, Group<Refinery> refineries) {
		
		this.mineralPatches = mineralPatches;
		this.refineries = refineries;
	}
	
	public void mineMinerals() {
		
		MineralPatch patch = this.mineralPatches.first();
		this.currentOrder = new MineCommand(this.frame, this.scv, patch);
		this.publicBoard.assign(this.scv, patch);
		this.sendOrInterrupt(new NewOrder());
	}
	
	public void mineGas() {
		
		Refinery refinery = this.refineries.first();
		this.currentOrder = new MineCommand(this.frame, this.scv, refinery, this.publicBoard);
		this.sendOrInterrupt(new NewOrder());
	}
	
	public void stopMine() {
		
		this.publicBoard.removeFromPatch(scv);
	}
	
	void onFrame(FrameUpdate frameUpdate) {
		
		this.frame = frameUpdate.getFrame();
		
		if (frame >= this.wakeUp) {
			
			if (this.nextCommand != null) {
				
				this.nextCommand.execute(scv);
				logger.trace("frame {}: {} executed {}", frame, this.scv, this.nextCommand);
				this.wakeUp = frame + this.nextCommand.getDelay();
				this.nextCommand = null;
			}
		}
		this.sendOrInterrupt(frameUpdate);
			
		
	}

	protected void defending() throws InterruptedException, SuspendExecution {
		
		this.nextCommand = new AttackUnitCommand(this.frame, this.attackingEnemies.get(0));
		while (!this.attackingEnemies.isEmpty()) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
			}
		}
		this.nextCommand = this.currentOrder;
	}
	
	protected void constructing() throws InterruptedException, SuspendExecution {
		
		// TODO abandon building if low on health
		// TODO ask for help if being attacked
	}
	
	protected void mining() throws InterruptedException, SuspendExecution {
		
		boolean done = false;
		while (!done) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
				
				if (!this.attackingEnemies.isEmpty()) {
					defending();
				}
			}
		}
	}
	
	private void update(FrameUpdate frameUpdate) {
		
		this.frame = frameUpdate.getFrame();
		this.remainingLatencyFrames = frameUpdate.getRemainingLatencyFrames();
		
		this.attackingEnemies = frameUpdate.getEnemyUnits().stream().filter(
				e -> e.isAttacking() && isInHisRange(e)).collect(Collectors.toList());
	}
	
	private Position getFuturePosition(MobileUnit unit, int frames) {
		
		Position currentPosition = unit.getPosition();
		
		double dx = unit.getVelocityX() * frames;
		double dy = unit.getVelocityY() * frames;
		
		return new Position((int)(currentPosition.getX() + dx), (int)(currentPosition.getY() + dy));
	}
	
	private boolean isInHisRange(MobileUnit otherUnit) {
		
		int range = otherUnit.getGroundWeapon().maxRange() 
				+ EXTRA_PIXELS_ENEMY_ATTACK_RANGE_DELAY * otherUnit.getGroundWeapon().damageAmount() * otherUnit.getMaxGroundHits();
		
		int latencyFrames = this.remainingLatencyFrames + EXTRA_REMAINING_LATENCY_FRAMES + (int)HALF_TURN_FRAMES;
		return this.scv.getDistance(getFuturePosition(otherUnit, latencyFrames)) <= range;
	}

	@Override
	protected Void doRun() throws InterruptedException, SuspendExecution {
		
		this.alive = true;
		
		while (alive) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
			} else if (message instanceof NewOrder) {
				
				logger.trace("frame {}: {} new order received: {}.", this.frame, this.scv, this.currentOrder);
				this.nextCommand = this.currentOrder;
				if (this.currentOrder instanceof MineCommand) {
					
					mining();
				}
			}
		}
		
		return null;
	}
}
