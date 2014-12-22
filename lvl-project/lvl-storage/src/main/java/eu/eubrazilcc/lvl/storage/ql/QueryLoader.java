/*
 * Copyright 2014 EUBrazilCC (EU‐Brazil Cloud Connect)
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

package eu.eubrazilcc.lvl.storage.ql;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import eu.eubrazilcc.lvl.storage.ql.antlr4.LvLQLLexer;
import eu.eubrazilcc.lvl.storage.ql.antlr4.LvLQLParser;
import eu.eubrazilcc.lvl.storage.ql.antlr4.LvLQLParserBaseListener;

/**
 * Provides the implementation of the LvLQL language for searching the LVL data collections. 
 * @author Erik Torres <ertorser@upv.es>
 */
public final class QueryLoader extends LvLQLParserBaseListener {

	public static void load(final String query) throws IOException {
		try (final InputStream is = new ByteArrayInputStream(query.getBytes())) {
			final ANTLRInputStream input = new ANTLRInputStream(is);
			final LvLQLLexer lexer = new LvLQLLexer(input);
			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			final LvLQLParser parser = new LvLQLParser(tokens);
			final ParseTree tree = parser.query();

			final ParseTreeWalker walker = new ParseTreeWalker();
			final QueryLoader loader = new QueryLoader();
			walker.walk(loader, tree);

			// TODO
		}
	}

}