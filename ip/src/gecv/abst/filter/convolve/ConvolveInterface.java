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

package gecv.abst.filter.convolve;

import gecv.abst.filter.FilterImageInterface;
import gecv.core.image.border.BorderType;
import gecv.struct.image.ImageBase;


/**
 * Generic interface for performing image convolutions.
 *
 * @author Peter Abeles
 */
public interface ConvolveInterface <Input extends ImageBase, Output extends ImageBase> 
		extends FilterImageInterface<Input,Output>
{
	/**
	 * Returns how the image border is handled.
	 */
	BorderType getBorderType();
}
