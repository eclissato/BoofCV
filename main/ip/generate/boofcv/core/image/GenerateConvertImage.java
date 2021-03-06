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

package boofcv.core.image;

import boofcv.misc.AutoTypeImage;
import boofcv.misc.CodeGeneratorBase;
import boofcv.misc.CodeGeneratorUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


/**
 * @author Peter Abeles
 */
public class GenerateConvertImage extends CodeGeneratorBase {

	String className = "ConvertImage";

	PrintStream out;

	public GenerateConvertImage() throws FileNotFoundException {
		out = new PrintStream(new FileOutputStream(className + ".java"));
	}

	@Override
	public void generate() throws FileNotFoundException {
		printPreamble();

		for( AutoTypeImage in : AutoTypeImage.getSpecificTypes()) {
			for( AutoTypeImage out : AutoTypeImage.getSpecificTypes() ) {
				if( in == out )
					continue;

				printConvert(in,out);
			}
		}

		out.print("\n" +
				"}\n");
	}

	private void printPreamble() {
		out.print(CodeGeneratorUtil.copyright);
		out.print("package boofcv.core.image;\n" +
				"\n" +
				"import boofcv.alg.InputSanityCheck;\n" +
				"import boofcv.core.image.impl.ImplConvertImage;\n" +
				"import boofcv.struct.image.*;\n" +
				"\n" +
				"/**\n" +
				" * <p>\n" +
				" * Functions for converting between different image types. Numerical values do not change or are closely approximated\n" +
				" * in these functions. If an output image is not specified then a new instance is declared and returned.\n" +
				" * </p>\n" +
				" *\n" +
				" * <p>\n" +
				" * DO NOT MODIFY: This class was automatically generated by {@link boofcv.core.image.impl.GenerateConvertImage}\n" +
				" * </p>\n" +
				" *\n" +
				" * @author Peter Abeles\n" +
				" */" +
				"public class "+className+" {\n\n");
	}

	private void printConvert( AutoTypeImage imageIn , AutoTypeImage imageOut ) {

		out.print("\t/**\n" +
				"\t * <p>\n" +
				"\t * Converts an {@link boofcv.struct.image."+imageIn.getImageName()+"} into a {@link boofcv.struct.image."+imageOut.getImageName()+"}.\n" +
				"\t * </p>\n" +
				"\t *\n" +
				"\t * @param input Input image which is being converted. Not modified.\n" +
				"\t * @param output The output image.  If null a new image is created. Modified.\n" +
				"\t */\n" +
				"\tpublic static "+imageOut.getImageName()+" convert("+imageIn.getImageName()+" input, "+imageOut.getImageName()+" output) {\n" +
				"\t\tif (output == null) {\n" +
				"\t\t\toutput = new "+imageOut.getImageName()+"(input.width, input.height);\n" +
				"\t\t} else {\n" +
				"\t\t\tInputSanityCheck.checkSameShape(input, output);\n" +
				"\t\t}\n" +
				"\n" +
				"\t\tImplConvertImage.convert(input, output);\n" +
				"\n" +
				"\t\treturn output;\n" +
				"\t}\n\n");
	}

	public static void main( String args[] ) throws FileNotFoundException {
		GenerateConvertImage app = new GenerateConvertImage();

		app.generate();
	}
}
