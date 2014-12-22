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

lexer grammar LvLQLLexer;

// Function Names
SEQUENCE_SIMILARITY_SEARCH : S I M S E Q ;

// Control Chars
FIELD_SEP : ':' ;

COMMA : ',' ;

OPEN_BRACK  : '[' ;
CLOSE_BRACK : ']' ;

OPEN_PAREN  : '(' ;
CLOSE_PAREN : ')' ;

// Comparison Operators
EQUALS   : '=' ;
NEQUALS  : '!=' ;
GTEQUALS : '>=' ;
LTEQUALS : '<=' ;
GT       : '>' ;
LT       : '<' ;

// Logical Operators
AND : A N D ;
NOT : N O T ;
OR  : O R ;

// Literals
NUMERIC : FLOAT
        | INT
        ;

FLOAT : DIGIT+ '.' DIGIT* EXP?
      | DIGIT+ EXP?
      | '.' DIGIT+ EXP?
      ;

INT : DIGIT+ ;

DOUBLE_QUOT : '"' ;

OPEN_STRING : DOUBLE_QUOT -> pushMode(STRING_MODE) ;

STRING : WORD
       | DOUBLE_QUOT S_STRING DOUBLE_QUOT
       ;

fragment
    WORD : (LETTER | DIGIT | '_' | '-')+ ;

fragment
    EXP : E ('+' | '-')? DIGIT+ ;

fragment
    DIGIT : [0-9] ;

fragment
    LETTER : [a-zA-Z] ;

fragment
    SPACE : (' ' | '\t' | '\u000B') ;

SPACES : [ \u000B\t\r\n]+ -> skip ;

fragment A : [aA] ;
fragment N : [nN] ;
fragment D : [dD] ;
fragment O : [oO] ;
fragment R : [rR] ;
fragment T : [tT] ;
fragment E : [eE] ;

fragment S : [sS] ;
fragment I : [iI] ;
fragment M : [mM] ;
fragment Q : [qQ] ;

mode STRING_MODE;

CLOSE_STRING : DOUBLE_QUOT -> popMode ;

S_STRING : .*? ~["] ;