import java.awt.*;

import static java.awt.Font.PLAIN;

/**
 * Created by Mark on 9/12/2015.
 */
public class Constants {
	// Graphics
	public static final String		FILE_IMG_ENTERPRISE	= "res/ST_Enterprise.gif";
	public static final String		FILE_IMG_TORPEDO	= "res/ST_Torpedo.gif";
	public static final String		FILE_IMG_PHASER		= "res/ST_Phaser.gif";
	public static final String 		FILE_IMG_ROMULAN 	= "res/ST_Romulan.gif";
	public static final String		FILE_IMG_STARBASE	= "res/ST_StarBase.gif";
	public static final String		FILE_IMG_STAR		= "res/ST_Star.gif";
	public static final String		FILE_IMG_PLANET		= "res/ST_Planet.gif";

	// Audio
	public static final String		FILE_SND_TORPEDO	= "res/ST_Torpedo.wav";

	// Originals	// TODO Replace sound with one of my own
	public static final String		FILE_SND_HIT		= "res/hit.wav";
	public static final String		FILE_SND_START		= "res/start.wav";
	public static final String		FILE_SND_WIN		= "res/win.wav";
	public static final String		FILE_SND_LOSE		= "res/ST_lose.wav";

	/** Game Parameters */
	public static final String		WINDOW_TITLE		= "Alliance";
	public static final int			FramesPerSecond		= 60;
	public static final int			viewSector = 0, viewStatus = 1, viewComputer = 2, viewGalaxy = 3;
	public static final double 		sectorWindowPercentW = 0.8;	// percentage of viewable screen space to allocate to sector view box
	public static final double 		sectorWindowPercentH = 0.8;

	// PRODUCTION PARAMETERS
	public static final int			maxEnemy			= 9;		// Per sector
	public static final float		starbaseProbability = 0.1f;
	public static final int			maxPlanets			= 9;		// Per Sector
	public static final int			startDate			= 20210703;

	// LIMITS
	public static final class Limits {
		float	minValue;
		float	initialValue;
		float	maxValue;
		public Limits(float min, float start, float max) {
			minValue = min;
			initialValue = start;
			maxValue = max;
		}
	}

	public static final Limits		warpSpeed 			= new Limits(	0f,		1f,		10f		);
	public static final Limits		impulseSpeed 		= new Limits(	0f,		1f,		100f	);
	public static final Limits		torpedoSpeed		= new Limits(	0f,		100f,	100f	);

	public static final Limits		shipTorpedoes 		= new Limits( 	0f, 	25f,	50f		);
	public static final Limits		starbaseTorpedoes 	= new Limits(	0f,		500f,	500f	);

	// ENERGY
	public static final class Energy {
		double	baseEnergy;
		double	minEnergy;
		double	maxEnergy;
		double	stdGrowth;
		double	dockedGrowth;
		double	runningEnergy;

		public Energy(double be, double mine, double maxe, double stdg, double dg, double re ) {
			baseEnergy = be;
			minEnergy = mine;
			maxEnergy = maxe;
			stdGrowth = stdg;
			dockedGrowth = dg;
			runningEnergy = re;
		}
	}

	public static final Energy starEnergy 		= new Energy( 1000000000, 	0, 	1000000000, 	100000, 	0, 		0 		);
	public static final Energy planetEnergy 	= new Energy( 10000000, 	0, 	10000000, 		10000, 		0, 		0 		);
	public static final Energy starbaseEnergy 	= new Energy( 1000000, 		0, 	1000000, 		60, 		0, 		10 		);
	public static final Energy shipEnergy 		= new Energy( 10000, 		0, 	10000, 			50, 		100, 	1 		);
	public static final Energy shieldEnergy 	= new Energy( 0, 			0, 	10000, 			0, 			0, 		0.001 	);
	public static final Energy torpedoEnergy 	= new Energy( 1000,			0, 	1000, 			0, 			0, 		0	 	);

	// Prevent construction call
	private Constants(){ }
}
