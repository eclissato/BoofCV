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

import boofcv.abst.geo.EpipolarMatrixEstimator;
import boofcv.abst.geo.EpipolarMatrixEstimatorN;
import boofcv.abst.geo.f.FundamentalNto1;
import boofcv.factory.geo.FactoryEpipolar;
import georegression.geometry.GeometryMath_F64;
import georegression.geometry.RotationMatrixGenerator;
import georegression.struct.point.Point3D_F64;
import georegression.struct.se.Se3_F64;
import georegression.transform.se.SePointOps_F64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Peter Abeles
 */
public class BenchmarkStabilityFundamental {

	Random rand = new Random(265);

	double sigmaPixels = 0;

	int totalPoints = 100;
	double sceneCenterZ = 3;
	double sceneRadius = 2;

	List<Point3D_F64> scene;
	List<AssociatedPair> observations;

	// create a reasonable calibration matrix
	DenseMatrix64F K = new DenseMatrix64F(3,3,true,60,0.01,-200,0,80,-150,0,0,1);
	DenseMatrix64F K_inv = new DenseMatrix64F(3,3);
	// relationship between both camera
	protected Se3_F64 motion;

	// are observations in pixels or normalized image coordinates
	public static boolean isPixels = false;

	List<Double> scores;

	public BenchmarkStabilityFundamental() {
		CommonOps.invert(K, K_inv);
	}

	public void createSceneCube() {
		scene = new ArrayList<Point3D_F64>();

		for( int i = 0; i < totalPoints; i++ ) {
			Point3D_F64 p = new Point3D_F64();

			p.x = (rand.nextDouble()-0.5)*2*sceneRadius;
			p.y = (rand.nextDouble()-0.5)*2*sceneRadius;
			p.z = (rand.nextDouble()-0.5)*2*sceneRadius+sceneCenterZ;

			scene.add(p);
		}
	}

	public void createScenePlane() {
		scene = new ArrayList<Point3D_F64>();

		for( int i = 0; i < totalPoints; i++ ) {
			Point3D_F64 p = new Point3D_F64();

			p.x = (rand.nextDouble()-0.5)*2*sceneRadius;
			p.y = (rand.nextDouble()-0.5)*2*sceneRadius;
			p.z = sceneCenterZ;

			scene.add(p);
		}
	}

	public void motionTranslate() {
		motion = new Se3_F64();
		motion.getT().set(0.2,0,0);
	}

	public void motionTransRot() {
		motion = new Se3_F64();
		motion.getR().set(RotationMatrixGenerator.eulerXYZ(0.01,-0.02,0.05,null));
		motion.getT().set(0.2,0,0);
	}

	public void createObservations() {
		observations = new ArrayList<AssociatedPair>();

		for( Point3D_F64 p1 : scene ) {
			Point3D_F64 p2 = SePointOps_F64.transform(motion, p1, null);

			if( p1.z < 0 || p2.z < 0 )
				continue;

			AssociatedPair pair = new AssociatedPair();
			pair.keyLoc.set(p1.x/p1.z,p1.y/p1.z);
			pair.currLoc.set(p2.x/p2.z,p2.y/p2.z);
			observations.add(pair);

			// convert to pixels
			GeometryMath_F64.mult(K, pair.keyLoc, pair.keyLoc);
			GeometryMath_F64.mult(K, pair.currLoc,pair.currLoc);

			// add noise
			pair.keyLoc.x += rand.nextGaussian()*sigmaPixels;
			pair.keyLoc.y += rand.nextGaussian()*sigmaPixels;
			pair.currLoc.x += rand.nextGaussian()*sigmaPixels;
			pair.currLoc.y += rand.nextGaussian()*sigmaPixels;

			// if needed, convert back into normalized image coordinates
			if( !isPixels ) {
				GeometryMath_F64.mult(K_inv, pair.keyLoc, pair.keyLoc);
				GeometryMath_F64.mult(K_inv, pair.currLoc,pair.currLoc);
			}
		}
	}

	public void evaluateMinimal( EpipolarMatrixEstimatorN estimatorN ) {

		EpipolarMatrixEstimator estimator = new FundamentalNto1(estimatorN,1);

		scores = new ArrayList<Double>();
		int failed = 0;

		int numSamples = estimator.getMinimumPoints();

		Random rand = new Random(234);

		for( int i = 0; i < 50; i++ ) {
			List<AssociatedPair> pairs = new ArrayList<AssociatedPair>();

			// create a unique set of pairs
			while( pairs.size() < numSamples) {
				AssociatedPair p = observations.get( rand.nextInt(observations.size()) );

				if( !pairs.contains(p) ) {
					pairs.add(p);
				}
			}

			if( !estimator.process(pairs) ) {
				failed++;
				continue;
			}


			DenseMatrix64F F = estimator.getEpipolarMatrix();

			// normalize the scale of F
			CommonOps.scale(1.0/CommonOps.elementMaxAbs(F),F);

			double totalScore = 0;
			// score against all observations
			for( AssociatedPair p : observations ) {
				double score = Math.abs(GeometryMath_F64.innerProd(p.currLoc, F, p.keyLoc));
				if( Double.isNaN(score))
					System.out.println("Score is NaN");
				scores.add(score);
				totalScore += score;
			}
//			System.out.println("  score["+i+"] = "+totalScore);
		}

		Collections.sort(scores);

		System.out.printf(" Failures %3d  Score:  50%% = %6.3e  95%% = %6.3e\n", failed, scores.get(scores.size() / 2), scores.get((int) (scores.size() * 0.95)));
	}

	public void evaluateAll( EpipolarMatrixEstimator estimator ) {
		scores = new ArrayList<Double>();
		int failed = 0;

		for( int i = 0; i < 50; i++ ) {

			if( !estimator.process(observations) ) {
				failed++;
				continue;
			}

			DenseMatrix64F F = estimator.getEpipolarMatrix();

			// normalize the scale of F
			CommonOps.scale(1.0/CommonOps.elementMaxAbs(F),F);

			// score against all observations
			for( AssociatedPair p : observations ) {
				double score = Math.abs(GeometryMath_F64.innerProd(p.currLoc, F, p.keyLoc));
				if( Double.isNaN(score))
					System.out.println("Score is NaN");
				scores.add(score);
			}
		}

		Collections.sort(scores);

		System.out.printf(" Failures %3d  Score:  50%% = %6.3e  95%% = %6.3e\n", failed, scores.get(scores.size() / 2), scores.get((int) (scores.size() * 0.95)));
	}

	public static void main( String args[] ) {
		BenchmarkStabilityFundamental app = new BenchmarkStabilityFundamental();

		app.createSceneCube();
//		app.createScenePlane();
		app.motionTranslate();
		app.createObservations();
		app.evaluateMinimal(FactoryEpipolar.computeFundamentalMulti(8, isPixels));
		app.evaluateMinimal(FactoryEpipolar.computeFundamentalMulti(7, isPixels));
		app.evaluateMinimal(FactoryEpipolar.computeFundamentalMulti(5, isPixels));

//		app.evaluateMinimal(FactoryEpipolar.computeFundamental(8));


//		app.evaluateAll(FactoryEpipolar.computeEssential(8));
//		app.evaluateAll(FactoryEpipolar.computeEssential(5));
	}
}
