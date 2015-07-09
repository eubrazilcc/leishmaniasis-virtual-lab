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

package eu.eubrazilcc.lvl.storage.mongodb.jackson;

import java.util.Arrays;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

/**
 * Custom escape definition set.
 * @author Erik Torres <ertorser@upv.es>
 */
public class MongoJsonCharacterEscapes extends CharacterEscapes {
	
	private static final long serialVersionUID = 5638980228171144113L;
	
	private final int[] _asciiEscapes;
	
	public MongoJsonCharacterEscapes() {
		_asciiEscapes = standardAsciiEscapesForJSON();
        _asciiEscapes['$'] = CharacterEscapes.ESCAPE_CUSTOM;
	}
	
	@Override
    public int[] getEscapeCodesForAscii() {
        return _asciiEscapes;
    }

    @Override
    public SerializableString getEscapeSequence(final int ch) {
    	
    	// TODO
        System.err.println("\n\n >> HERE_ESCAPES2: " + Arrays.toString(_asciiEscapes) + "\n");
        // TODO
    	
    	if (ch == '$') {
    		
    		// TODO
            System.err.println("\n\n >> HERE_ESCAPES: " + Arrays.toString(_asciiEscapes) + "\n");
            // TODO
    		
            return new SerializedString("\uff04");
        }
        return null;
    }
	
}