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

parser grammar LvLQLParser;
options { tokenVocab=LvLQLLexer; }

query : OPEN_PAREN query CLOSE_PAREN  # EmbeddedQuery
      | atomic_query                  # SimpleQuery
      | NOT query                     # NegationQuery
      | query (AND | OR) query        # RelationQuery
      ;

atomic_query : expression comparison_operator expression  # ComparisonSimpleQuery
             | term                                       # TermSimpleQuery
             ;

comparison_operator : EQUALS                 # EqualityOp
                    | NEQUALS                # InequalityOp
                    | (GT | LT)              # ComparisonOp
                    | (GTEQUALS | LTEQUALS)  # ComparisonEqualityOp
                    ;

expression : OPEN_PAREN expression CLOSE_PAREN  # EmbeddedEx
           | atomic_expression                  # SimpleEx
           ;

// the second parameter in sequence similarity search is reserved for future use (e.g. program name, local/global strategy)
atomic_expression : SEQUENCE_SIMILARITY_SEARCH OPEN_PAREN term (COMMA STRING)? CLOSE_PAREN  # SSSSimpleEx
                  | NEAR_SEARCH OPEN_PAREN coordinates CLOSE_PAREN                          # NearSearchEx
                  | term                                                                    # TermSimpleEx
                  | NUMERIC                                                                 # NumericSimpleEx
                  ;

coordinates : longitude COMMA latitude ;

latitude : NUMERIC ;

longitude : NUMERIC ;

term : field   # FieldTerm
     | STRING  # StringTerm
     ;

field : STRING FIELD_SEP STRING ;