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

package boofcv.alg.misc;

import boofcv.misc.AutoTypeImage;
import boofcv.misc.CodeGeneratorBase;

import java.io.FileNotFoundException;


/**
 * Generates functions inside of {@link boofcv.alg.misc.ImageTestingOps}.
 *
 * @author Peter Abeles
 */
public class GenerateImageTestingOps extends CodeGeneratorBase {

	String className = "ImageTestingOps";

	private AutoTypeImage imageType;
	private String imageName;
	private String dataType;
	private String bitWise;

	public void generate() throws FileNotFoundException {
		printPreamble();
		printAllGeneric();
		printAllSpecific();
		out.println("}");
	}

	private void printPreamble() throws FileNotFoundException {
		setOutputFile(className);
		out.print("import boofcv.struct.image.*;\n" +
				"\n" +
				"import java.util.Random;\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Image operations which are primarily used for testing and evaluation.\n" +
				" *\n" +
				" * DO NOT MODIFY: Generated by {@link boofcv.alg.misc.GenerateImageTestingOps}.\n"+
				" *\n"+
				" * @author Peter Abeles\n" +
				" */\n" +
				"public class "+className+" {\n\n");
	}

	public void printAllGeneric() {
		AutoTypeImage types[] = AutoTypeImage.getGenericTypes();

		for( AutoTypeImage t : types ) {
			imageType = t;
			imageName = t.getImageName();
			dataType = t.getDataType();
			printFill();
			printFillRectangle();
			printRandomize();
			printComputeMSE();
			printFlipVertical();
		}
	}

	public void printAllSpecific() {
		AutoTypeImage types[] = AutoTypeImage.getSpecificTypes();

		for( AutoTypeImage t : types ) {
			imageType = t;
			imageName = t.getImageName();
			dataType = t.getDataType();
			bitWise = t.getBitWise();
			printAddUniform();
			printAddGaussian();
		}
	}

	public void printFill()
	{
		String typeCast = imageType.getTypeCastFromSum();
		out.print("/**\n" +
				"\t * Fills the whole image with the specified pixel value\n" +
				"\t *\n" +
				"\t * @param img   An image.\n" +
				"\t * @param value The value that the image is being filled with.\n" +
				"\t */\n" +
				"\tpublic static void fill("+imageName+" img, "+imageType.getSumType()+" value) {\n" +
				"\t\tfinal int h = img.getHeight();\n" +
				"\t\tfinal int w = img.getWidth();\n" +
				"\n" +
				"\t\t"+dataType+"[] data = img.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint index = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\tfor (int x = 0; x < w; x++) {\n" +
				"\t\t\t\tdata[index++] = "+typeCast+"value;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printFillRectangle()
	{
		out.print("\t/**\n" +
				"\t * Sets a rectangle inside the image with the specified value.\n" +
				"\t */\n" +
				"\tpublic static void fillRectangle("+imageName+" img, "+imageType.getSumType()+" value, int x0, int y0, int width, int height) {\n" +
				"\t\tint x1 = x0 + width;\n" +
				"\t\tint y1 = y0 + height;\n" +
				"\n" +
				"\t\tfor (int y = y0; y < y1; y++) {\n" +
				"\t\t\tfor (int x = x0; x < x1; x++) {\n" +
				"\t\t\t\tif( img.isInBounds(x,y ))\n" +
				"\t\t\t\t\timg.set(x, y, value);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printRandomize() {

		String sumType = imageType.getSumType();
		String typeCast = imageType.getTypeCastFromSum();

		out.print("\t/**\n" +
				"\t * Sets each value in the image to a value drawn from an uniform distribution that has a range of min <= X < max.\n" +
				"\t */\n" +
				"\tpublic static void randomize("+imageName+" img, Random rand , "+sumType+" min , "+sumType+" max) {\n" +
				"\t\tfinal int h = img.getHeight();\n" +
				"\t\tfinal int w = img.getWidth();\n" +
				"\n" +
				"\t\t"+sumType+" range = max-min;\n" +
				"\n" +
				"\t\t"+dataType+"[] data = img.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint index = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\tfor (int x = 0; x < w; x++) {\n");
		if( imageType.isInteger() && imageType.getNumBits() < 64) {
			out.print("\t\t\t\tdata[index++] = "+typeCast+"(rand.nextInt(range)+min);\n");
		} else if( imageType.isInteger() ) {
			out.print("\t\t\t\tdata[index++] = rand.nextInt((int)range)+min;\n");
		} else {
			String randType = imageType.getRandType();
			out.print("\t\t\t\tdata[index++] = rand.next"+randType+"()*range+min;\n");
		}
		out.print("\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printAddUniform() {

		String sumType = imageType.getSumType();
		int min = imageType.getMin().intValue();
		int max = imageType.getMax().intValue();
		String typeCast = imageType.getTypeCastFromSum();

		out.print("\t/**\n" +
				"\t * Adds uniform i.i.d noise to each pixel in the image.  Noise range is min <= X < max.\n" +
				"\t */\n" +
				"\tpublic static void addUniform("+imageName+" img, Random rand , "+sumType+" min , "+sumType+" max) {\n" +
				"\t\tfinal int h = img.getHeight();\n" +
				"\t\tfinal int w = img.getWidth();\n" +
				"\n" +
				"\t\t"+sumType+" range = max-min;\n" +
				"\n" +
				"\t\t"+dataType+"[] data = img.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint index = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\tfor (int x = 0; x < w; x++) {\n");
		if( imageType.isInteger() && imageType.getNumBits() != 64) {
			out.print("\t\t\t\t"+sumType+" value = (data[index] "+bitWise+") + rand.nextInt(range)+min;\n");
			if( imageType.getNumBits() < 32 ) {
				out.print("\t\t\t\tif( value < "+min+" ) value = "+min+";\n" +
						"\t\t\t\tif( value > "+max+" ) value = "+max+";\n" +
						"\n");
			}
		} else if( imageType.isInteger() ) {
			out.print("\t\t\t\t"+sumType+" value = data[index] + rand.nextInt((int)range)+min;\n");
		} else {
			String randType = imageType.getRandType();
			out.print("\t\t\t\t"+sumType+" value = data[index] + rand.next"+randType+"()*range+min;\n");
		}
		out.print("\t\t\t\tdata[index++] = "+typeCast+" value;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printAddGaussian() {
		String sumType = imageType.getSumType();
		String typeCast = imageType.getTypeCastFromSum();
		String sumCast = sumType.equals("double") ? "" : "("+sumType+")";

		out.print("\t/**\n" +
				"\t * Adds Gaussian/normal i.i.d noise to each pixel in the image.\n" +
				"\t */\n" +
				"\tpublic static void addGaussian("+imageName+" img, Random rand , double sigma , "+sumType+" min , "+sumType+" max ) {\n" +
				"\t\tfinal int h = img.getHeight();\n" +
				"\t\tfinal int w = img.getWidth();\n" +
				"\n" +
				"\t\t"+dataType+"[] data = img.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint index = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\tfor (int x = 0; x < w; x++) {\n");
		out.print("\t\t\t\t"+sumType+" value = (data[index] "+bitWise+") + "+sumCast+"(rand.nextGaussian()*sigma);\n");
		out.print("\t\t\t\tif( value < min ) value = min;\n" +
				"\t\t\t\tif( value > max ) value = max;\n" +
				"\n");
		out.print("\t\t\t\tdata[index++] = "+typeCast+" value;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printComputeMSE() {

		out.print("\t/**\n" +
				"\t * <p>Computes the mean squared error (MSE) between the two images.</p>\n" +
				"\t *\n" +
				"\t * @param imgA first image. Not modified.\n" +
				"\t * @param imgB second image. Not modified.\n" +
				"\t * @return error between the two images.\n" +
				"\t */\n" +
				"\tpublic static double computeMeanSquaredError("+imageName+" imgA, "+imageName+" imgB ) {\n" +
				"\t\tfinal int h = imgA.getHeight();\n" +
				"\t\tfinal int w = imgA.getWidth();\n" +
				"\n" +
				"\t\tdouble total = 0;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tfor (int x = 0; x < w; x++) {\n" +
				"\t\t\t\tdouble difference =  imgA.get(x,y)-imgB.get(x,y);\n" +
				"\t\t\t\ttotal += difference*difference;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\n" +
				"\t\treturn total / (w*h);\n" +
				"\t}\n\n");
	}

	public void printFlipVertical() {
		String sumType = imageType.getSumType();

		out.print("\t/**\n" +
				"\t * Flips the image from top to bottom\n" +
				"\t */\n" +
				"\tpublic static void flipVertical( "+imageName+" img ) {\n" +
				"\t\tint h2 = img.height/2;\n" +
				"\n" +
				"\t\tfor( int y = 0; y < h2; y++ ) {\n" +
				"\t\t\tint index1 = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\tint index2 = img.getStartIndex() + (img.height - y - 1) * img.getStride();\n" +
				"\n" +
				"\t\t\tint end = index1 + img.width;\n" +
				"\n" +
				"\t\t\twhile( index1 < end ) {\n" +
				"\t\t\t\t"+sumType+" tmp = img.data[index1];\n" +
				"\t\t\t\timg.data[index1++] = img.data[index2];\n" +
				"\t\t\t\timg.data[index2++] = ("+dataType+")tmp;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}


	public static void main( String args[] ) throws FileNotFoundException {
		GenerateImageTestingOps gen = new GenerateImageTestingOps();
		gen.generate();
	}
}
