/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.geo;

import georegression.struct.point.Point2D_F64;


/**
 * <p>
 * Contains the location of a point feature in an image in the key frame and the current frame.
 * Useful for applications where the motion or structure of a scene is computed between
 * two images.
 * </p>
 *
 * @author Peter Abeles
 */
public class AssociatedPair {

	/**
	 * Location of the feature in the key frame.
	 */
	public Point2D_F64 keyLoc;
	/**
	 * Location of the feature in the current frame.
	 */
	public Point2D_F64 currLoc;

	public AssociatedPair() {
		keyLoc = new Point2D_F64();
		currLoc = new Point2D_F64();
	}

	/**
	 * Creates a new associated point from the two provided points.
	 *
	 * @param x1 keyframe location x-axis.
	 * @param y1 keyframe location y-axis.
	 * @param x2 current location x-axis.
	 * @param y2 current location y-axis.
	 */
	public AssociatedPair(double x1, double y1,
						  double x2, double y2) {
		keyLoc = new Point2D_F64(x1, y1);
		currLoc = new Point2D_F64(x2, y2);
	}

	/**
	 * Creates a new associated point from the two provided points.
	 *
	 * @param keyLoc keyframe location
	 * @param currLoc current location
	 */
	public AssociatedPair(Point2D_F64 keyLoc, Point2D_F64 currLoc) {
		this(keyLoc,currLoc,true);
	}

	/**
	 * Creates a new associated point from the two provided points.
	 *
	 * @param keyLoc keyframe location
	 * @param currLoc current location
	 * @param newInstance Should it create new points or save a reference to these instances.
	 */
	public AssociatedPair(Point2D_F64 keyLoc, Point2D_F64 currLoc, boolean newInstance) {
		if (newInstance) {
			this.keyLoc = new Point2D_F64(keyLoc);
			this.currLoc = new Point2D_F64(currLoc);
		} else {
			this.keyLoc = keyLoc;
			this.currLoc = currLoc;
		}
	}
}
