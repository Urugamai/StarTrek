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

	public boolean doCommand(String cmd) {
		lastCommand = cmd;
		String[] pieces;
		lastCommandState = false;

				pieces = cmd.trim().toUpperCase().split("[ ,\t\\n\\x0B\\f\\r]");

		if (pieces[0].compareToIgnoreCase("TOR") == 0 ) { lastCommandState = command_TOR(pieces); }
		else if (pieces[0].compareToIgnoreCase("PHA") == 0 ) { lastCommandState = command_PHA(pieces); }
		else if (pieces[0].compareToIgnoreCase("IMP") == 0 ) { lastCommandState = command_IMP(pieces); }
		else if (pieces[0].compareToIgnoreCase("WARP") == 0 ) { lastCommandState = command_WARP(pieces); }
		else if (pieces[0].compareToIgnoreCase("STOP") == 0 ) { lastCommandState = command_STOP(pieces); }
		else if (pieces[0].compareToIgnoreCase("COMP") == 0 ) { lastCommandState = command_COMP(pieces); }
		else if (pieces[0].compareToIgnoreCase("LRS") == 0 ) { lastCommandState = command_LRS(pieces); }
		else if (pieces[0].compareToIgnoreCase("SHUP") == 0 ) { lastCommandState = command_SHUP(pieces); }
		else if (pieces[0].compareToIgnoreCase("SHDOWN") == 0 ) { lastCommandState = command_SHDOWN(pieces); }
		else if (pieces[0].compareToIgnoreCase("EXIT") == 0 ) { alliance.gameRunning = false; }

		return lastCommandState;
	}

	public String getLastCommand() {
		return lastCommand + " " + (lastCommandState ? "OK" : "Failed.");
	}

	private boolean command_LRS(String[] pieces) {
		for ( int gx = alliance.playerGalacticX - 1; gx <= alliance.playerGalacticX + 1; gx++) {
			for (int gy = alliance.playerGalacticY - 1; gy <= alliance.playerGalacticY + 1; gy++) {
				if (gx < 0 || gx >= alliance.galaxySize) continue;
				if (gy < 0 || gy >= alliance.galaxySize) continue;
				galaxy.getSector(gx, gy).setLRS(ship.getLRS(gx, gy));	// Scan current sector
			}
		}
		return true;
	}

	private boolean command_TOR(String[] pieces) {
		float angle;
		String direction = "";

		if (pieces.length > 1) {
			direction = pieces[1];
			try {
				angle = Float.valueOf(direction);
			} catch (Exception e) {
				return false;  // no firing for you when you get the parameter wrong
			}
		} else {
			return false;  // no firing for you when you get the parameter wrong
		}

		if (ship.torpedoCount > 0) {
			Entity torpedo =  new Entity(Entity.SubType.TORPEDO, Constants.FILE_IMG_TORPEDO, 0);
			torpedo.sprite.setLocation(ship.sprite.getLocation());
			torpedo.energyLevel = 5000;
			torpedo.maxEnergy = torpedo.energyLevel;
			torpedo.sprite.setRotationAngle(0, 0, 360-angle);
			Vector3f shipMotion = ship.sprite.getMotion();
			torpedo.sprite.setMotion(shipMotion.x-Constants.torpedoSpeed*(float)Math.sin(Math.toRadians(360-angle)),shipMotion.y+Constants.torpedoSpeed*(float)Math.cos(Math.toRadians(360-angle)), 0f);
			torpedo.sprite.doLogic(.6); // move the torpedo out before it starts working (clears us).
			sector.AddEntity(torpedo);
			sound.playSound("Torpedo");
			ship.torpedoCount--;
		}
		return true;
	}

	private boolean command_PHA(String[] pieces) {
		float power = 0;

		if (pieces.length > 1) {
			try {
				power = Float.valueOf(pieces[1]);
			} catch (Exception e) {
				return false;  // no firing for you when you get the parameter wrong
			}
		} else {
			return false;  // no firing for you when you get the parameter wrong
		}

		for (Entity entity : sector.getEntities()) {
			if (entity.eType == Entity.SubType.ENEMYSHIP) {
				entity.energyLevel -= (power - (distanceBetween(ship, entity)/10)) / sector.enemyCount;
				view.writeScreen("Unit " + entity.eType + " energy left " + entity.energyLevel);
			}
		}
		ship.energyLevel -= power;

		return true;
	}

	private boolean command_IMP(String[] pieces) {
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
		Vector3f currentMotion = ship.sprite.getMotion();
		double fx = -Math.sin(Math.toRadians(360-angle))*force - currentMotion.x;
		double fy = Math.cos(Math.toRadians(360-angle))*force - currentMotion.y;
		double fz = 0;
		ship.sprite.setInfluence((float)fx, (float)fy, (float)fz, seconds);

		// rotation adjustment
		Vector3f currentRot = ship.sprite.getRotationAngle();
		float newRotation = (360-angle+180-currentRot.z) % 360;
		ship.sprite.setRotationInfluence(0, 0, newRotation/3, 3);

		return true;
	}

	private boolean command_WARP(String[] pieces) {
		float angle;
		float range;
//		float seconds;
		String direction = "", power = ""; //, duration = "";

		if ( pieces.length > 2 ) {
			direction = pieces[1];
			power = pieces[2];
//			duration = pieces[3];

			try {
				angle = Float.valueOf(direction) % 360;
				if (angle < 0) angle += 360;
				range = Float.valueOf(power); if (range > alliance.galaxySize) range = alliance.galaxySize;
//				seconds = Float.valueOf(duration);
			} catch (Exception e) {
				return false; // no moving for you when you get the parameters wrong
			}
		} else {
			return false; // no moving for you when you get the parameters wrong
		}

		int gx = alliance.playerGalacticX;
		int gy = alliance.playerGalacticY;
		Sector currentSector = galaxy.getSector(gx, gy);
		int deltaGx = (int)Math.ceil(-Math.sin(Math.toRadians(360-angle))*range);
		int deltaGy = (int)Math.ceil(Math.cos(Math.toRadians(360-angle))*range);
		gx += deltaGx;
		gy += deltaGy;

		if (gx < 0) gx = 0; if (gx >= alliance.galaxySize) gx = alliance.galaxySize - 1;
		if (gy < 0) gy = 0; if (gy >= alliance.galaxySize) gy = alliance.galaxySize - 1;

		float energyNeeded = (float)Math.sqrt(Math.pow(deltaGx,2)+Math.pow(deltaGy,2)) * 1000;

		if (energyNeeded > ship.energyLevel) return false;

		ship.energyLevel -= energyNeeded;
		Sector newSector = galaxy.getSector(gx, gy);
		currentSector.removeEntity(ship);
		newSector.AddEntity(ship);
		view.setSector(newSector);
		sector = newSector;
		alliance.placePlayerShip(sector);
		alliance.playerGalacticX = gx;
		alliance.playerGalacticY = gy;

		alliance.rawStarDate += range;

		return true;
	}

	private boolean command_SHUP(String[] pieces) {
		float shEnergy;
		String energy = "";

		if (pieces.length > 1) {
			energy = pieces[1];
			try {
				shEnergy = Float.valueOf(energy);
			} catch (Exception e) {
				return false;  // no firing for you when you get the parameter wrong
			}
		} else {
			return false;  // no firing for you when you get the parameter wrong
		}

		if (shEnergy >= ship.energyLevel) return false;	// not enough power

		ship.energyLevel -= shEnergy;
		ship.shieldEnergy += shEnergy;
		ship.shieldsUp = true;

		return true;
	}

	private boolean command_SHDOWN(String[] pieces) {
		ship.energyLevel += ship.shieldEnergy*0.99;	// losses in transferring energy
		ship.shieldEnergy = 0;
		ship.shieldsUp = false;

		return true;
	}

	private boolean command_STOP(String[] pieces) {
		Vector3f shipMotion = ship.sprite.getMotion();
		ship.sprite.setInfluence(-shipMotion.x/3, -shipMotion.y/3, 0, 3);
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

	private boolean command_COMP(String[] pieces) {
		if (pieces.length < 2) {
			return false; // no computer command
		}

		if (pieces[1].compareToIgnoreCase("TGT") == 0 ) { command_COMP_TARGET(pieces); }
		else if (pieces[1].compareToIgnoreCase("NAV") == 0 ) { command_COMP_NAVIGATION(pieces); }
		else return false;

		return true;
	}

	// TODO work out how to get help on the screen and IF we even want to do this.
//	private void initHelp() {
//		helpWindow.writeLn( "TOR angle                   Send a torpedo out at the angle (in degrees) provided");
//		helpWindow.writeLn( "PHA power		             Fire phasers at every enemy with total indicated power divided amongst the targets. Power drop proportional with range to target.");
//		helpWindow.writeLn( "IMP angle force duration    Turn ship towards angle while applying force for indicated number of seconds");
//		helpWindow.writeLn( "WARP angle factor           Turn ship towards angle then travel at Warp Factor provided");
//		helpWindow.writeLn( "                            Warp factor 1 for 1 second will take you one sector.  Higher factors take more energy, travel further and get there faster.");
//		helpWindow.writeLn( "STOP                        apply maximum deceleration force until we have stopped moving");
//		helpWindow.writeLn( "SRS                         Short range scan, refreshes data about the current sector.");
//		helpWindow.writeLn( "LRS                         Long range scan, Gather information from adjoining sectors. This updates Galactic Map.");
//		helpWindow.writeLn( "SHUP energy                 Shields UP");
//		helpWindow.writeLn( "SHDOWN                      Shields DOWN");
//		helpWindow.writeLn( "EXIT");
//		helpWindow.writeLn("");
//		helpWindow.writeLn( "COMP command [parameters]   Ask computer to calculate something");
//		helpWindow.writeLn( "        NAV [BE]            Computer navigation angle (and range) to any Base or Enemy in current sector");
//		helpWindow.writeLn( "        TGT [BES]           Computer targeting information (angle and range) to Base, Enemy or Star in sector.");
//		helpWindow.writeLn( " ");
//		helpInitialised = true;
//	}
//	private void drawHelp() {
//		if (!helpInitialised) initHelp();
//		helpWindow.draw();
//	}
//

	private float distanceBetween(Entity me, Entity him) {
		Vector3f meLoc = me.sprite.getLocation();
		Vector3f himLoc = him.sprite.getLocation();
		return (float)Math.sqrt(Math.pow(meLoc.x-himLoc.x,2)+Math.pow(meLoc.y-himLoc.y,2));
	}

}
