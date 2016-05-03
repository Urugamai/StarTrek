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
	public static final String		FILE_SND_LOSE		= "res/ST_lose.wav";	// TODO Replace sound with one of my own

	// Audio
	public static final String		FILE_SND_TORPEDO	= "res/ST_Torpedo.wav";

	// Originals - to be replaced
	public static final String		FILE_SND_HIT		= "res/hit.wav";
	public static final String		FILE_SND_START		= "res/start.wav";
	public static final String		FILE_SND_WIN		= "res/win.wav";

	/** Game Parameters */
	public static final String		WINDOW_TITLE		= "Alliance";
	public static final int			FramesPerSecond		= 60;

	public static final float		fractionMessageWindowWidth	= 1f;
	public static final float		fractionMessageWindowHeight	= 0.20f;

	public static final float		fractionStatusWindowWidth	= 0.20f;
	public static final float		fractionStatusWindowHeight	= 1 - fractionMessageWindowHeight;


	public static final float		fractionSectorWindowWidth	= 1 - fractionStatusWindowWidth;
	public static final float		fractionSectorWindowHeight	= 1 - fractionMessageWindowHeight;

	public static final int			sectorSize			= 501;		// MUST be ODD (just do it)
	public static final int 		sectorCentre 		= (int)((sectorSize-1)/2);

	public static final String		txtFont				= "Courier New";
	public static final int			txtStyle			= Font.PLAIN;
	public static final int			txtSize				= 14;

	public static final int			textBufferSize		= 100;		// Number of text lines to keep

	public static final int			maxEnemy			= 5;		// Per sector
	public static final float		starbaseProbability = 0.1f;
	public static final int			startEnemyCount		= 100;		// Per Galaxy
	public static final int			maxPlanets			= 9;		// Per Sector

	// All speeds are % of lightspeed?
	public static final float 		PHASER_SPEED 		= 1000.0f;		// pixels per second
	public static final float		warpSpeedMax		= 10.0f;		// multiple of light speed
	public static final float 		IMPULSE_MAX 		= 0.2f;		// multiple of light speed
	public static final float 		TORPEDO_SPEED 		= 100.0f;		// pixels per second
	public static final float		c					= 500.0f;	// The speed of light, in pixels per second

	public static final float		maxEnergy			= 3000f;
	public static final int 		maxTorpedoes		= 15;

	public static final float		maxStarbaseEnergy	= 300000f;
	public static final int 		maxStarbaseTorpedoes = 1500;

	public static enum DisplayMode {HELP_SCREEN, DISPLAY_SECTOR, GALACTIC_MAP, SHIP_STATUS};
	public static enum sectorDirection { LeftTop, Top, RightTop, Left, here, Right, LeftBottom, Bottom, RightBottom };
	public static enum LRSItems {
		Enemy(0), Starbases(1), Planets(2);
		private final int value;
		LRSItems(int value) {this.value = value; }
		public int value() { return value; }
		public static final int Size = LRSItems.values().length;
	};

	public static double sectorXScale = 1, sectorYScale = 1;

	public static int Pixels2UnitsX(int pixels) {
		return (int)Math.ceil(pixels / Constants.sectorXScale) - Constants.sectorCentre;
	}

	public static int Pixels2UnitsY(int pixels) {
		return (int)Math.ceil(pixels / Constants.sectorYScale) - Constants.sectorCentre;
	}

	public static int Units2PixelsX(int units) {
		return (int)Math.floor((units + Constants.sectorCentre) * Constants.sectorXScale);
	}

	public static int Units2PixelsY(int units) {
		return (int)Math.floor((units + Constants.sectorCentre) * Constants.sectorYScale);
	}

	// Prevent construction call
	private Constants(){ }
}
