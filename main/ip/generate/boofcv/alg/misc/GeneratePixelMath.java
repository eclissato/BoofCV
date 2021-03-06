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
public class GeneratePixelMath extends CodeGeneratorBase {

	String className = "PixelMath";

	private AutoTypeImage input;

	public void generate() throws FileNotFoundException {
		printPreamble();
		printAllSigned();
		printAll();
		out.println("}");
	}

	private void printPreamble() throws FileNotFoundException {
		setOutputFile(className);
		out.print("import boofcv.struct.image.*;\n" +
				"\n" +
				"import boofcv.alg.InputSanityCheck;\n" +
				"import java.util.Random;\n" +
				"\n" +
				"/**\n" +
				" * Standard mathematical operations performed on a per-pixel basis or computed across the whole image.\n" +
				" *\n" +
				" * DO NOT MODIFY: Generated by {@link boofcv.alg.misc.GeneratePixelMath}.\n"+
				" *\n"+
				" * @author Peter Abeles\n" +
				" */\n" +
				"public class "+className+" {\n\n");
	}

	public void printAll() {
		AutoTypeImage types[] = AutoTypeImage.getSpecificTypes();

		for( AutoTypeImage t : types ) {
			input = t;
			printMin();
			printMax();
			printMaxAbs();
			printDivide();
			printMult();
			printPlus();
			printBoundImage();
			printDiffAbs();
			printSum();
			printBandAve();
		}
	}

	public void printAllSigned() {
		AutoTypeImage types[] = AutoTypeImage.getSigned();

		for( AutoTypeImage t : types ) {
			input = t;
			printAbs();
		}
	}

	public void printAbs()
	{
		out.print("\t/**\n" +
				"\t * Sets each pixel in the output image to be the absolute value of the input image.\n" +
				"\t * Both the input and output image can be the same instance.\n" +
				"\t * \n" +
				"\t * @param input The input image. Not modified.\n" +
				"\t * @param output Where the absolute value image is written to. Modified.\n" +
				"\t */\n" +
				"\tpublic static void abs( "+ input.getImageName()+" input , "+ input.getImageName()+" output ) {\n" +
				"\n" +
				"\t\tInputSanityCheck.checkSameShape(input,output);\n" +
				"\t\t\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint indexSrc = input.startIndex + y* input.stride;\n" +
				"\t\t\tint indexDst = output.startIndex + y* output.stride;\n" +
				"\t\t\tint end = indexSrc + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; indexSrc < end; indexSrc++ , indexDst++) {\n" +
				"\t\t\t\toutput.data[indexDst] = "+input.getTypeCastFromSum()+"Math.abs(input.data[indexSrc]);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printMaxAbs() {
		out.print("\t/**\n" +
				"\t * Returns the absolute value of the element with the largest absolute value.\n" +
				"\t * \n" +
				"\t * @param input Input image. Not modified.\n" +
				"\t * @return Largest pixel absolute value.\n" +
				"\t */\n" +
				"\tpublic static "+input.getSumType()+" maxAbs( "+input.getImageName()+" input ) {\n" +
				"\n" +
				"\t\t"+input.getSumType()+" max = 0;\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint index = input.startIndex + y*input.stride;\n" +
				"\t\t\tint end = index + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; index < end; index++ ) {\n");
		if( input.isSigned() )
			out.print("\t\t\t\t"+input.getSumType()+" v = Math.abs(input.data[index]);\n");
		else
			out.print("\t\t\t\t"+input.getSumType()+" v = input.data[index]"+input.getBitWise()+";\n");
		out.print("\t\t\t\tif( v > max )\n" +
				"\t\t\t\t\tmax = v;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t\treturn max;\n" +
				"\t}\n\n");
	}

	public void printMax() {
		out.print("\t/**\n" +
				"\t * Returns the maximum element value.\n" +
				"\t * \n" +
				"\t * @param input Input image. Not modified.\n" +
				"\t * @return Maximum pixel value.\n" +
				"\t */\n" +
				"\tpublic static "+input.getSumType()+" max( "+input.getImageName()+" input ) {\n" +
				"\n" +
				"\t\t"+input.getSumType()+" max = input.get(0,0);\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint index = input.startIndex + y*input.stride;\n" +
				"\t\t\tint end = index + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; index < end; index++ ) {\n");
		out.print("\t\t\t\t"+input.getSumType()+" v = input.data[index] "+input.getBitWise()+";\n");
		out.print("\t\t\t\tif( v > max )\n" +
				"\t\t\t\t\tmax = v;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t\treturn max;\n" +
				"\t}\n\n");
	}

	public void printMin() {
		out.print("\t/**\n" +
				"\t * Returns the minimum element value.\n" +
				"\t * \n" +
				"\t * @param input Input image. Not modified.\n" +
				"\t * @return Minimum pixel value.\n" +
				"\t */\n" +
				"\tpublic static "+input.getSumType()+" min( "+input.getImageName()+" input ) {\n" +
				"\n" +
				"\t\t"+input.getSumType()+" min = input.get(0,0);\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint index = input.startIndex + y*input.stride;\n" +
				"\t\t\tint end = index + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; index < end; index++ ) {\n");
		out.print("\t\t\t\t"+input.getSumType()+" v = input.data[index] "+input.getBitWise()+";\n");
		out.print("\t\t\t\tif( v < min )\n" +
				"\t\t\t\t\tmin = v;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t\treturn min;\n" +
				"\t}\n\n");
	}

	public void printDivide() {

		String divisorType = input.isInteger() ? "double" : input.getSumType();

		out.print("\t/**\n" +
				"\t * Divides each element by the denominator. Both input and output images can\n" +
				"\t * be the same.\n" +
				"\t *\n" +
				"\t * @param input The input image. Not modified.\n" +
				"\t * @param output The output image. Modified.\n" +
				"\t * @param denominator What each element is divided by.\n" +
				"\t */\n" +
				"\tpublic static void divide( "+input.getImageName()+" input , "+input.getImageName()+" output, "+divisorType+" denominator ) {\n" +
				"\n" +
				"\t\tInputSanityCheck.checkSameShape(input,output);\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint indexSrc = input.startIndex + y* input.stride;\n" +
				"\t\t\tint indexDst = output.startIndex + y* output.stride;\n" +
				"\t\t\tint end = indexSrc + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; indexSrc < end; indexSrc++, indexDst++ ) {\n");
		if( input.isInteger() ) {
			String typeCast = "("+input.getDataType()+")";
			if( input.isSigned() )
				out.print("\t\t\t\toutput.data[indexDst] = "+typeCast+"((input.data[indexSrc] "+input.getBitWise()+")/ denominator);\n");
			else
				out.print("\t\t\t\toutput.data[indexDst] = "+typeCast+"(input.data[indexSrc] / denominator);\n");
		} else {
			out.print("\t\t\t\toutput.data[indexDst] = input.data[indexSrc] / denominator;\n");
		}
		out.print("\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printMult() {

		String scaleType = input.isInteger() ? "double" : input.getSumType();

		out.print("\t/**\n" +
				"\t * Multiplied each element by the scale factor. Both input and output images can\n" +
				"\t * be the same.\n" +
				"\t *\n" +
				"\t * @param input The input image. Not modified.\n" +
				"\t * @param output The output image. Modified.\n" +
				"\t * @param scale What each element is divided by.\n" +
				"\t */\n" +
				"\tpublic static void multiply( "+input.getImageName()+" input , "+input.getImageName()+" output, "+scaleType+" scale ) {\n" +
				"\n" +
				"\t\tInputSanityCheck.checkSameShape(input,output);\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint indexSrc = input.startIndex + y* input.stride;\n" +
				"\t\t\tint indexDst = output.startIndex + y* output.stride;\n" +
				"\t\t\tint end = indexSrc + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; indexSrc < end; indexSrc++, indexDst++ ) {\n");
		if( input.isInteger() ) {
			if( input.isSigned() )
				out.print("\t\t\t\t"+input.getSumType()+" val = ("+input.getSumType()+")(input.data[indexSrc] * scale);\n");
			else
				out.print("\t\t\t\t"+input.getSumType()+" val = ("+input.getSumType()+")((input.data[indexSrc] "+input.getBitWise()+")* scale);\n");
			if( input.getPrimitiveType() != int.class && input.getPrimitiveType() != long.class ) {
				out.print("\t\t\t\tif( val < "+input.getMin()+" ) val = "+input.getMin()+";\n" +
						"\t\t\t\telse if( val > "+input.getMax()+" ) val = "+input.getMax()+";\n");
			}
			out.print("\t\t\t\toutput.data[indexDst] = "+input.getTypeCastFromSum()+"val;\n");
		} else {
			out.print("\t\t\t\toutput.data[indexDst] = input.data[indexSrc] * scale;\n");
		}
		out.print("\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printPlus() {
		out.print("\t/**\n" +
				"\t * Each element has the specified number added to it. Both input and output images can\n" +
				"\t * be the same.\n" +
				"\t *\n" +
				"\t * @param input The input image. Not modified.\n" +
				"\t * @param output The output image. Modified.\n" +
				"\t * @param value What is added to each element.\n" +
				"\t */\n" +
				"\tpublic static void plus( "+input.getImageName()+" input , "+input.getImageName()+" output, "+input.getSumType()+" value ) {\n" +
				"\n" +
				"\t\tInputSanityCheck.checkSameShape(input,output);\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint indexSrc = input.startIndex + y* input.stride;\n" +
				"\t\t\tint indexDst = output.startIndex + y* output.stride;\n" +
				"\t\t\tint end = indexSrc + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; indexSrc < end; indexSrc++, indexDst++ ) {\n");
		if( input.isInteger() ) {
			if( input.isSigned() )
				out.print("\t\t\t\t"+input.getSumType()+" val = input.data[indexSrc] + value;\n");
			else
				out.print("\t\t\t\t"+input.getSumType()+" val = (input.data[indexSrc] "+input.getBitWise()+") + value;\n");
			if( input.getPrimitiveType() != int.class) {
				out.print("\t\t\t\tif( val < "+input.getMin()+" ) val = "+input.getMin()+";\n" +
						"\t\t\t\telse if( val > "+input.getMax()+" ) val = "+input.getMax()+";\n");
			}
			out.print("\t\t\t\toutput.data[indexDst] = "+input.getTypeCastFromSum()+"val;\n");
		} else {
			out.print("\t\t\t\toutput.data[indexDst] = input.data[indexSrc] + value;\n");
		}
		out.print("\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printBoundImage() {

		String bitWise = input.getBitWise();
		String sumType = input.getSumType();

		out.print("\t/**\n" +
				"\t * Bounds image pixels to be between these two values\n" +
				"\t * \n" +
				"\t * @param img Image\n" +
				"\t * @param min minimum value.\n" +
				"\t * @param max maximum value.\n" +
				"\t */\n" +
				"\tpublic static void boundImage( "+input.getImageName()+" img , "+sumType+" min , "+sumType+" max ) {\n" +
				"\t\tfinal int h = img.getHeight();\n" +
				"\t\tfinal int w = img.getWidth();\n" +
				"\n" +
				"\t\t"+input.getDataType()+"[] data = img.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint index = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\tint indexEnd = index+w;\n" +
				"\t\t\t// for(int x = 0; x < w; x++ ) {\n" +
				"\t\t\tfor (; index < indexEnd; index++) {\n" +
				"\t\t\t\t"+sumType+" value = data[index]"+bitWise+";\n" +
				"\t\t\t\tif( value < min )\n" +
				"\t\t\t\t\tdata[index] = "+input.getTypeCastFromSum()+"min;\n" +
				"\t\t\t\telse if( value > max )\n" +
				"\t\t\t\t\tdata[index] = "+input.getTypeCastFromSum()+"max;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printDiffAbs() {

		String bitWise = input.getBitWise();
		String typeCast = input.isInteger() ? "("+input.getDataType()+")" : "";

		out.print("\t/**\n" +
				"\t * <p>\n" +
				"\t * Computes the absolute value of the difference between each pixel in the two images.<br>\n" +
				"\t * d(x,y) = |img1(x,y) - img2(x,y)|\n" +
				"\t * </p>\n" +
				"\t * @param imgA Input image. Not modified.\n" +
				"\t * @param imgB Input image. Not modified.\n" +
				"\t * @param diff Absolute value of difference image. Modified.\n" +
				"\t */\n" +
				"\tpublic static void diffAbs( "+input.getImageName()+" imgA , "+input.getImageName()+" imgB , "+input.getImageName()+" diff ) {\n" +
				"\t\tInputSanityCheck.checkSameShape(imgA,imgB,diff);\n" +
				"\t\t\n" +
				"\t\tfinal int h = imgA.getHeight();\n" +
				"\t\tfinal int w = imgA.getWidth();\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint indexA = imgA.getStartIndex() + y * imgA.getStride();\n" +
				"\t\t\tint indexB = imgB.getStartIndex() + y * imgB.getStride();\n" +
				"\t\t\tint indexDiff = diff.getStartIndex() + y * diff.getStride();\n" +
				"\t\t\t\n" +
				"\t\t\tint indexEnd = indexA+w;\n" +
				"\t\t\t// for(int x = 0; x < w; x++ ) {\n" +
				"\t\t\tfor (; indexA < indexEnd; indexA++, indexB++, indexDiff++ ) {\n" +
				"\t\t\t\tdiff.data[indexDiff] = "+typeCast+"Math.abs((imgA.data[indexA] "+bitWise+") - (imgB.data[indexB] "+bitWise+"));\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printSum() {

		String bitWise = input.getBitWise();

		out.print("\t/**\n" +
				"\t * <p>\n" +
				"\t * Returns the sum of all the pixels in the image.\n" +
				"\t * </p>\n" +
				"\t * \n" +
				"\t * @param img Input image. Not modified.\n" +
				"\t */\n" +
				"\tpublic static "+input.getSumType()+" sum( "+input.getImageName()+" img ) {\n" +
				"\n" +
				"\t\tfinal int h = img.getHeight();\n" +
				"\t\tfinal int w = img.getWidth();\n" +
				"\n" +
				"\t\t"+input.getSumType()+" total = 0;\n" +
				"\t\t\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint index = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\t\n" +
				"\t\t\tint indexEnd = index+w;\n" +
				"\t\t\t// for(int x = 0; x < w; x++ ) {\n" +
				"\t\t\tfor (; index < indexEnd; index++ ) {\n" +
				"\t\t\t\ttotal += img.data[index] "+bitWise+";\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t\t\n" +
				"\t\treturn total;\n" +
				"\t}\n\n");
	}
	
	public void printBandAve() {
		
		String imageName = input.getImageName();
		String sumType = input.getSumType();
		String typecast = input.getTypeCastFromSum();
		String bitwise = input.getBitWise();
		
		out.print("\t/**\n" +
				"\t * Computes the average for each pixel across all bands in the {@link MultiSpectral} image.\n" +
				"\t * \n" +
				"\t * @param input MultiSpectral image\n" +
				"\t * @param output Gray scale image containing average pixel values\n" +
				"\t */\n" +
				"\tpublic static void bandAve( MultiSpectral<"+imageName+"> input , "+imageName+" output ) {\n" +
				"\t\tfinal int h = input.getHeight();\n" +
				"\t\tfinal int w = input.getWidth();\n" +
				"\n" +
				"\t\t"+imageName+"[] bands = input.bands;\n" +
				"\t\t\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint indexInput = input.getStartIndex() + y * input.getStride();\n" +
				"\t\t\tint indexOutput = output.getStartIndex() + y * output.getStride();\n" +
				"\n" +
				"\t\t\tint indexEnd = indexInput+w;\n" +
				"\t\t\t// for(int x = 0; x < w; x++ ) {\n" +
				"\t\t\tfor (; indexInput < indexEnd; indexInput++, indexOutput++ ) {\n" +
				"\t\t\t\t"+sumType+" total = 0;\n" +
				"\t\t\t\tfor( int i = 0; i < bands.length; i++ ) {\n" +
				"\t\t\t\t\ttotal += bands[i].data[ indexInput ]"+bitwise+";\n" +
				"\t\t\t\t}\n" +
				"\t\t\t\toutput.data[indexOutput] = "+typecast+"(total / bands.length);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public static void main( String args[] ) throws FileNotFoundException {
		GeneratePixelMath gen = new GeneratePixelMath();
		gen.generate();
	}
}
