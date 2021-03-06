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

package boofcv.struct.image;

import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Standard tests for children of {@link ImageSingleBand}.  Ensures that they contain
 * all the expected functions and that they have the expected behavior.  This is done
 * through extensive use of reflections.
 *
 * @author Peter Abeles
 */
public abstract class StandardImageInterleavedTests {

	public Random rand = new Random(234);

	public abstract ImageInterleaved createImage(int width, int height, int numBands);

	public abstract Number randomNumber();

	/**
	 * Sets each element in the image to a random value.
	 */
	public void setRandom(ImageSingleBand img) {
		Object data = img._getData();

		int N = Array.getLength(data);
		for (int i = 0; i < N; i++) {
			Array.set(data, i, randomNumber());
		}
	}

	/**
	 * Checks to see if the implementation specific to ImageInterleavedTests
	 * works
	 */
	@Test
	public void isSubimage() {
		ImageInterleaved a = createImage(10, 20, 3);

		assertFalse(a.isSubimage());

		assertTrue(a.subimage(0, 5, 0, 5).isSubimage());
		assertTrue(a.subimage(2, 5, 2, 5).isSubimage());
	}

	/**
	 * Check for a positive case of get() and set()
	 */
	@Test
	public void get_set() {
		ImageInterleaved img = createImage(10, 20, 3);
		setRandom(img);

		Object expected = createPixelArray(img);
		Object orig = call(img, "get", 2, null, 1, 1);

		// make sure the two are not equal
		assertFalse(compareArrays(expected, orig, img.getNumBands()));

		// set the expected to the point in the image
		call(img, "set", 2, expected, 1, 1);
		Object found = call(img, "get", 2, null, 1, 1);
		assertTrue(compareArrays(expected, found, img.getNumBands()));
	}

	/**
	 * Check for a positive case of get() and set()
	 */
	@Test
	public void getBand_setBand() {
		ImageInterleaved img = createImage(10, 20, 2);
		setRandom(img);

		Number expected = randomNumber();
		Number orig = (Number) call(img, "getBand", 0, null, 1, 1, 0);

		// make sure the two are not equal
		assertFalse(expected.equals(orig));

		// set the expected to the point in the image
		call(img, "setBand", 1, expected, 1, 1, 0);
		Number found = (Number) call(img, "getBand", 0, null, 1, 1, 0);
		assertTrue(expected.equals(found));
	}

	/**
	 * Makes sure all the accessors do proper bounds checking
	 */
	@Test
	public void accessorBounds() {
		ImageInterleaved img = createImage(10, 20, 3);

		checkBound(img, "get", 2, null);
		checkBoundBand(img, "getBand", 0, null);
		checkBound(img, "set", 2, null);
		checkBoundBand(img, "setBand", 1, randomNumber());
	}

	private void checkBound(ImageInterleaved img, String method,
							int type, Object typeData) {
		checkException(img, method, type, typeData, -1, 0);
		checkException(img, method, type, typeData, 0, -1);
		checkException(img, method, type, typeData, img.getWidth(), 0);
		checkException(img, method, type, typeData, 0, img.getHeight());
	}

	private void checkBoundBand(ImageInterleaved img, String method,
								int type, Object typeData) {
		checkException(img, method, type, typeData, -1, 0, 0);
		checkException(img, method, type, typeData, 0, -1, 0);
		checkException(img, method, type, typeData, img.getWidth(), 0, 0);
		checkException(img, method, type, typeData, 0, img.getHeight(), 0);
		checkException(img, method, type, typeData, 0, 0, img.getNumBands());
	}

	private void checkException(ImageSingleBand img, String method,
								int type, Object typeData, int... where) {
		boolean found = false;
		try {
			call(img, method, type, typeData, where);
		} catch (ImageAccessException e) {
			found = true;
		}

		assertTrue("No exception was thrown", found);
	}

	private Object call(ImageSingleBand img, String method,
						int type, Object typeData, int... where) {
		try {
			Class<?>[] paramTypes = type == 0 ?
					new Class<?>[where.length] : new Class<?>[where.length + 1];
			Object[] args = new Object[paramTypes.length];

			int index;

			for (index = 0; index < where.length; index++) {
				paramTypes[index] = int.class;
				args[index] = where[index];
			}
			if (type == 1) {
				paramTypes[index] = img.getTypeInfo().getDataType();
				args[index] = typeData;
			} else if (type == 2) {
				String name = "[" + img.getTypeInfo().getDataType().getName().toUpperCase().charAt(0);
				paramTypes[index] = Class.forName(name);
				args[index] = typeData;
			}

			Method m = img.getClass().getMethod(method, paramTypes);

			return m.invoke(img, args);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			fail("The method " + method + " needs to be implemented");
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw (RuntimeException) e.getCause();
		}
		throw new RuntimeException("Shouldn't be here");
	}

	private Object createPixelArray(ImageInterleaved img) {
		int numBands = img.getNumBands();

		Object ret = Array.newInstance(img.getTypeInfo().getDataType(), numBands);

		for (int i = 0; i < numBands; i++)
			Array.set(ret, i, randomNumber());

		return ret;
	}

	private boolean compareArrays(Object a, Object b, int length) {
		for (int i = 0; i < length; i++) {
			Number valA = (Number) Array.get(a, i);
			Number valB = (Number) Array.get(b, i);

			if (!valA.equals(valB))
				return false;
		}
		return true;
	}

}
