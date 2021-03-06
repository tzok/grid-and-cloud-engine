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

Author: XXXXXXXXXXXXXXXXXX
****************************************************************************/

package it.infn.ct.GridEngine.Data;

public class DataError extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2342342321L;

	/**
     * Creates a new instance of <code>DataError</code> without detail message.
     */
    public DataError() {
    }

    /**
     * Constructs an instance of <code>DataError</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DataError(String msg) {
        super(msg);
    }
}
