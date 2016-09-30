/**
 * Created by Mark on 30/08/2016.
 *
 * all coordinates are in xyz plane with x being right, y up and z into screen (;-))
 * all angles are in the trigonometric plane (0 is right/x-axis and angles increase anticlockwise)
 */

public class ObjectManagement {
	public class Vector3d {
		double x, y, z;

		public void add(Vector3d increment, double factor) { x += (increment.x * factor); y += (increment.y * factor); z += (increment.z * factor); }
		public void standardise(double minValue, double maxValue) {
			assert(minValue < maxValue);
			while (x < minValue) x += maxValue;
			while (y < minValue) y += maxValue;
			while (z < minValue) z += maxValue;

			while (x > maxValue) x -= maxValue;
			while (y > maxValue) y -= maxValue;
			while (z > maxValue) z -= maxValue;
		}
		public void multiply(double amt) {
			x = x * amt;
			y = y * amt;
			z = z * amt;
		}
	}

	// Position, motion, rotation
	Vector3d location;			// My Location
	Vector3d motion; 			// My Direction AND Speed (length from zero = speed as units per second)
	Vector3d heading;			// Rotation degrees around each axis (For 2D, use Z rotation only)

	Vector3d thrust;			// Current force being applied per second
	Vector3d forceSum;			// Sum of forces acting in this time period on this object (starts with thrust, add gravities from others, etc)

	Vector3d rollMotion;		// Current rotations around x, y and z axis in degrees per second
	Vector3d rollForce;			// rotational force applied about x, about y and about z in degrees per second - alters rollMotion

	public void setLocation(double x, double y, double z) { location.x = x; location.y = y; location.z = z; }
	public Vector3d getLocation() { return location; }
	public void addToLocation(double x, double y, double z) { location.x += x; location.y += y; location.z += z; }
	public void addToLocation(Vector3d delta) { addToLocation(delta.x, delta.y, delta.z); }

	public void setMotion(double x, double y, double z) { motion.x = x; motion.y = y; motion.z = z; }
	public Vector3d getMotion() { return motion; }
	public void addToMotion(double x, double y, double z) {}
	public void addToMotion(Vector3d delta) { addToMotion(delta.x, delta.y, delta.z); }

	public void setHeading(double x, double y, double z) { heading.x = x; heading.y = y; heading.z = z; }
	public Vector3d getHeading() { return heading; }
	public void addToHeading(double x, double y, double z) { heading.x += x; heading.y += y; heading.z += z; }
	public void addToHeading(Vector3d delta) { addToHeading( delta.x, delta.y, delta.z); }

	public void setThrust(double x, double y, double z) { thrust.x = x; thrust.y = y; thrust.z = z; }
	public Vector3d getThrust() { return thrust; }

	public void setForceSum(double x, double y, double z) { forceSum.x = x; forceSum.y = y; forceSum.z = z; }
	public void setForceSum(Vector3d value) { setForceSum(value.x, value.y, value.z); }
	public Vector3d getForceSum() { return forceSum; }

	public void setRollMotion(double x, double y, double z) { rollMotion.x = x; rollMotion.y = y; rollMotion.z = z; }
	public Vector3d getRollMotion() { return rollMotion; }
	public void addToRollMotion(double x, double y, double z) { rollMotion.x += x; rollMotion.y += y; rollMotion.z += z; }
	public void addToRollMotion(Vector3d delta) { addToRollMotion(delta.x, delta.y, delta.z); }

	public void setRollForce(double x, double y, double z) { rollForce.x = x; rollForce.y = y; rollForce.z = z; }
	public Vector3d getRollForce() { return rollForce; }

	public Vector3d multiply(Vector3d vector, double amt) {
		Vector3d newVector = new Vector3d();

		newVector.x = vector.x * amt;
		newVector.y = vector.y * amt;
		newVector.z = vector.z * amt;

		return newVector;
	}

	// Update all primary details based on effect details based on 'elapsed' seconds
	public void doUpdate(double elapsed) {
		addToMotion(multiply(forceSum, elapsed));
		addToLocation(multiply(motion, elapsed));
		addToRollMotion(multiply(rollForce, elapsed));
		addToHeading(multiply(rollMotion, elapsed));

		setForceSum(thrust);
		return;
	}

}
