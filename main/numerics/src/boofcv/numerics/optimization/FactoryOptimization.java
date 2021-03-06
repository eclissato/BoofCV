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

package boofcv.numerics.optimization;

import boofcv.numerics.optimization.impl.*;
import boofcv.numerics.optimization.wrap.WrapLevenbergDampened;
import boofcv.numerics.optimization.wrap.WrapQuasiNewtonBFGS;
import boofcv.numerics.optimization.wrap.WrapTrustRegion;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolver;
import org.ejml.factory.LinearSolverFactory;

/**
 * Creates optimization algorithms using easy to use interfaces.  These implementations/interfaces
 * are designed to be easy to use and effective for most tasks.  If more control is needed then
 * create an implementation directly.
 *
 * @author Peter Abeles
 */
public class FactoryOptimization {
	/**
	 * <p>
	 * Creates a solver for the unconstrained minimization problem.  Here a function has N parameters
	 * and a single output.  The goal is the minimize the output given the function and its derivative.
	 * </p>
	 *
	 * @return UnconstrainedMinimization
	 */
	public static UnconstrainedMinimization unconstrained()
	{
		return new WrapQuasiNewtonBFGS();
	}

	/**
	 * <p>
	 * Unconstrained least squares Levenberg-Marquardt (LM) optimizer for dense problems.  There are many
	 * different variants of LM and this function provides an easy to use interface for selecting and
	 * configuring them.  Scaling of function parameters and output might be needed to ensure good results.
	 * </p>
	 *
	 * @param dampInit Initial value of dampening parameter.  Tune.  Start at around 1e-3.
	 * @param robust If true a slower, more robust algorithm that can handle more degenerate cases will be used.
	 * @return UnconstrainedLeastSquares
	 */
	public static UnconstrainedLeastSquares leastSquaresLM( double dampInit ,
															boolean robust )
	{
		LinearSolver<DenseMatrix64F> solver;

		if( robust ) {
			solver = LinearSolverFactory.pseudoInverse(true);
		} else {
			solver = LinearSolverFactory.symmPosDef(10);
		}

		LevenbergMarquardtDampened alg = new LevenbergMarquardtDampened(solver,dampInit);
		return new WrapLevenbergDampened(alg);
	}

	/**
	 * <p>
	 * Unconstrained least squares Levenberg optimizer for dense problems.  Some times works better than
	 * </p>
	 *
	 * @param dampInit Initial value of dampening parameter.  Tune.  Start at around 1e-3.
	 * @return UnconstrainedLeastSquares
	 */
	public static UnconstrainedLeastSquares leastSquareLevenberg( double dampInit )
	{
		LevenbergDampened alg = new LevenbergDampened(dampInit);
		return new WrapLevenbergDampened(alg);
	}

	/**
	 * Creates a trust region based optimization algorithm for least squares problem.
	 *
	 * @see TrustRegionLeastSquares
	 *
	 *
	 * @param regionSize
	 * @param type
	 * @return
	 */
	public static UnconstrainedLeastSquares leastSquaresTrustRegion( double regionSize ,
																	 RegionStepType type ,
																	 boolean robustSolver )
	{
		TrustRegionStep stepAlg;

		switch( type ) {
			case CAUCHY:
				stepAlg = new CauchyStep();
				break;

			case DOG_LEG_F:
				if( robustSolver )
					stepAlg = new DoglegStepF(LinearSolverFactory.pseudoInverse(true));
				else
					stepAlg = new DoglegStepF(LinearSolverFactory.leastSquaresQrPivot(true, false));
				break;

			case DOG_LEG_FTF:
				if( robustSolver )
					stepAlg = new DoglegStepFtF(LinearSolverFactory.pseudoInverse(true));
				else
					stepAlg = new DoglegStepFtF(LinearSolverFactory.leastSquaresQrPivot(true, false));
				break;

			default:
				throw new IllegalArgumentException("Unknown type = "+type);
		}
		
		TrustRegionLeastSquares alg = new TrustRegionLeastSquares(regionSize,stepAlg);

		return new WrapTrustRegion(alg);
	}
}
