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

import boofcv.numerics.optimization.impl.LevenbergMarquardtDampened;
import boofcv.numerics.optimization.wrap.WrapLevenbergDampened;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolver;
import org.ejml.factory.LinearSolverFactory;

/**
 * @author Peter Abeles
 */
public class EvaluateLevenbergMarquardtDampened extends UnconstrainedLeastSquaresEvaluator {

	boolean robust = true;
	double dampInit = 1e-3;

	public EvaluateLevenbergMarquardtDampened(boolean verbose) {
		super(verbose, false);
	}

	@Override
	protected UnconstrainedLeastSquares createSearch(double minimumValue) {
		LinearSolver<DenseMatrix64F> solver;

		if( robust ) {
			solver = LinearSolverFactory.pseudoInverse(true);
		} else {
			solver = LinearSolverFactory.symmPosDef(10);
		}

		LevenbergMarquardtDampened alg = new LevenbergMarquardtDampened(solver,dampInit);
		return new WrapLevenbergDampened(alg);
	}

	public static void main( String args[] ) {
		EvaluateLevenbergMarquardtDampened eval = new EvaluateLevenbergMarquardtDampened(false);

		System.out.println("Powell              ----------------");
		eval.powell();
		System.out.println("Helical Valley      ----------------");
		eval.helicalValley();
		System.out.println("Rosenbrock          ----------------");
		eval.rosenbrock();
		System.out.println("Rosenbrock Mod      ----------------");
		eval.rosenbrockMod(Math.sqrt(2*1e6));
		System.out.println("variably            ----------------");
		eval.variably();
		System.out.println("trigonometric       ----------------");
		eval.trigonometric();
		System.out.println("Bady Scaled Brown   ----------------");
		eval.badlyScaledBrown();
	}
}
