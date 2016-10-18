/**************************************************************************
Copyright (c) 2011:
Istituto Nazionale di Fisica Nucleare (INFN), Italy
Consorzio COMETA (COMETA), Italy

See http://www.infn.it and and http://www.consorzio-cometa.it for details on
the
copyright holders.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Author: Diego Scardaci (INFN)
 ****************************************************************************/

package it.infn.ct.GridEngine.Job;

/**
 * This class creates a relation between the grid job identifier and the
 * database identifier into the ActiveGridinterazions table for this job.
 * 
 * @author mario
 * 
 */
public class JobId {

	private String GridJobId;
	private int DbId;

	/**
	 * Constructs a {@link JobId} object with the specified identifiers.
	 * 
	 * @param gridJobId
	 *            the grid job identifier
	 * @param dbId
	 *            database identifier into the ActiveGridinterazions table.
	 */
	public JobId(String gridJobId, int dbId) {
		GridJobId = gridJobId;
		DbId = dbId;
	}

	/**
	 * Retruns the grid job identifier.
	 * 
	 * @return grid job identifier.
	 */
	public String getGridJobId() {
		return GridJobId;
	}

	/**
	 * Returns the database identifier into the ActiveGridinterazions table.
	 * 
	 * @return database identifier into the ActiveGridinterazions table.
	 */
	public int getDbId() {
		return DbId;
	}
}
