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

package boofcv.geo.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which puts together all the simulation components
 *
 * @author Peter Abeles
 */
public class SimulationEngine {

	List<CameraModel> cameras = new ArrayList<CameraModel>();
	List<CameraControl> cameraControl = new ArrayList<CameraControl>();

	EnvironmentModel environment;

	public SimulationEngine(EnvironmentModel environment) {
		this.environment = environment;
	}

	public void addCamera(CameraModel model , CameraControl control ) {
		cameras.add(model);
		cameraControl.add(control);
	}

	/**
	 * Updates the world.
	 */
	public void step() {
		// update the camera positions
		for( int i = 0; i < cameras.size(); i++ ) {
			cameraControl.get(i).update();
		}
	}

	/**
	 * Removes unused objects
	 */
	public void maintenance() {
		environment.maintenance();
	}
}
