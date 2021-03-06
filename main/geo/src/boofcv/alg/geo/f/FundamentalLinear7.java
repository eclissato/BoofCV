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
import boofcv.alg.geo.UtilEpipolar;
import boofcv.numerics.solver.Polynomial;
import boofcv.numerics.solver.PolynomialRoots;
import boofcv.numerics.solver.PolynomialSolver;
import boofcv.numerics.solver.RootFinderType;
import boofcv.struct.FastQueue;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.SpecializedOps;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Computes the essential or fundamental matrix using exactly 7 points with linear algebra.  The number of required points
 * is reduced from 8 to 7 by enforcing the singularity constraint, det(F) = 0.  The number of solutions found is
 * either one or three depending on the number of real roots found in the quadratic.
 * </p>
 *
 * <p>
 * The computed fundamental matrix follow the following convention (with no noise) for the associated pair:
 * x2<sup>T</sup>*F*x1 = 0<br>
 * x1 = keyLoc and x2 = currLoc.
 * </p>
 *
 * <p>
 * References:
 * <ul>
 * <li> R. Hartley, and A. Zisserman, "Multiple View Geometry in Computer Vision", 2nd Ed, Cambridge 2003 </li>
 * </ul>
 *
 * @author Peter Abeles
 */
public class FundamentalLinear7 extends FundamentalLinear {

	// extracted from the null space of A
	protected DenseMatrix64F F1 = new DenseMatrix64F(3,3);
	protected DenseMatrix64F F2 = new DenseMatrix64F(3,3);

	// temporary storage for cubic coefficients
	private Polynomial poly = new Polynomial(4);
	PolynomialRoots rootFinger = PolynomialSolver.createRootFinder(RootFinderType.EVD,4);

	// where the found solutions are stored
	FastQueue<DenseMatrix64F> solutions;

	/**
	 * When computing the essential matrix normalization is optional because pixel coordinates
	 *
	 * @param computeFundamental true it computes a fundamental matrix and false for essential
	 */
	public FundamentalLinear7(boolean computeFundamental) {
		super(computeFundamental);

		solutions = new FastQueue<DenseMatrix64F>(3,DenseMatrix64F.class,false);
		for( int i = 0; i < 3; i++)
			solutions.data[i] = new DenseMatrix64F(3,3);
	}

	/**
	 * <p>
	 * Computes a fundamental or essential matrix from a set of associated point correspondences.
	 * </p>
	 *
	 * @param points List of corresponding image coordinates. In pixel for fundamental matrix or
	 *               normalized coordinates for essential matrix.
	 * @return true If successful or false if it failed
	 */
	public boolean process( List<AssociatedPair> points ) {
		if( points.size() != 7 )
			throw new IllegalArgumentException("Must be exactly 7 points. Not "+points.size()+" you gelatinous piece of pond scum.");

		// reset data structures
		solutions.reset();

		// must normalize for when points are in either pixel or calibrated units
		UtilEpipolar.computeNormalization(N1, N2, points);

		// extract F1 and F2 from two null spaces
		createA(points,A);

		if (process(A))
			return false;

		undoNormalizationF(F1,N1,N2);
		undoNormalizationF(F2,N1,N2);

		// compute polynomial coefficients
		computeCoefficients(F1, F2, poly.c);

		// Find polynomial roots and solve for Fundamental matrices
		computeSolutions();

		return true;
	}

	/**
	 * Set of found solutions.  Will be one or three.
	 *
	 * @return Solutions.
	 */
	public List<DenseMatrix64F> getSolutions() {
		return solutions.toList();
	}

	/**
	 * Computes the SVD of A and extracts the essential/fundamental matrix from its null space
	 */
	private boolean process(DenseMatrix64F A) {
		if( !svd.decompose(A) )
			return true;

		// extract the two singular vectors
		DenseMatrix64F V = svd.getV(null,false);
		SpecializedOps.subvector(V, 0, 7, V.numCols, false, 0, F1);
		SpecializedOps.subvector(V, 0, 8, V.numCols, false, 0, F2);

		return false;
	}

	/**
	 * <p>
	 * Find the polynomial roots and for each root compute the Fundamental matrix.
	 * Given the two matrices it will compute an alpha such that the determinant is zero.<br>
	 *
	 * det(&alpha*F1 + (1-&alpha;)*F2 ) = 0
	 * </p>

	 */
	public void computeSolutions()
	{
		if( !rootFinger.process(poly))
			return;

		List<Complex64F> zeros = rootFinger.getRoots();

		for( Complex64F c : zeros ) {
			if( !c.isReal() && Math.abs(c.imaginary) > 1e-10 )
				continue;

			DenseMatrix64F F = solutions.pop();

			double a = c.real;
			double b = 1-c.real;

			for( int i = 0; i < 9; i++ ) {
				F.data[i] = a*F1.data[i] + b*F2.data[i];
			}

			// Well these procedures might improve the quality of the result, but aren't strictly necessary
			// since the singularity constraints has already been applied
//			if( computeFundamental) {
//				if( !projectOntoFundamentalSpace(F) )
//					solutions.removeTail();
//			} else {
//				if( !projectOntoEssential(F) )
//					solutions.removeTail();
//			}
		}
	}

	/**
	 * <p>
	 * Computes the coefficients such that the following is true:<br>
	 *
	 * det(&alpha*F1 + (1-&alpha;)*F2 ) = c<sub>0</sub> + c<sub>1</sub>*&alpha; + c<sub>2</sub>*&alpha;<sup>2</sup>  + c<sub>2</sub>*&alpha;<sup>3</sup><br>
	 * </p>
	 *
	 * @param F1 a matrix
	 * @param F2 a matrix
	 * @param coefs Where results are returned.
	 */
	public static void computeCoefficients( DenseMatrix64F F1 ,
											DenseMatrix64F F2 ,
											double coefs[] )
	{
		Arrays.fill(coefs, 0);

		computeCoefficients(F1,F2,0,4,8,coefs,false);
		computeCoefficients(F1,F2,1,5,6,coefs,false);
		computeCoefficients(F1,F2,2,3,7,coefs,false);
		computeCoefficients(F1,F2,2,4,6,coefs,true);
		computeCoefficients(F1,F2,1,3,8,coefs,true);
		computeCoefficients(F1,F2,0,5,7,coefs,true);
	}

	public static void computeCoefficients( DenseMatrix64F F1 ,
											DenseMatrix64F F2 ,
											int i , int j , int k ,
											double coefs[] , boolean minus )
	{
		if( minus )
			computeCoefficientsMinus(F1.data[i], F1.data[j], F1.data[k], F2.data[i], F2.data[j], F2.data[k], coefs);
		else
			computeCoefficients(F1.data[i],F1.data[j],F1.data[k],F2.data[i],F2.data[j],F2.data[k],coefs);
	}

	public static void computeCoefficients( double x1 , double y1 , double z1 ,
											double x2 , double y2 , double z2 ,
											double coefs[] )
	{
		coefs[3] += x1*y1*z1 - x1*y1*z2 - x1*y2*z1 + x1*y2*z2 - x2*y1*z1 + x2*y1*z2 + x2*y2*z1 - x2*y2*z2;
		coefs[2] += x1*y1*z2 + x1*y2*z1 - 2*x1*y2*z2 + x2*y1*z1 - 2*x2*y1*z2 - 2*x2*y2*z1 + 3*x2*y2*z2;
		coefs[1] += x1*y2*z2 + x2*y1*z2 + x2*y2*z1 - 3*x2*y2*z2;
		coefs[0] += x2*y2*z2;
	}
	public static void computeCoefficientsMinus( double x1 , double y1 , double z1 ,
												 double x2 , double y2 , double z2 ,
												 double coefs[] )
	{
		coefs[3] -= x1*y1*z1 - x1*y1*z2 - x1*y2*z1 + x1*y2*z2 - x2*y1*z1 + x2*y1*z2 + x2*y2*z1 - x2*y2*z2;
		coefs[2] -= x1*y1*z2 + x1*y2*z1 - 2*x1*y2*z2 + x2*y1*z1 - 2*x2*y1*z2 - 2*x2*y2*z1 + 3*x2*y2*z2;
		coefs[1] -= x1*y2*z2 + x2*y1*z2 + x2*y2*z1 - 3*x2*y2*z2;
		coefs[0] -= x2*y2*z2;
	}
}
