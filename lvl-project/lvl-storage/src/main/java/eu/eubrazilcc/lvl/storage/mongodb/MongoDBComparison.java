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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;

/**
 * Transforms comparison query operators supported in mongoDB.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://docs.mongodb.org/manual/reference/operator/query-comparison/">Comparison Query Operators</a>
 */
public final class MongoDBComparison {

	private static final Pattern LOGICAL_PATTERN = compile("^(<|>|<=|>=|<>){0,1}(\\s){0,}(\\d+)$");

	public static final BasicDBObject mongoNumeriComparison(final String field, final String expression) {
		checkArgument(isNotBlank(field), "Uninitialized or invalid field");
		checkArgument(isNotBlank(expression), "Uninitialized or invalid expression");
		final Matcher matcher = LOGICAL_PATTERN.matcher(expression.trim());
		checkState(matcher.find() && matcher.groupCount() == 3, "Unsupported expression: " + expression);
		String operator = null;
		Integer quantity = null;
		for (int i = 1; i < 4; i++) {
			switch (i) {
			case 0: // skip first position where the input is stored
				break;
			case 1: // grab operator
				operator = parseLogicalOperator(matcher.group(i));
				break;
			case 2: // skip empty spaces				
				break;
			case 3: // grab quantity
				quantity = Integer.valueOf(matcher.group(i));
				break;
			default:
				throw new IllegalStateException("Unsupported expression: " + expression);
			}
		}
		return isBlank(operator) ? new BasicDBObject(field, quantity) : new BasicDBObject(field, new BasicDBObject(operator, quantity));
	}

	private static String parseLogicalOperator(final String op) {
		String queryOp = "";
		if (isBlank(op)) {
			queryOp = "";
		} else if ("<".equals(op)) {
			queryOp = "$lt";
		} else if ("<=".equals(op)) {
			queryOp = "$lte";
		} else if (">".equals(op)) {
			queryOp = "$gt";
		} else if (">=".equals(op)) {
			queryOp = "$gte";
		} else if ("<>".equals(op)) {
			queryOp = "$ne";
		} else {
			throw new IllegalStateException("Unsupported operator: " + op);
		}
		return queryOp;
	}

}