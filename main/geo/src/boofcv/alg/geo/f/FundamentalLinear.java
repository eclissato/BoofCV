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
import georegression.struct.point.Point2D_F64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.SingularValueDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.ops.SingularOps;
import org.ejml.simple.SimpleMatrix;

import java.util.List;

/**
 * <p>
 * Base class for linear algebra based algorithms for computing the Fundamental/Essential matrices.
 * </p>
 *
 * <p>
 * The computed fundamental matrix follow the following convention (with no noise) for the associated pair:
 * x2<sup>T</sup>*F*x1 = 0<br>
 * x1 = keyLoc and x2 = currLoc.
 * </p>
 *
 * @author Peter Abeles
 */
public abstract class FundamentalLinear {

	// contains the set of equations that are solved
	protected DenseMatrix64F A = new DenseMatrix64F(1,9);
	protected SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(0, 0, true, true, false);

	// SVD decomposition of F = U*S*V^T
	protected DenseMatrix64F svdU;
	protected DenseMatrix64F svdS;
	protected DenseMatrix64F svdV;

	protected DenseMatrix64F temp0 = new DenseMatrix64F(3,3);

	// matrix used to normalize results
	protected DenseMatrix64F N1 = new DenseMatrix64F(3,3);
	protected DenseMatrix64F N2 = new DenseMatrix64F(3,3);

	// should it compute a fundamental (true) or essential (false) matrix?
	boolean computeFundamental;

	/**
	 * Specifies which type of matrix is to be computed
	 *
	 * @param computeFundamental true it computes a fundamental matrix and false for essential
	 */
	public FundamentalLinear( boolean computeFundamental ) {
		this.computeFundamental = computeFundamental;
	}

	/**
	 * Projects the found estimate of E onto essential space.
	 *
	 * @return true if svd returned true.
	 */
	protected boolean projectOntoEssential( DenseMatrix64F E ) {
		if( !svd.decompose(E) ) {
			return false;
		}
		svdV = svd.getV(svdV,false);
		svdU = svd.getU(svdU,false);
		svdS = svd.getW(svdS);

		SingularOps.descendingOrder(svdU, false, svdS, svdV, false);

		// project it into essential space
		// the scale factor is arbitrary, but the first two singular values need
		// to be the same.  so just set them to one
		svdS.unsafe_set(0, 0, 1);
		svdS.unsafe_set(1, 1, 1);
		svdS.unsafe_set(2, 2, 0);

		// recompute F
		CommonOps.mult(svdU, svdS, temp0);
		CommonOps.multTransB(temp0,svdV, E);

		return true;
	}

	/**
	 * Projects the found estimate of F onto Fundamental space.
	 *
	 * @return true if svd returned true.
	 */
	protected boolean projectOntoFundamentalSpace( DenseMatrix64F F ) {
		if( !svd.decompose(F) ) {
			return false;
		}
		svdV = svd.getV(null,false);
		svdU = svd.getU(null,false);
		svdS = svd.getW(null);

		SingularOps.descendingOrder(svdU, false, svdS, svdV, false);

		// the smallest singular value needs to be set to zero, unlike
		svdS.set(2, 2, 0);

		// recompute F
		CommonOps.mult(svdU, svdS, temp0);
		CommonOps.multTransB(temp0,svdV, F);

		return true;
	}

	/**
	 * Undo the normalization done to the input matrices for a Fundamental matrix.
	 * <br>
	 * M = N<sub>2</sub><sup>T</sup>*M*N<sub>1</sub>
	 *
	 * @param M Either the homography or fundamental matrix computed from normalized points.
	 * @param N1 normalization matrix.
	 * @param N2 normalization matrix.
	 */
	protected void undoNormalizationF(DenseMatrix64F M, DenseMatrix64F N1, DenseMatrix64F N2) {
		SimpleMatrix a = SimpleMatrix.wrap(M);
		SimpleMatrix b = SimpleMatrix.wrap(N1);
		SimpleMatrix c = SimpleMatrix.wrap(N2);

		SimpleMatrix result = c.transpose().mult(a).mult(b);

		M.set(result.getMatrix());
	}

	/**
	 * Reorganizes the epipolar constraint equation (x<sup>T</sup><sub>2</sub>*F*x<sub>1</sub> = 0) such that it
	 * is formulated as a standard linear system of the form Ax=0.  Where A contains the pixel locations and x is
	 * the reformatted fundamental matrix.
	 *
	 * @param points Set of associated points in left and right images.
	 * @param A Matrix where the reformatted points are written to.
	 */
	protected void createA(List<AssociatedPair> points, DenseMatrix64F A ) {
		A.reshape(points.size(),9, false);
		A.zero();

		Point2D_F64 f_norm = new Point2D_F64();
		Point2D_F64 s_norm = new Point2D_F64();

		final int size = points.size();
		for( int i = 0; i < size; i++ ) {
			AssociatedPair p = points.get(i);

			Point2D_F64 f = p.keyLoc;
			Point2D_F64 s = p.currLoc;

			// normalize the points
			UtilEpipolar.pixelToNormalized(f, f_norm, N1);
			UtilEpipolar.pixelToNormalized(s, s_norm, N2);

			// perform the Kronecker product with the two points being in
			// homogeneous coordinates (z=1)
			A.set(i,0,s_norm.x*f_norm.x);
			A.set(i,1,s_norm.x*f_norm.y);
			A.set(i,2,s_norm.x);
			A.set(i,3,s_norm.y*f_norm.x);
			A.set(i,4,s_norm.y*f_norm.y);
			A.set(i,5,s_norm.y);
			A.set(i,6,f_norm.x);
			A.set(i,7,f_norm.y);
			A.set(i,8,1);
		}
	}

	/**
	 * Returns the U from the SVD of F.
	 *
	 * @return U matrix.
	 */
	public DenseMatrix64F getSvdU() {
		return svdU;
	}

	/**
	 * Returns the S from the SVD of F.
	 *
	 * @return S matrix.
	 */
	public DenseMatrix64F getSvdS() {
		return svdS;
	}

	/**
	 * Returns the V from the SVD of F.
	 *
	 * @return V matrix.
	 */
	public DenseMatrix64F getSvdV() {
		return svdV;
	}

	/**
	 * Returns true if it is computing a fundamental matrix or false if it is an essential matrix.
	 * @return true for fundamental and false for essential
	 */
	public boolean isComputeFundamental() {
		return computeFundamental;
	}

}
