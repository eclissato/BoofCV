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

package boofcv.alg.geo.f;

import boofcv.alg.geo.AssociatedPair;
import boofcv.alg.geo.GeoTestingOps;
import georegression.geometry.GeometryMath_F64;
import georegression.geometry.RotationMatrixGenerator;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.se.Se3_F64;
import georegression.transform.se.SePointOps_F64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Standardized checks for computing fundamental matrices
 *
 * @author Peter Abeles
 */
public abstract class EpipolarTestSimulation {

	Random rand = new Random(234234);

	// create a reasonable calibration matrix
	DenseMatrix64F K = new DenseMatrix64F(3,3,true,60,0.01,-200,0,80,-150,0,0,1);

	protected Se3_F64 motion;
	protected List<Point3D_F64> worldPts;
	protected List<AssociatedPair> pairs;
	protected List<Point2D_F64> currentObs;
	
	public void init( int N , boolean isFundamental ) {
		// define the camera's motion
		motion = new Se3_F64();
		motion.getR().set(RotationMatrixGenerator.eulerArbitrary(0, 1, 2, 0.05, -0.03, 0.02));
		motion.getT().set(0.1,-0.1,0.01);

		// randomly generate points in space
		worldPts = GeoTestingOps.randomPoints_F64(-1, 1, -1, 1, 2, 3, N, rand);

		// transform points into second camera's reference frame
		pairs = new ArrayList<AssociatedPair>();
		currentObs = new ArrayList<Point2D_F64>();

		for(Point3D_F64 p1 : worldPts) {
			Point3D_F64 p2 = SePointOps_F64.transform(motion, p1, null);

			AssociatedPair pair = new AssociatedPair();
			pair.keyLoc.set(p1.x/p1.z,p1.y/p1.z);
			pair.currLoc.set(p2.x/p2.z,p2.y/p2.z);
			pairs.add(pair);

			if( isFundamental ) {
				GeometryMath_F64.mult(K, pair.keyLoc, pair.keyLoc);
				GeometryMath_F64.mult(K,pair.currLoc,pair.currLoc);
			}

			currentObs.add(pair.currLoc);
		}
	}

	public List<AssociatedPair> randomPairs( int N ) {
		List<AssociatedPair> ret = new ArrayList<AssociatedPair>();

		while( ret.size() < N ) {
			AssociatedPair p = pairs.get(rand.nextInt(pairs.size()));
			if( !ret.contains(p) )
				ret.add(p);
		}

		return ret;
	}
}
