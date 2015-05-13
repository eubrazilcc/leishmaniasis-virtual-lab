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

package eu.eubrazilcc.lvl.storage.mongodb;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mongodb.BasicDBObject;

/**
 * Utility class to facilitate common tasks needed to transform between Java objects and mongoDB objects. The following limitations are currently imposed
 * by mongoDB to the use of projections:
 * <ul>
 * <li>Inclusion and exclusion semantics cannot be combined in a single projection with the exception of the <tt>_id</tt> field.</li>
 * </ul>
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://docs.mongodb.org/manual/reference/method/db.collection.find/">mongoDB: db.collection.find()</a>
 */
public final class MongoDBHelper {

	public static BasicDBObject toProjection(final @Nullable ImmutableMap<String, Boolean> projection) {
		BasicDBObject obj = null;
		if (projection != null && !projection.isEmpty()) {
			obj = new BasicDBObject();
			final ImmutableSet<Entry<String, Boolean>> entrySet = projection.entrySet();
			final boolean action = entrySet.iterator().next().getValue();
			for (final Entry<String, Boolean> entry : entrySet) {
				String field = null;
				checkState(action == entry.getValue(), "A projection cannot contain both include and exclude specifications");				
				checkState(isNotBlank(field = trimToNull(entry.getKey())), "Uninitialized field");
				obj.append(field, action ? 1 : 0);
			}
		}
		return obj;
	}

}