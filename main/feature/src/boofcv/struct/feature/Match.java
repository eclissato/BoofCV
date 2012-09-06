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

package boofcv.struct.feature;

import georegression.struct.point.Point2D_I32;

/**
 * Found match during template matching.
 *
 * @author Peter Abeles
 */
public class Match extends Point2D_I32 {
	public double score;

	public Match(int x, int y, double score) {
		this.x = x;
		this.y = y;
		this.score = score;
	}

	public Match() {
	}

	@Override
	public String toString() {
		return "Match{x=" + x + ",y=" + y + ",score=" + score + "}";
	}
}
