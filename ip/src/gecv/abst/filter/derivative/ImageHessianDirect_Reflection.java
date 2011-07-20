/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.abst.filter.derivative;

import gecv.core.image.border.BorderType;
import gecv.core.image.border.FactoryImageBorder;
import gecv.core.image.border.ImageBorder;
import gecv.struct.GecvDefaults;
import gecv.struct.image.ImageBase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Generic implementation which uses reflections to call hessian functions
 *
 * @author Peter Abeles
 */
public class ImageHessianDirect_Reflection<Input extends ImageBase, Output extends ImageBase>
		implements ImageHessianDirect<Input, Output>
{
	// How the image border should be handled
	BorderType borderType = GecvDefaults.DERIV_BORDER_TYPE;
	ImageBorder<Output> border;
	// the image hessian function
	private Method m;

	public ImageHessianDirect_Reflection(Method m ) {
		this.m = m;
		setBorderType(borderType);
	}

	@Override
	public void process(Input inputImage , Output derivXX, Output derivYY, Output derivXY) {
		try {
			m.invoke(null,inputImage, derivXX, derivYY, derivXY, border);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBorderType(BorderType type) {
		this.borderType = type;
		Class<?> imageType = m.getParameterTypes()[0];
		border = FactoryImageBorder.general(imageType,borderType);
	}

	@Override
	public BorderType getBorderType() {
		return borderType;
	}

	@Override
	public int getBorder() {
		if( borderType != BorderType.SKIP )
			return 0;
		else
			return 1;
	}
}
