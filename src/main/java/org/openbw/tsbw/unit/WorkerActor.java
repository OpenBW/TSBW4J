package org.openbw.tsbw.unit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.BwError;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Mechanical;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.tsbw.building.ConstructionType;
import org.openbw.tsbw.micro.AttackUnitCommand;
import org.openbw.tsbw.micro.Command;
import org.openbw.tsbw.micro.ConstructCommand;
import org.openbw.tsbw.micro.GatherGasCommand;
import org.openbw.tsbw.micro.GatherMineralsCommand;
import org.openbw.tsbw.micro.HaltConstructionCommand;
import org.openbw.tsbw.micro.MoveCommand;
import org.openbw.tsbw.micro.RepairCommand;
import org.openbw.tsbw.micro.ResumeBuildingCommand;
import org.openbw.tsbw.micro.ScoutCommand;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;

public class WorkerActor extends BasicActor<Message, Void> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger();
	
	private SCV scv;
	private WorkerBoard publicBoard;
	private List<MobileUnit> attackingEnemies;
	private List<Building> buildingsToRepair;
	private boolean alive;
	private int wakeUp;
	private Command nextCommand;
	private int frame;
	private int minerals;
	private int gas;
	
	private boolean gathering;
	private boolean available;
	private boolean lastCommandReturnValue;
	
	WorkerActor(WorkerBoard publicBoard) {
		
		this.publicBoard = publicBoard;
		this.wakeUp = 0;
		this.frame = 0;
		this.minerals = 0;
		this.gas = 0;
		this.nextCommand = null;
		this.gathering = false;
		this.available = true;
		this.alive = true;
		this.lastCommandReturnValue = false;
	}
	
	void setSCV(SCV scv) {
		
		this.scv = scv;
	}
	
	boolean isAvailable() {
		
		return this.available;
	}
	
	void setAvailable(boolean available) {
		
		this.available = available;
	}
	
	boolean isGathering() {
	
		return this.gathering;
	}
	
	void onFrame(FrameUpdate frameUpdate) {
		
		this.frame = frameUpdate.getFrame();
		if (frame >= this.wakeUp) {
			
			if (this.nextCommand != null) {
				
				this.lastCommandReturnValue = this.nextCommand.execute();
				if (this.lastCommandReturnValue) { // TODO this is not guaranteed to return the error related to the command
				
					this.wakeUp = frame + this.nextCommand.getDelay();
				} else {
					BwError error = this.publicBoard.getInteractionHandler().getLastError();
					logger.warn("{} failed with error probably being {}", this.nextCommand, error);
				}
				logger.trace("frame {}: {} executed {} ({})", frame, this.scv, this.nextCommand, this.lastCommandReturnValue ? "success" : "failed");
				
				this.nextCommand = null;
				Strand.unpark(this.getStrand());
			}
		}
		this.sendOrInterrupt(frameUpdate);
	}

	private boolean execute(Command command) throws InterruptedException, SuspendExecution {
		
		this.nextCommand = command;
		Strand.park();
		return this.lastCommandReturnValue;
	}
	
	protected void scouting() throws InterruptedException, SuspendExecution {
		
		this.available = false;
		execute(new ScoutCommand(this.scv, this.publicBoard.getMyInventory()));
		while(this.alive) {
			
			Message message = receive();
			if (!(message instanceof FrameUpdate)) {
				
				logger.warn("frame {}: {} received {} but am scouting.", this.frame, this.scv, message);
			}
			this.alive &= this.scv.exists();
		}
		this.available = true;
	}
	
	protected void defending() throws InterruptedException, SuspendExecution {
		
		this.available = false;
		logger.trace("frame {}: {} is defending.", frame, this.scv);
		
		while (!this.attackingEnemies.isEmpty() && this.alive) {
			
			MobileUnit enemyToAttack = null;
			for (MobileUnit enemy : this.publicBoard.getAttackers().keySet()) {
				
				Set<SCV> currentDefenders = this.publicBoard.getAttackers().get(enemy);
				double combinedHitPoints = currentDefenders.stream().mapToInt(s -> s.getHitPoints()).sum();
				
				if (combinedHitPoints * WeaponType.Fusion_Cutter.damageAmount() / WeaponType.Fusion_Cutter.damageCooldown() < 
						enemy.getHitPoints() * enemy.getGroundWeapon().damageAmount() / enemy.getGroundWeapon().damageCooldown()) {
					
					enemyToAttack = enemy;
					break;
				}
			}
			if (enemyToAttack == null) {
				
				enemyToAttack = this.attackingEnemies.stream().filter(e -> e.getGroundWeapon() != WeaponType.None).min((e1, e2) -> Integer.compare(
		        		e1.getHitPoints(), 
		        		e2.getHitPoints())).get();
			}
			
			Set<SCV> currentDefenders = this.publicBoard.getAttackers().get(enemyToAttack);
			if (currentDefenders == null) {
				
				currentDefenders = new HashSet<>();
				this.publicBoard.getAttackers().put(enemyToAttack, currentDefenders);
			}
			currentDefenders.add(this.scv);
			
			this.nextCommand = new AttackUnitCommand(this.scv, enemyToAttack);
			while (enemyToAttack.exists() && this.scv.getDistance(this.publicBoard.getMyInventory().getMain()) < 192 && this.alive) {
				
				Message message = receive();
				if (message instanceof FrameUpdate) {
					
					update((FrameUpdate)message);
					
					for (Building toRepair : this.buildingsToRepair) {
						
						if (this.publicBoard.addRepair(toRepair, this.scv)) {
							
							repairing(toRepair);
							break;
						}
						
					}
				} else if (message instanceof BuildMessage) {
					
					logger.warn("frame {}: {} received build request but am defending.", this.frame, this.scv);
				}
				
				this.alive &= this.scv.exists();
			}
			
			currentDefenders.remove(this.scv);
		}
		this.available = true;
	}
	
	protected void waitForResources(int requiredMinerals, int requiredGas) throws InterruptedException, SuspendExecution {
		
		logger.trace("frame {}: {} waiting for resources: {} minerals and {} gas (currently at {} and {}).", this.frame, this.scv, 
				requiredMinerals, requiredGas, this.minerals, this.gas);
		
		while (this.alive && (this.minerals < requiredMinerals || this.gas < requiredGas)) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
			} else if (message instanceof BuildMessage) {
				
				logger.warn("frame {}: {} received build request although I am already constructing (waiting for resources).", this.frame, this.scv);
			}
			
			this.alive &= this.scv.exists();
		}
	}
	
	protected void resumeBuilding(Building construction) throws InterruptedException, SuspendExecution {
		
		this.available = false;
		boolean success = execute(new ResumeBuildingCommand(this.scv, construction));
		while(construction.exists() && !construction.isCompleted() && this.alive) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
				if (!success) {
					
					success = execute(new ResumeBuildingCommand(this.scv, construction));
				}
			}
			
			this.alive &= this.scv.exists();
		}
		
		this.available = true;
	}
	
	protected void constructing(TilePosition constructionSite, ConstructionType type) throws InterruptedException, SuspendExecution {
		
		logger.trace("frame {}: {} constructing {} at {}.", this.frame, this.scv, type, constructionSite);

		this.available = false;
		
		Position constructionSiteCenter = new Position(constructionSite.getX() * 32 + 32, constructionSite.getY() * 32 + 32); 
		if (this.scv.getDistance(constructionSiteCenter) > this.scv.getSightRange() - 64) {
			
			moveTo(constructionSiteCenter);
		}
		logger.trace("frame {}: {} arrived at construction site at {}.", this.frame, this.scv, constructionSite);
		
		waitForResources(type.getMineralPrice(), type.getGasPrice());
		logger.trace("frame {}: {} has enough resources to build {}.", this.frame, this.scv, type);
		
		boolean success = false;
		Command constructCommand = new ConstructCommand(this.scv, constructionSite, type);
		while(!success && this.alive) {
			
			success = execute(constructCommand);
			if (!success) {
				logger.error("frame {}: build command failed for {}.", this.frame, this.scv);
			}
			receive(m -> m instanceof FrameUpdate);
			this.alive &= this.scv.exists();
		}
		
		boolean done = false;
		while (!done && this.alive) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
				
				if (this.scv.isIdle()) {
					
					logger.warn("{}: warning: {} should be constructing {} but is idle. Attempting to restart construction...", this.frame, this.scv, type);
					execute(constructCommand);
				}
				if (this.scv.getHitPoints() < 25) {
					
					// TODO call for help
					success = execute(new HaltConstructionCommand(this.scv));
					MineralPatch nearestPatch = this.scv.getClosest(this.publicBoard.getMyInventory().getMineralPatches());
					success = execute(new GatherMineralsCommand(this.scv, nearestPatch));
					done = true;
				}
			} else if (message instanceof GatherMineralsMessage) {
					
				done = true;
			} else if (message instanceof BuildMessage) {
				
				logger.warn("frame {}: {} received build request although I am already constructing.", this.frame, this.scv);
			}
			// TODO ask for help if being attacked
			
			this.alive &= this.scv.exists();
		}
		this.available = true;
	}
	
	protected void gathering(Refinery refinery) throws InterruptedException, SuspendExecution {
		
		this.available = false;
		logger.trace("frame {}: {} gathering from {}.", this.frame, this.scv, refinery);
		
		execute(new GatherGasCommand(this.scv, refinery));
		
		boolean done = false;
		while (!done && this.alive && refinery.exists()) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
			} else if (message instanceof BuildMessage) {
				
				logger.warn("frame {}: {} received build request although I am gathering gas.", this.frame, this.scv);
			} else if (message instanceof ScoutMessage) {
				
				logger.warn("frame {}: {} received scout request although I am gathering gas.", this.frame, this.scv);
			}
			
			this.alive &= this.scv.exists();
		}
		
		logger.trace("frame {}: {} stopped gathering gas from {}.", this.frame, this.scv, refinery);
		this.available = true;
	}
	
	protected void moveTo(Position position) throws InterruptedException, SuspendExecution {
		
		logger.trace("frame {}: {} moving to {}.", this.frame, this.scv, position);
		
		boolean success = execute(new MoveCommand(this.scv, position));
		
		while (this.scv.getDistance(position) > 96 && this.alive) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
				if (!success) {
					
					success = execute(new MoveCommand(this.scv, position));
				}
			} else if (message instanceof BuildMessage) {
				
				BuildMessage bm = (BuildMessage) message;
				constructing(bm.getConstructionSite(), bm.getType());
			} else if (message instanceof ScoutMessage) {
				
				scouting();
			}
			
			this.alive &= this.scv.exists();
		}
	}
	
	protected void repairing(Building toRepair) throws InterruptedException, SuspendExecution {
		
		logger.info("BUNKER NEEDS REPAIR");
		
		execute(new RepairCommand(this.scv, (Mechanical)toRepair));
		while (toRepair.exists() && toRepair.getHitPoints() < toRepair.maxHitPoints() && this.alive) {
			
			receive();
			
			this.alive &= this.scv.exists();
		}
	}
	
	protected void gathering(MineralPatch mineralPatch) throws InterruptedException, SuspendExecution {
		
		logger.trace("frame {}: {} gathering from {}.", this.frame, this.scv, mineralPatch);
		
		MineralPatch myPatch = mineralPatch;
		
		this.gathering = true;
		if (!myPatch.exists()) {
			
			logger.trace("frame {}: {} target {} is not visible. Moving there first.", this.frame, this.scv, mineralPatch);
			moveTo(myPatch.getPosition());
		}
		boolean success = false;
		boolean done = false;
		while (!done && this.alive) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
				
				for (Building toRepair : this.buildingsToRepair) {
					
					if (this.publicBoard.addRepair(toRepair, this.scv)) {
						
						this.gathering = false;
						myPatch.removeScv();
						repairing(toRepair);
						this.gathering = true;
						success = false;
						break;
					}
				}
				
				if (!this.attackingEnemies.isEmpty()) {
					
					this.gathering = false;
					myPatch.removeScv();
					defending();
					this.gathering = true;
					success = false;
				}
				if (!success) {
					
					myPatch.addScv();
					success = execute(new GatherMineralsCommand(this.scv, myPatch));
					if (!success) {
						logger.error("frame {}: gather command failed for {}.", this.frame, this.scv);
					} else {
						
						this.gathering = true;
					}
				} else if (this.scv.getTargetUnit() instanceof MineralPatch && !myPatch.equals(this.scv.getTargetUnit())) {
					
//					execute(new GatherMineralsCommand(this.scv, myPatch));
				}
			} else if (message instanceof GatherGasMessage) {
				
				gathering(((GatherGasMessage) message).getRefinery());
			} else if (message instanceof GatherMineralsMessage) {
				
				myPatch.removeScv();
				myPatch = ((GatherMineralsMessage) message).getMineralPatch();
				if (!myPatch.isVisible() || !myPatch.exists()) {
					moveTo(myPatch.getPosition());
				}
				success = false;
			} else if (message instanceof BuildMessage) {
				
				this.gathering = false;
				myPatch.removeScv();
				BuildMessage bm = (BuildMessage) message;
				constructing(bm.getConstructionSite(), bm.getType());
				success = false;
			} else if (message instanceof ResumeBuildingMessage) {
				
				ResumeBuildingMessage bm = (ResumeBuildingMessage) message;
				this.gathering = false;
				myPatch.removeScv();
				resumeBuilding(bm.getConstruction());
				success = false;
			} else if (message instanceof ScoutMessage) {
				
				scouting();
			}
			
			done &= myPatch.getResources() > 0 && myPatch.exists();
			this.alive &= this.scv.exists();
		}
		
		if (this.gathering) {
			
			myPatch.removeScv();
		}
		this.gathering = false;
	}
	
	private void update(FrameUpdate frameUpdate) {
		
		this.frame = frameUpdate.getFrame();
		this.minerals = frameUpdate.getMinerals();
		this.gas = frameUpdate.getGas();
		
		this.attackingEnemies = frameUpdate.getAttackingUnits();
		this.buildingsToRepair = frameUpdate.getBuildingsToRepair();
	}
	
	@Override
	protected Void doRun() throws InterruptedException, SuspendExecution {
		
		logger.trace("frame {}: {} doRun()", this.frame, this.scv);
		this.alive = true;
		
		while (this.alive) {
			
			Message message = receive();
			if (message instanceof FrameUpdate) {
				
				update((FrameUpdate)message);
				gathering(this.publicBoard.getMyInventory().getMineralPatches().first());
			}
			
			this.alive &= this.scv.exists();
		}
		
		logger.trace("frame {}: {} died ({} hitpoints left).", this.frame, this.scv, this.scv.getHitPoints());
		
		return null;
	}
}
