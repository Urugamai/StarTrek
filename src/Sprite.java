/*
 * Copyright (c) 2002-2010 LWJGL Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.lwjgl.util.vector.*;
import java.io.IOException;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Mark W. Watson
 */
public class Sprite {
	private Texture		texture;					/** The texture that stores the image for this sprite */
	private static TextureLoader loader = new TextureLoader();

	private Vector3f	location;					/** Where Am I */
	private Vector3f 	motion;						/** What we are currently doing (magnitude and direction from origin) */
	private Vector3f 	influence;					/** Sum of influences on our current motion */
	private float		influenceDuration;			/** How long before we turn off the motion influence */

	private Vector3f	rotationAngle;				/** Radians of rotation (X,Y), (X,Z), (Y,Z) */
	private Vector3f	rotationInfluence;			/** Change in Radians of rotation per second */
	private float		rotationDuration;			/** How long before we turn off the rotationInfluence */

	public double		energyConsumption = 0;

	/**
	 * Create a new sprite from a specified image.
	 *
	 * @param ref A reference to the image on which this sprite should be based
	 */
	public Sprite( String ref) {
		try {
			texture = loader.getTexture(ref);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(-1);
		}

		location = new Vector3f(0,0,0);	// Start at origin until we are positioned by game
		motion = new Vector3f(0, 0, 0);	// No Motion
		influence = new Vector3f(0,0,0);	// No influences
		rotationAngle = new Vector3f(0,0,0);	// not rotated in any direction
		rotationInfluence = new Vector3f(0,0,0);	// not changing rotation
	}

	public int getWidth() {
		return texture.getImageWidth();
	}

	public int getHeight() {
		return texture.getImageHeight();
	}

	public Vector3f getMotion() {
		return motion;
	}

	public Texture getTexture() { return texture; }

	public void setLocation(float x, float y, float z) {
		location.set(x,y,z);
	}

	public void setLocation(Vector3f loc) {
		setLocation(loc.x, loc.y, loc.z);
	}

	public void setMotion(float x, float y, float z) {
		motion.set(x,y,z);
	}

	public void setInfluence(float x, float y, float z, float d) {
		influence.set(x,y,z);
		influenceDuration = d;
	}

	public void setRotationAngle(float x, float y, float z) {
		rotationAngle.set(x,y,z);
	}

	public void setRotationInfluence(float x, float y, float z, float d) {
		rotationInfluence.set(x,y,z);
		rotationDuration = d;
	}

	public Vector3f getRotationAngle() {
		return rotationAngle;
	}

	public Vector3f getLocation() {
		return location;
	}

	public void doLogic(double secondsElapsed) {
		double remainingDuration = secondsElapsed < influenceDuration || influenceDuration <= 0 ? secondsElapsed : influenceDuration;
		motion = motion.translate((float)(influence.x*remainingDuration), (float)(influence.y*remainingDuration), (float)(influence.z*remainingDuration));
		if (influenceDuration >= 0) {
			if (influenceDuration <= secondsElapsed) {
				influenceDuration = 0;
				influence.set(0, 0, 0);
			} else influenceDuration -= secondsElapsed;
		}
		location = location.translate((float)(motion.x*remainingDuration), (float)(motion.y*remainingDuration), (float)(motion.z*remainingDuration));
		energyConsumption += Math.sqrt(Math.pow(influence.x*remainingDuration,2) + Math.pow(influence.y*remainingDuration,2) + Math.pow(influence.z*remainingDuration,2));

		remainingDuration = secondsElapsed < rotationDuration || rotationDuration < 0 ? secondsElapsed : rotationDuration;
		rotationAngle = rotationAngle.translate((float)(rotationInfluence.x*remainingDuration), (float)(rotationInfluence.y*remainingDuration), (float)(rotationInfluence.z*remainingDuration) );
		if (rotationDuration >= 0) {
			if (rotationDuration <= secondsElapsed) {
				rotationDuration = 0;
				rotationInfluence.set(0, 0, 0);
			} else rotationDuration -= secondsElapsed;
		}
		energyConsumption += Math.sqrt(Math.pow(rotationInfluence.x*remainingDuration,2) + Math.pow(rotationInfluence.y*remainingDuration,2) + Math.pow(rotationInfluence.z*remainingDuration,2));
	}
}
