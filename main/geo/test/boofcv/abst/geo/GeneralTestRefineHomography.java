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

package boofcv.abst.geo;

import boofcv.alg.geo.h.CommonHomographyChecks;
import boofcv.alg.geo.h.HomographyLinear4;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class GeneralTestRefineHomography extends CommonHomographyChecks {

	public abstract RefineEpipolarMatrix createAlgorithm();

	@Test
	public void perfectInput() {
		createScene(30,false);

		// use the linear algorithm to compute the homography
		HomographyLinear4 estimator = new HomographyLinear4(true);
		estimator.process(pairs);
		DenseMatrix64F H = estimator.getHomography();

		RefineEpipolarMatrix alg = createAlgorithm();

		//give it the perfect matrix and see if it screwed it up
		assertTrue(alg.process(H, pairs));

		DenseMatrix64F found = alg.getRefinement();

		// normalize so that they are the same
		CommonOps.divide(H.get(2, 2), H);
		CommonOps.divide(found.get(2, 2), found);

		assertTrue(MatrixFeatures.isEquals(H, found, 1e-8));
	}

	@Test
	public void incorrectInput() {
		createScene(30,false);

		// use the linear algorithm to compute the homography
		HomographyLinear4 estimator = new HomographyLinear4(true);
		estimator.process(pairs);
		DenseMatrix64F H = estimator.getHomography();

		RefineEpipolarMatrix alg = createAlgorithm();

		//give it the perfect matrix and see if it screwed it up
		DenseMatrix64F Hmod = H.copy();
		Hmod.data[0] += 0.1;
		Hmod.data[5] += 0.1;
		assertTrue(alg.process(Hmod, pairs));

		DenseMatrix64F found = alg.getRefinement();

		// normalize to allow comparison
		CommonOps.divide(H.get(2,2),H);
		CommonOps.divide(Hmod.get(2,2),Hmod);
		CommonOps.divide(found.get(2,2),found);

		double error0 = 0;
		double error1 = 0;

		// very crude error metric
		for( int i = 0; i < 9; i++ ) {
			error0 += Math.abs(Hmod.data[i]-H.data[i]);
			error1 += Math.abs(found.data[i]-H.data[i]);
		}

//		System.out.println("error "+error1);
		assertTrue(error1 < error0);
	}
}
