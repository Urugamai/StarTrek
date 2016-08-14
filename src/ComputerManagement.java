import org.lwjgl.util.vector.Vector3f;

/**
 * Created by Mark on 25/06/2016.
 */
public class ComputerManagement {
	private Alliance alliance = null;
	private Galaxy galaxy = null;
	private Sector sector = null;
	private Entity ship = null;
	private ViewManagement view = null;
	private SoundManagement sound = null;
	private String lastCommand = "";
	private boolean lastCommandState = false;

	public void setAlliance(Alliance a) { alliance = a; }

	public void setGalaxy(Galaxy g) {
		galaxy = g;
	}

	public void setSector(Sector s) { sector = s; }

	public void setShip(Entity s) {
		ship = s;
	}

	public void setSound(SoundManagement s) { sound = s; }

	public void setView(ViewManagement v) { view = v; }

	public boolean doCommand(Entity entity, String cmd) {
		String[] pieces;
		boolean commandResult = false;

		pieces = cmd.trim().toUpperCase().split("[ ,\t\\n\\x0B\\f\\r]");

		     if (pieces[0].compareToIgnoreCase("TOR") == 0 ) 		{ commandResult = command_TOR(entity, pieces); }
		else if (pieces[0].compareToIgnoreCase("PHA") == 0 ) 		{ commandResult = command_PHA(entity, pieces); }
		else if (pieces[0].compareToIgnoreCase("IMP") == 0 ) 		{ commandResult = command_IMP(entity, pieces); }
		else if (pieces[0].compareToIgnoreCase("WARP") == 0 ) 		{ commandResult = command_WARP(entity, pieces); }
		else if (pieces[0].compareToIgnoreCase("STOP") == 0 ) 		{ commandResult = command_STOP(entity, pieces); }
		else if (pieces[0].compareToIgnoreCase("LRS") == 0 ) 		{ commandResult = command_LRS(entity, pieces); }
		else if (pieces[0].compareToIgnoreCase("SHUP") == 0 ) 		{ commandResult = command_SHUP(entity, pieces); }
		else if (pieces[0].compareToIgnoreCase("SHDOWN") == 0 ) 	{ commandResult = command_SHDOWN(entity, pieces); }
		else if (pieces[0].compareToIgnoreCase("EXIT") == 0 ) 		{ alliance.gameRunning = false; }
		else if (pieces[0].compareToIgnoreCase("COMP") == 0 ) 		{ commandResult = command_COMP(pieces); }

		if (entity.eType == Entity.SubType.FEDERATIONSHIP) {
			lastCommand = cmd;
			lastCommandState = commandResult;
		}

		if (!commandResult) {
			helpMessage(pieces[0]);
		}
		return commandResult;
	}

	public String getLastCommand() {
		return lastCommand + " " + (lastCommandState ? "OK" : "Failed.");
	}

	private boolean command_LRS(Entity entity, String[] pieces) {
		int ex = (int)Math.floor(entity.galacticLoc.x);
		int ey = (int)Math.floor(entity.galacticLoc.y);

		for ( int gx = ex - 1; gx <= ex + 1; gx++) {
			for (int gy = ey - 1; gy <= ey + 1; gy++) {
				if (gx < 0 || gx >= alliance.galaxySize) continue;
				if (gy < 0 || gy >= alliance.galaxySize) continue;
				galaxy.getSector(gx, gy).setLRS(entity, entity.getLRS(gx, gy));	// Scan current sector
			}
		}

		return true;
	}

	private boolean command_TOR(Entity entity, String[] pieces) {
		float angle = 0;
		String direction = "";
		boolean AllTargets = false;

		if (pieces.length > 1) {
			direction = pieces[1];
			if (direction.equalsIgnoreCase("*")) AllTargets = true;
			else {
				try {
					angle = Float.valueOf(direction);
				} catch (Exception e) {
					return false;  // no firing for you when you get the parameter wrong
				}
			}
		} else {
			return false;  // no firing for you when you get the parameter wrong
		}

		float[] angleList = new float[Constants.maxEnemy];
		int angleEntry = 0;
		if (AllTargets) {
			Entity.SubType opponentType = (entity.eType == Entity.SubType.ENEMYSHIP ? Entity.SubType.FEDERATIONSHIP : Entity.SubType.ENEMYSHIP);
			for (Entity enemy : galaxy.getSector((int)entity.galacticLoc.x, (int)entity.galacticLoc.y).getEntities()) {
				if (enemy.eType == opponentType) {
					angleList[angleEntry++] = (float)getDirection(entity, enemy);
					assert (angleEntry <= Constants.maxEnemy);
				}
			}
			for (int i = 0; i < angleEntry; i++)
				launchTorpedo(entity, angleList[i]);
		} else {
			launchTorpedo(entity, angle);
		}
		return true;
	}

	private void launchTorpedo(Entity entity, float angle) {
//		angle = 360 - angle;
		if (entity.torpedoCount > 0) {
			Entity torpedo = new Entity(Entity.SubType.TORPEDO, Constants.FILE_IMG_TORPEDO, 0);
			torpedo.sprite.setLocation(entity.sprite.getLocation());
			torpedo.energyLevel = Constants.torpedoEnergy.baseEnergy;
			torpedo.energyGrowth = Constants.torpedoEnergy.stdGrowth;
			torpedo.maxEnergy = Constants.torpedoEnergy.maxEnergy;
			torpedo.sprite.setRotationAngle(0, 0, angle);
			Vector3f shipMotion = entity.sprite.getMotion();
			torpedo.sprite.setMotion(shipMotion.x - Constants.torpedoSpeed.initialValue * (float) Math.sin(Math.toRadians(angle))
					, shipMotion.y + Constants.torpedoSpeed.initialValue * (float) Math.cos(Math.toRadians(angle))
					, 0f
			);
			torpedo.sprite.setInfluence(0,0,0,0);	// NO influences
			torpedo.sprite.setRotationInfluence(0,0,0,0);

			torpedo.sprite.doLogic(.6); // move the torpedo out before it starts working (clears us).
			galaxy.getSector((int)entity.galacticLoc.x, (int)entity.galacticLoc.y).AddEntity(torpedo);
			sound.playSound("Torpedo");
			entity.torpedoCount--;
		}

	}

	private boolean command_PHA(Entity entity, String[] pieces) {
		double power = 0;

		if (pieces.length > 1) {
			try {
				power = Double.valueOf(pieces[1]);
			} catch (Exception e) {
				return false;  // no firing for you when you get the parameter wrong
			}
		} else {
			return false;  // no firing for you when you get the parameter wrong
		}

		if (entity.energyLevel < power)
		{
			view.writeScreen("Insufficient energy available for requested phaser fire: " + (int)power + " > " + (int)entity.energyLevel);
			return false;
		}

		int enemyCount = 0;
		Entity.SubType opponentType = ( entity.eType == Entity.SubType.ENEMYSHIP ? Entity.SubType.FEDERATIONSHIP : Entity.SubType.ENEMYSHIP );
		for (Entity enemy : galaxy.getSector((int)entity.galacticLoc.x, (int)entity.galacticLoc.y).getEntities()) {		// count opponents in sector
			if (enemy.eType == opponentType) enemyCount++;
		}

		for (Entity enemy : galaxy.getSector((int)entity.galacticLoc.x, (int)entity.galacticLoc.y).getEntities()) {
			if (enemy.eType == opponentType) {
				enemy.energyLevel -= (power / enemyCount) - (distanceBetween(entity, enemy)/10);
				view.writeScreen("Unit " + enemy.eType + " energy left " + enemy.energyLevel);
			}
		}
		entity.energyLevel -= power;

		return true;
	}

	private boolean command_IMP(Entity entity, String[] pieces) {
		float angle;
		float force;
		float seconds;
		String direction = "", power = "", duration = "";

		if ( pieces.length > 3 ) {
			direction = pieces[1];
			power = pieces[2];
			duration = pieces[3];

			try {
				angle = Float.valueOf(direction);

				force = Float.valueOf(power); if (force > 20) force = 20;	// technically could make the energy requirements exponential and so preclude the need for a limit

				seconds = Float.valueOf(duration);							// Dont need time limit as energy reserves will expire and stop progress anyway
			} catch (Exception e) {
				return false; // no moving for you when you get the parameters wrong
			}
		} else {
			return false; // no moving for you when you get the parameters wrong
		}

		// Motion corrections
		double fx = -Math.sin(Math.toRadians(angle))*force;// + currentMotion.x;
		double fy = Math.cos(Math.toRadians(angle))*force;// + currentMotion.y;
		double fz = 0;
		entity.sprite.setInfluence((float)fx, (float)fy, (float)fz, seconds);

		// rotation adjustment - face direction THRUSTING, NOT direction travelling!
		Vector3f currentRot = entity.sprite.getRotationAngle();
		float rotation = angle-currentRot.z;
		float rotationDuration = (Math.abs(rotation)%180) / 180 * 3;	// 3 seconds to rotate 180 degrees
		if (rotationDuration < 1) rotationDuration = 1;
		entity.sprite.setRotationInfluence(0, 0, (rotation) / rotationDuration, rotationDuration );

		return true;
	}

	private boolean command_WARP(Entity entity, String[] pieces) {
		float angle;
		float range;
		String direction = "", power = "";

		if ( pieces.length > 2 ) {
			direction = pieces[1];
			power = pieces[2];

			try {
				angle = Float.valueOf(direction);
				while (angle < 0) angle += 360;
				angle %= 360;
				range = Float.valueOf(power); if (range > alliance.galaxySize) range = alliance.galaxySize;
			} catch (Exception e) {
				return false; // no moving for you when you get the parameters wrong
			}
		} else {
			return false; // no moving for you when you get the parameters wrong
		}

		int gx = (int)entity.galacticLoc.x;
		int gy = (int)entity.galacticLoc.y;
		Sector currentSector = galaxy.getSector(gx, gy);
		int deltaGx = (int)(-Math.sin(Math.toRadians(angle))*range);
		int deltaGy = (int)(Math.cos(Math.toRadians(angle))*range);
		gx += deltaGx;
		gy += deltaGy;

		// bring destination back inside the galaxy
		if (gx < 0) gx = 0; if (gx >= alliance.galaxySize) gx = alliance.galaxySize - 1;
		if (gy < 0) gy = 0; if (gy >= alliance.galaxySize) gy = alliance.galaxySize - 1;

		// you lose all the energy intended even if you do not travel the distance requested (due to trying to leave the galaxy for example)
		float energyNeeded = (float)Math.sqrt(Math.pow(deltaGx,2)+Math.pow(deltaGy,2)) * 1000;

		if (energyNeeded > entity.energyLevel) return false;

		entity.energyLevel -= energyNeeded;
		Sector newSector = galaxy.getSector(gx, gy);
		currentSector.removeEntity(entity);
		newSector.AddEntity(entity);
		view.setSector(newSector);
		sector = newSector;
		alliance.placeEntityInSector(entity, sector);

		if (entity == alliance.playerShip) {
			alliance.rawStarDate += range;
		}

		return true;
	}

	private boolean command_SHUP(Entity entity, String[] pieces) {
		double shEnergy;
		String energy = "";

		if (pieces.length > 1) {
			energy = pieces[1];
			try {
				shEnergy = Double.valueOf(energy);
			} catch (Exception e) {
				return false;  // no firing for you when you get the parameter wrong
			}
		} else {
			return false;  // no firing for you when you get the parameter wrong
		}

		if (shEnergy >= entity.energyLevel) return false;	// not enough power

		shEnergy = -entity.addEnergy(-shEnergy);
		entity.addShieldEnergy(shEnergy);

		return true;
	}

	private boolean command_SHDOWN(Entity entity, String[] pieces) {
		entity.addEnergy( entity.shieldEnergy*0.99 );	// losses in transferring energy
		entity.shieldEnergy = 0;	// excess energy is vented as we cannot leave SHUP if captain wants them down.
		entity.shieldsUp = false;

		return true;
	}

	private boolean command_STOP(Entity entity, String[] pieces) {
		Vector3f shipMotion = entity.sprite.getMotion();
		entity.sprite.setInfluence(-shipMotion.x/3, -shipMotion.y/3, 0, 3);
		return true;
	}

	private boolean command_COMP_TARGET(String[] pieces) {
		int currentLine = 1;

		if (pieces.length < 3) {
			return false; // no computer command
		}

		if (pieces[2].compareToIgnoreCase("E") == 0 ) {
//TODO			for (Entity ent : galaxy.playerSector.entities) {
//TODO				if (ent instanceof PlayerShipEntity) continue;
//TODO				if (ent instanceof StarbaseEntity) continue;
//TODO				if (ent instanceof StarEntity) continue;

			// Must be an enemy
			if (currentLine < 6) {
//TODO					otherLoc = new Galaxy.locationSpec( ent.getX(), ent.getY(), ent.getZ());
//TODO					textWindow.writeAt(currentLine++, ent.eType + " angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
			}
//TODO			}
		}
		else if (pieces[2].compareToIgnoreCase("B") == 0 ) {
//TODO			for (Entity ent : galaxy.playerSector.entities) {
//TODO				if (! (ent instanceof StarbaseEntity) ) continue;

			// Must be an Starbase
			if (currentLine < 6) {
//TODO					otherLoc = new Galaxy.locationSpec( ent.getX(), ent.getY(), ent.getZ());
//TODO					textWindow.writeAt(currentLine++, "Starbase angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
			}
//TODO			}
		}
		else if (pieces[2].compareToIgnoreCase("S") == 0 ) {
//TODO			for (Entity ent : galaxy.playerSector.entities) {
//TODO				if (! (ent instanceof StarEntity) ) continue;

			// Must be a Star
			if (currentLine < 6) {
//TODO					otherLoc = new Galaxy.locationSpec( ent.getX(), ent.getY(), ent.getZ());
//TODO					textWindow.writeAt(currentLine++, "Star angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
			}
//TODO			}
		}
		else return false;

		return true;
	}

	private boolean command_COMP_NAVIGATION(String[] pieces) {
//TODO		Galaxy.locationSpec myLoc = new Galaxy.locationSpec( galaxy.playerSector.getGalacticX(), galaxy.playerSector.getGalacticY(), galaxy.playerSector.getGalacticZ() );
		Vector3f otherLoc = null;
		int enemyCount = 0;

		if (pieces.length < 3) {
			return false; // no computer command
		}

		if (pieces[2].compareToIgnoreCase("B") == 0 ) {
//TODO			otherLoc = galaxy.getNearestStarbase(myLoc);
			if (otherLoc == null) {
				return true;
			}
//TODO			textWindow.writeAt(1, "Starbase angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
		}
		else if (pieces[2].compareToIgnoreCase("E") == 0 ) {
			if (pieces.length > 3) {
				try {
					enemyCount = Integer.valueOf(pieces[3]);
				} catch (Exception e) {
					return false; // no moving for you when you get the parameters wrong
				}
//TODO				otherLoc = galaxy.getNearestEnemyByCount(myLoc, enemyCount);
			} else
//TODO				otherLoc = galaxy.getNearestEnemy(myLoc);

				if (otherLoc == null) {
					return true;
				}
//TODO			textWindow.writeAt(1, "Nearest Enemy angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
		}

		return true;
	}

	private boolean command_COMP_COURSE(String[] pieces) {
		return false;
	}

	private boolean command_COMP(String[] pieces) {
		if (pieces.length < 2) {
			return false; // no computer command
		}

		if (pieces[1].compareToIgnoreCase("TGT") == 0 ) { command_COMP_TARGET(pieces); }
		else if (pieces[1].compareToIgnoreCase("NAV") == 0 ) { command_COMP_NAVIGATION(pieces); }
		else if (pieces[1].compareToIgnoreCase("CRS") == 0 ) { command_COMP_COURSE(pieces); }
		else return false;

		return true;
	}

	// TODO work out how to get help on the screen and IF we even want to do this.
	private void helpMessage(String command) {
		if ( command.isEmpty() || command.compareToIgnoreCase("TOR")==0)	view.writeScreen( "TOR angle                   Send a torpedo out at the angle (in degrees) provided");
		if ( command.isEmpty() || command.compareToIgnoreCase("PHA")==0)	view.writeScreen( "PHA power		             Fire phasers at every enemy with total indicated power divided amongst the targets. Power drop proportional with range to target.");
		if ( command.isEmpty() || command.compareToIgnoreCase("IMP")==0)	view.writeScreen( "IMP angle force duration    Turn ship towards angle while applying force for indicated number of seconds");
		if ( command.isEmpty() || command.compareToIgnoreCase("WARP")==0)	view.writeScreen( "WARP angle factor           Turn ship towards angle then travel at Warp Factor provided");
		if ( command.isEmpty() || command.compareToIgnoreCase("STOP")==0)	view.writeScreen( "STOP                        apply maximum deceleration force until we have stopped moving");
		if ( command.isEmpty() || command.compareToIgnoreCase("SRS")==0)	view.writeScreen( "SRS                         Short range scan, refreshes data about the current sector.");
		if ( command.isEmpty() || command.compareToIgnoreCase("LRS")==0)	view.writeScreen( "LRS                         Long range scan, Gather information from adjoining sectors. This updates Galactic Map.");
		if ( command.isEmpty() || command.compareToIgnoreCase("SHUP")==0)	view.writeScreen( "SHUP energy                 Shields UP");
		if ( command.isEmpty() || command.compareToIgnoreCase("SHDOWN")==0)	view.writeScreen( "SHDOWN                      Shields DOWN");
		if ( command.isEmpty() || command.compareToIgnoreCase("EXIT")==0)	view.writeScreen( "EXIT");
		if ( command.isEmpty() || command.compareToIgnoreCase("COMP")==0) {
			view.writeScreen("COMP command [parameters]   Ask computer to calculate something");
			view.writeScreen("        NAV [BE]            Compute navigation angle (and range) to any Base or Enemy in current sector");
			view.writeScreen("        TGT [BES]           Compute targeting information (angle and range) to Base, Enemy or Star in sector.");
			view.writeScreen("        CRS Angle           (TODO) Compute impulse setting (angle and force for 1 second) to change current heading to desired heading."); //todo implement COMP CRS
		}
	}

	private float distanceBetween(Entity me, Entity him) {
		Vector3f meLoc = me.sprite.getLocation();
		Vector3f himLoc = him.sprite.getLocation();
		return (float)Math.sqrt(Math.pow(meLoc.x-himLoc.x,2)+Math.pow(meLoc.y-himLoc.y,2));
	}

	public double getDirection(Entity me, Entity him) {
		Vector3f meLoc = me.sprite.getLocation();
		Vector3f himLoc = him.sprite.getLocation();

		double deltaX = himLoc.x - meLoc.x;
		double deltaY = himLoc.y - meLoc.y;
		double rad = Math.atan2(deltaY, deltaX);
		double deg = (Math.toDegrees(rad) + 270)%360;

		return deg;
	}
}
