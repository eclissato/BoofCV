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

package boofcv.alg.feature.associate;

import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.struct.FastQueue;
import boofcv.struct.feature.TupleDesc_F64;
import pja.storage.GrowQueue_F64;
import pja.storage.GrowQueue_I32;


/**
 * <p>
 * Brute force greedy association for objects described by a {@link TupleDesc_F64}.  An
 * object is associated with whichever object has the best fit score and every possible combination
 * is examined.  If there are a large number of features this can be quite slow.
 * </p>
 *
 * <p>
 * Optionally, backwards validation can be used to reduce the number of false associations.
 * Backwards validation works by checking to see if two objects are mutually the best association
 * for each other.  First an association is found from src to dst, then the best fit in dst is
 * associated with feature in src.
 * </p>
 *
 * @author Peter Abeles
 */
public class AssociateGreedy<T> {

	// computes association score
	private ScoreAssociation<T> score;
	// worst allowed fit score to associate
	private double maxFitError;
	// stores the quality of fit score
	private GrowQueue_F64 fitQuality = new GrowQueue_F64(100);
	// stores indexes of associated
	private GrowQueue_I32 pairs = new GrowQueue_I32(100);
	// various
	private GrowQueue_F64 workBuffer = new GrowQueue_F64(100);
	// if true backwardsValidation is done
	private boolean backwardsValidation;

	/**
	 * Configure association
	 *
	 * @param score Computes the association score.
	 * @param maxFitError Maximum allowed fit error.  To disable set to Double.MAX_VALUE
	 * @param backwardsValidation If true then backwards validation is performed.
	 */
	public AssociateGreedy(ScoreAssociation<T> score,
						   double maxFitError,
						   boolean backwardsValidation) {
		this.score = score;
		this.maxFitError = maxFitError;
		this.backwardsValidation = backwardsValidation;
	}

	/**
	 * Associates the two sets objects against each other by minimizing fit score.
	 *
	 * @param src Source list.
	 * @param dst Destination list.
	 */
	public void associate( FastQueue<T> src ,
						   FastQueue<T> dst )
	{
		fitQuality.reset();
		pairs.reset();
		workBuffer.reset();

		for( int i = 0; i < src.size; i++ ) {
			T a = src.data[i];
			double bestScore = maxFitError;
			int bestIndex = -1;

			for( int j = 0; j < dst.size; j++ ) {
				T b = dst.data[j];

				double fit = score.score(a,b);
				workBuffer.push(fit);

				if( fit < bestScore ) {
					bestIndex = j;
					bestScore = fit;
				}
			}
			pairs.push(bestIndex);
			fitQuality.push(bestScore);
		}

		if( backwardsValidation ) {
			for( int i = 0; i < src.size; i++ ) {
				int match = pairs.data[i];
				if( match == -1 )
					continue;

				double scoreToBeat = workBuffer.data[i*dst.size+match];

				for( int j = 0; j < src.size; j++ , match += dst.size ) {
					if( workBuffer.data[match] < scoreToBeat ) {
						pairs.data[i] = -1;
						fitQuality.data[i] = Double.MAX_VALUE;
						break;
					}
				}
			}
		}
	}

	/**
	 * Returns a list of association pairs.  Each element in the returned list corresponds
	 * to an element in the src list.  The value contained in the index indicate which element
	 * in the dst list that object was associated with.  If a value of -1 is stored then
	 * no association was found.
	 *
	 * @return Array containing associations by src index.
	 */
	public int[] getPairs() {
		return pairs.data;
	}

	/**
	 * Quality of fit scores for each association.  Lower fit scores are better.
	 *
	 * @return Array of fit sources by src index.
	 */
	public double[] getFitQuality() {
		return fitQuality.data;
	}
}
