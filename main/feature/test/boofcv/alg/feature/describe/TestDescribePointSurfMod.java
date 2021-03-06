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

package boofcv.alg.feature.describe;

import boofcv.struct.image.ImageFloat32;


/**
 * @author Peter Abeles
 */
public class TestDescribePointSurfMod  extends BaseTestDescribeSurf<ImageFloat32,ImageFloat32> {

	public TestDescribePointSurfMod() {
		super(ImageFloat32.class,ImageFloat32.class);
	}

	@Override
	public DescribePointSurf<ImageFloat32> createAlg() {
		return new DescribePointSurfMod<ImageFloat32>(ImageFloat32.class);
	}
}
