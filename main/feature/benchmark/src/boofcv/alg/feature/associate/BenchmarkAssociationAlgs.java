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

import boofcv.abst.feature.associate.GeneralAssociation;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.misc.Performer;
import boofcv.misc.ProfileOperation;
import boofcv.struct.FastQueue;
import boofcv.struct.feature.TupleDescQueue;
import boofcv.struct.feature.TupleDesc_F64;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkAssociationAlgs {

	static final long TEST_TIME = 1000;
	static final Random rand = new Random(234234);
	static final int DOF = 10;
	static final int NUM_FEATURES = 2000;

	static final FastQueue<TupleDesc_F64> listA = createSet();
	static final FastQueue<TupleDesc_F64> listB = createSet();

	public static class General implements Performer {

		GeneralAssociation<TupleDesc_F64> alg;
		String name;

		public General(String name, GeneralAssociation<TupleDesc_F64> alg) {
			this.alg = alg;
			this.name = name;
		}

		@Override
		public void process() {
			alg.associate(listA,listB);
		}

		@Override
		public String getName() {
			return name;
		}
	}

	private static FastQueue<TupleDesc_F64> createSet() {
		FastQueue<TupleDesc_F64> ret = new TupleDescQueue<TupleDesc_F64>(TupleDesc_F64.class,DOF, true);

		for( int i = 0; i < NUM_FEATURES; i++ ) {
			TupleDesc_F64 t = ret.pop();
			for( int j = 0; j < DOF; j++ ) {
				t.value[j] = (rand.nextDouble()-0.5)*20;
			}
		}
		return ret;
	}

	public static void main( String argsp[ ] ) {
		System.out.println("=========  Profile Description Length "+DOF+" ========== Num Features "+NUM_FEATURES);
		System.out.println();

		ScoreAssociation<TupleDesc_F64> score = FactoryAssociation.scoreEuclidean(TupleDesc_F64.class,true);
		int maxMatches = 200;

		ProfileOperation.printOpsPerSec(new General("Greedy", FactoryAssociation.greedy(score, Double.MAX_VALUE, maxMatches, false)),TEST_TIME);
		ProfileOperation.printOpsPerSec(new General("Greedy Backwards", FactoryAssociation.greedy(score, Double.MAX_VALUE, maxMatches, true)),TEST_TIME);
		
	}
}
