/**
 * Created by Mark on 15/08/2016.
 */
public class Angle {
	double screenAngle;							// This is in SCREEN angles in DEGREES
	double trigAngle;							// same angle in trig rotations
	double spriteAngle;							// same angle in Sprite Rotations

	// Conversion settings
	double spriteToScreenOffset = 180;			// The offset from pointing straight DOWN to the images appearance angle
	double screenToTrigOffset = -270;			// the offset from screen's zero direction to Trigs zero direction
	boolean screenToTrigInvert = true;			// true when screen angles increase clockwise whereas trig is anticlockwise
	boolean deliverShortestRotation = false;	// true delivers -180 to 180, false delivers 0 to 360

	public Angle() {
		setTrigAngle(0);
	}

	public Angle(double trigAngle) {
		setTrigAngle(trigAngle);
	}

	public Angle(String aType, double angle) {
		if (aType.toUpperCase().compareTo("TRIG") == 0) this.setTrigAngle(angle);
		else if (aType.toUpperCase().compareTo("SCREEN") == 0) this.setScreenAngle(angle);
		else if (aType.toUpperCase().compareTo("SPRITE") == 0) this.setSpriteAngle(angle);
	}

	// TRIG Angles
	public void setTrigAngle(double angle) {
		trigAngle = angle;
		screenAngle = ((screenToTrigInvert ? 360 - angle : angle) - screenToTrigOffset) % 360;
		spriteAngle = ((screenAngle + spriteToScreenOffset) % 360);
	}

	public void setTrigAngleRad(double rads) {
		setTrigAngle(Math.toDegrees(rads));
	}

	public double getTrigAngle() {
		return trigAngle;
	}

	public double getTrigAngleRad() {
		return Math.toRadians(getTrigAngle());
	}

	// SCREEN Angles
	public void setScreenAngle(double angle) {
		screenAngle = angle;
		trigAngle = (screenToTrigInvert ? 360-(screenAngle + screenToTrigOffset) : screenAngle + screenToTrigOffset);
		spriteAngle = ((screenAngle + spriteToScreenOffset) % 360);
	}

	public void setScreenAngleRad(double rads) {
		screenAngle = Math.toDegrees(rads);
	}

	public double getScreenAngle() {
		return screenAngle;
	}

	public double getScreenAngleRad() {
		return Math.toRadians(getScreenAngle());
	}

	// Sprite Angles (Rotation from vertical)
	public void setSpriteRotation(double angle) {
		spriteToScreenOffset = angle;
	}

	public void setSpriteAngle(double angle) {
		spriteAngle = angle;
		screenAngle = angle - spriteToScreenOffset;
		trigAngle = (screenToTrigInvert ? 360-(screenAngle + screenToTrigOffset) : screenAngle + screenToTrigOffset) - spriteToScreenOffset;
	}

	public void setSpriteAngleRad(double rads) {
		setSpriteAngle(Math.toDegrees(rads));
	}

	public double getSpriteAngle() {
		return spriteAngle;
	}

	public double getSpriteAngleRad() {
		return Math.toRadians(getSpriteAngle());
	}

}
