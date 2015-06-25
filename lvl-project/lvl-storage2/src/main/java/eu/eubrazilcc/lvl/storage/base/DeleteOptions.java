/*
 * Copyright 2014-2015 EUBrazilCC (EU‐Brazil Cloud Connect)
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by 
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *   http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 * 
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */

package eu.eubrazilcc.lvl.storage.base;

/**
 * Deleting options. By default, the LeishVL will not perform any action on the references registered for an object when the object is 
 * deleted from the database. Specify the option {@link DeleteOptions#ON_DELETE_CASCADE} to delete the references with the referrer object.
 * Similarly, the default action on object deletion is to only delete the active version of the object. Specify the option {@link DeleteOptions#DELETE_ALL}
 * to delete all versions of the object stored in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum DeleteOptions {

	ON_DELETE_CASCADE, // allows deletions of references registered within an object when the object is deleted from the database
	ON_DELETE_NO_ACTION, // (default) when the object is deleted its references are not modified
	DELETE_ACTIVE, // (default) delete the active version of the object
	DELETE_ALL // deletes all the versions of the object
	
}