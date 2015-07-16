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

import static eu.eubrazilcc.lvl.storage.base.ObjectState.DRAFT;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.addEditProv;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.storage.security.User;

/**
 * Behavior corresponding to the draft state.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DraftStateHandler<T extends LvlObject> extends ObjectStateHandler<T> {

	@Override
	public ListenableFuture<Void> save(final T obj, final @Nullable User user, final SaveOptions... options) {
		if (obj.getState() == null) obj.setState(DRAFT);
		if (user != null && obj.getProvenance() != null) addEditProv(obj.getProvenance(), user, obj.getLvlId());
		return MONGODB_CONN.client().saveActive(obj, DRAFT.name());
	}	

}