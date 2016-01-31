/**
 * Created by Mark on 31/01/2016.
 */

public class Transaction {
	public static enum Type {GALAXY, SECTOR, ENTITY};
	public static enum Action {ADD, DEDUCT, UPDATE, INSERT, DELETE};
	public static enum SubType { STAR, STARBASE, FEDERATIONSHIP, ROMULANSHIP, TORPEDO, BORGSHIP, PHASER };	// What Am I
	public static enum What {ENERGY, TORPEDO, STRUCTURE, SELF};

	protected Type type;
	protected Action action;
	protected SubType subType;
	protected Object who;
	protected What what;
	protected double howMuch;
}
