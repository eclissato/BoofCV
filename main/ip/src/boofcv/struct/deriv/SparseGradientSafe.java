/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://www.boofcv.org).
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

package boofcv.struct.deriv;

import boofcv.struct.image.ImageSingleBand;

/**
 * Wraps around other {@link SparseImageGradient} classes and checks to see if
 * the image is in bounds or not.  If not the gradient is set to one.
 * 
 * @author Peter Abeles
 */
public class SparseGradientSafe<T extends ImageSingleBand, G extends GradientValue> 
	implements SparseImageGradient<T,G>
{
	SparseImageGradient<T,G> wrap;
	G zero;
	
	public SparseGradientSafe(SparseImageGradient<T, G> wrap) {
		this.wrap = wrap;

		try {
			zero = (G) wrap.getGradientType().newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setImage(T input) {
		wrap.setImage(input);
	}

	@Override
	public boolean isInBounds(int x, int y) {
		return wrap.isInBounds(x,y);
	}

	@Override
	public G compute(int x, int y) {                                                           
		if( wrap.isInBounds(x,y))
			return wrap.compute(x,y);
		else
			return zero;
	}

	@Override
	public Class<G> getGradientType() {
		return wrap.getGradientType();
	}
}