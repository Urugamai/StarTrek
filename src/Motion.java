import java.util.ArrayList;

/**
 * Created by Mark W. Watson on 13/12/2015.
 */
public class Motion {
	private static final double G = 1E-1;	// The universal gravitational constant in Pixel^3/Kg/s^2
	private static final double c = 1000;			// The speed of light in pixels per second

	private ArrayList<objectData> localObjects = new ArrayList<>();

	public Motion() {
	}

	public class objectData {
		public int handle;
		public String name;
		public double x, y, z, heading, inclination, velocity, mass;		// Where we are and where we are going
		public double thrustAcceleration, thrustDuration, thrustHeading, thrustInclination; // where we would like to head

		public objectData(int handle, double x, double y, double z, double mass) {
			this.handle = handle;
			this.x = x;
			this.y = y;
			this.z = z;
			this.mass = mass;
			this.heading = 0;
			this.inclination = 0;
			this.velocity = 0;
			this.thrustHeading = 0;
			this.thrustInclination = 0;
			this.thrustAcceleration = 0;
			this.thrustDuration = 0;
		}

		public void setHeadingData(double heading, double inclination, double velocity) {
			this.heading = heading;
			this.inclination = inclination;
			this.velocity = velocity;
		}

		public void setThrustData(double heading, double inclination, double acceleration, double duration) {
			this.thrustHeading = heading;
			this.thrustInclination = inclination;
			this.thrustAcceleration = acceleration;	// Metres/Second/Second
			this.thrustDuration = duration;
		}

		public float getHeading() { return (float)heading; }
		public float getInclination() { return (float)inclination; }

	} // END ObjectData

	public objectData addObject(String name, double x, double y, double z, double mass) {
		objectData obj;
		int handle = localObjects.size() + 1;

		obj = new objectData(handle, x, y, z, mass);
		obj.name = name + ":" + handle;
		localObjects.add(obj);

		return obj;
	}

	public int getObjectCount() {
		return localObjects.size();
	}

	// NOT NICE, avoid if at all possible,  Save the return from addObject in your source routine is preferred.
	public objectData getObject(int handle) {
		for (objectData obj : localObjects) {
			if (obj.handle == handle) return obj;
		}
		return null;
	}

	private double separation(objectData obj1, objectData obj2) {
		double dx = obj1.x - obj2.x;
		double dy = obj1.y - obj2.y;
		double dz = obj1.z - obj2.z;

		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}

	private double getHeadingBetween(objectData obj1, objectData obj2) {
		double dx = obj1.x - obj2.x;
		double dy = obj1.y - obj2.y;

		double rad = Math.atan(dy/dx);
		return rad;
	}

	private double getInclinationBetween(objectData obj1, objectData obj2) {
		double dx = obj1.x - obj2.x;
		double dy = obj1.y - obj2.y;
		double dxy = Math.sqrt(Math.pow(dx,2) + Math.pow(dy,2));  // length from origin to directly under Z
		double dz = obj1.z - obj2.z;

		double rad = Math.atan(dz/dxy);
		return rad;
	}

	// Update the object 1 based on current obj1 effects plus obj2 gravity
	private void updateObject(objectData obj1, objectData obj2, double deltaTime) {
		double oldX = obj1.x;
		double oldY = obj1.y;
		double oldZ = obj1.z;
		double distance = separation(obj1, obj2);
		double hdg = Math.toRadians(obj1.heading);
		double inc = Math.toRadians(obj1.inclination);
		double tHdg = Math.toRadians(obj1.thrustHeading);
		double tInc = Math.toRadians(obj1.thrustInclination);

		// GRAVITY  - calculate effect caused by object 2 on object 1
		double gravity = -G * obj2.mass / (distance*distance);	// M/s/s
		double gHdg = getHeadingBetween(obj1, obj2);
		double gInc = getInclinationBetween(obj1, obj2);

		double gvx = gravity * deltaTime * Math.cos(gHdg);
		double gvy = gravity * deltaTime * Math.sin(gHdg);
		double gvz = gravity * deltaTime * Math.sin(gInc);

		double dgx = gvx * deltaTime;
		double dgy = gvy * deltaTime;
		double dgz = gvz * deltaTime;

		// HEADING
		double vx = obj1.velocity * Math.cos(hdg);
		double vy = obj1.velocity * Math.sin(hdg);
		double vz = obj1.velocity * Math.sin(inc);

		double dx = vx * deltaTime;
		double dy = vy * deltaTime;
		double dz = vz * deltaTime;

		// THRUST
		double thrustTime = deltaTime;
		if (deltaTime > obj1.thrustDuration) thrustTime = obj1.thrustDuration;
		double tvx = obj1.thrustAcceleration * thrustTime * Math.cos(tHdg);
		double tvy = obj1.thrustAcceleration * thrustTime * Math.sin(tHdg);
		double tvz = obj1.thrustAcceleration * thrustTime * Math.sin(tInc);

		double dtx = tvx * thrustTime;
		double dty = tvy * thrustTime;
		double dtz = tvz * thrustTime;

		obj1.thrustDuration -= thrustTime;

		double newX = oldX + dgx + dx + dtx;
		double newY = oldY + dgy + dy + dty;
		double newZ = oldZ + dgz + dz + dtz;

		double newDx = (newX - oldX);
		double newDy = (newY - oldY);
		double newDz = (newZ - oldZ);
		double newDxy = (Math.sqrt(Math.pow(newDx,2)+ Math.pow(newDy,2)));

		double newHdg = Math.abs(newDx) < 0.00001 ? newDy > 0 ? Math.PI/2 : 3*Math.PI / 2 : Math.atan(newDy/newDx);
		double newInc = Math.abs(newDxy) < 0.00001 ? newDz > 0 ? Math.PI/2 : 3*Math.PI / 2 : Math.atan(newDz/newDxy);
		double newVelocity = Math.sqrt(Math.pow(newDx,2)+Math.pow(newDy,2)+Math.pow(newDz,2));

		obj1.x = newX;
		obj1.y = newY;
		obj1.z = newZ;
		obj1.heading = Math.toDegrees(newHdg);
		obj1.inclination = Math.toDegrees(newInc);
		obj1.velocity = newVelocity;

//		if ( deltaTime > 0 && obj1.name.startsWith("Enterprise") && obj2.name.startsWith("STAR") ) {
//			System.out.println("DEBUG: updateObject (" + deltaTime + ")"
//					+ " Source " + obj1.name + "(" + obj1.mass + ")"
//					+ " relative to " + obj2.name + "(" + obj2.mass + ")"
//					+ " at (" + obj1.x + "," + obj1.y + "," + obj1.z + ")"
//					+ " hdg (" + obj1.heading + "," + obj1.inclination + ")"
//					+ " thrust hdg (" + obj1.thrustHeading + "," + obj1.thrustInclination + ")"
//					+ " force (" + obj1.thrustAcceleration + "," + obj1.thrustDuration + ")"
//					+ " gravity hdg (" + gHdg + "," + gInc + ")"
//					+ " force (" + gravity + ")"
//			);
//		}
	}

	public void updateObjects(double deltaTime) {
		for (objectData obj1 : localObjects) {
			for (objectData obj2 : localObjects) {
				if (obj1 == obj2) continue;
				updateObject(obj1, obj2, deltaTime);
			}
		}
	}
}
