org.terasology.logic.grammar PAGDefinition;

@header{
  package org.terasology.logic.org.terasology.logic.grammar;
}

@lexer::header {
  package org.terasology.logic.org.terasology.logic.grammar;
}

// The org.terasology.logic.grammar file consists of a header and a rules section
pag	:	header '#rules'	rules
	;

// The header contains only assignments
header	:	(assignment)+
	;

// assignments are for defining some org.terasology.logic.grammar system attributes
// and user specific fields
assignment
	:	ID '=' ID ';'
	;

// The rules sections consists of one or more producton rules
rules	:	(rule)+
	;

/* Each rule is of the form 
 * 		predecessor : condition ::- successor : probability;
 * <conditon> and <probability> are optional for each rule.
 */
rule	:	ID (':' expression)? '::-' successor (':' floating_point)? ('|' successor (':' floating_point)? )*  ';'
	;

// Expressions are relations combined via logical AND or logical OR.
expression
	:	relation (( AND | OR ) relation )*
	;

// The relations for bool expressions. Can either be a single add expression or a relation between two add expressions.
relation
	: add ( ( IS | ISNOT | '>' | '<' | '>=' | '<=' ) add )?
	;

// Addition expression of multiply expressions. Can be a single multiply expression, too.
add
	:	mult (( '+' | '-' ) mult)*
	;

/* A multiply expression is either single unary expression or a chain of unary expressions combined with one of the
 * specified symbols. '*' will stand for multiply, '/' for divide and '%' for the modulo operator.
 */
mult
	:	unary (( '*' | '/' | '%' ) unary)*
	;

/* Enables the use of the unary + and - operator, which means you can use '-3' in a expression.
 * Not sure if it is actually needed.
 */
unary
	:	( '+' | '-' )* negation
	;

// The atom can either be negated (first alternative with NOT) or not (no NOT) - obviously!
negation
	:	NOT? atom;

// An atom in an expression has to be either an identifier (ID), and integer value (INTEGER), an occlusion query or
// another expression in parentheses.
atom
	: ID
	| occlusion_query
	|'(' expression ')'
	| INTEGER
	;

// An occlusion query starts with the keyword 'Occ' and continues with an identifier as argument and an
// <occlusion_result> to test against.
occlusion_query
	:	'Occ' '(' ID ')' ( IS | ISNOT ) occlusion_result
	;

// These are the results that can occur on a occlusion query.
occlusion_result
	:	'none'
	|	'part'
	|   'full'
	;

/* A successor is anything can occur on the right side of a production rule. Namely that are the defined methods such
 * as Set, Subdivide, Repeat and the the Component Split. Furthermore, a successor can be another non terminal shapeSymbol.
 * Therefore it can also be an identifier.
 */
successor
	:	'Set' '(' asset_uri ')'
	|	'Subdiv' '(' direction ',' split_sizes ')' '{' shapes '}'
	|	'Repeat' '(' direction ',' INTEGER ')' '{' ID '}'
	|	composition_split+
	|   ID
	;

// The component split rule takes a component type as an argument for the split and an <successor> as the 'body' argument
composition_split
	:	'Comp' '(' comp_type ')' '{' successor '}'
	;

/* The component type defines which part of the current scope will form the scope for the following rules
 * Most of the types are self-explanatory such as top-/downside. 'faces' splits the current scope into all faces,
 * which includes all sidefaces and top- and downside.
 * 'edges' splits into all edges/corners of the scope, leaving the vertices (corner blocks) out.
 * 'vertices' splits into only these corner blocks.
 * 'vertEdges' and 'horizEdges' are kind of self-explanatory, I think).
 */
 comp_type
	:	'upside'
	|	'downside'
	|	'sidefaces'
	|	'faces'
	|	'edges'
	| 'vertices'
	|	'vertEdges'
	|	'horizEdges'
	| 'inner'
	;

// Split seizes are any number of comma seperated sizes
split_sizes
	:	size (',' size)*
	;

// Seizes are either absolute (integer value) or relative ('r')
size
    :	INTEGER
    |	'r'
    ;

// Shapes can be any number of shapes in a comma separated list
shapes
	: successor (',' successor)*
	;

// possible directions
// vert is equal to the horizontal axis
direction
	:	'vert'
	|	'X'
	|	'Y'
	|	'Z'
	|	'XY'
	|	'YZ'
	|	'XZ'
	;

// An <AssetURI> is a ':' seperated list of strings, framed by double quotes
// e.g.		"engine:CobbleStone", "mod:myMod:Block"
asset_uri
	:	'"' ID (':' ID)* '"'
	;

floating_point
    : INTEGER ('.' INTEGER)?
    | '.' INTEGER
    ;


// Integer with no leading zero
INTEGER
	:	'0'
	|	'1'..'9' ('0'..'9')*
	;

// logical AND shapeSymbol
AND
	:	'&&'
	;

// logical OR shapeSymbol
OR:	'||';


// Token for the realitonal 'is' shapeSymbol in <condition>
IS:	'==';

// Token for the relational 'is-not' shapeSymbol in <condition>..
ISNOT
	:	'!='
	;

// Token for negation.
NOT
	:	'!'
	;

TRUE
	: 'true'
	;

FALSE
	:	 'false'
	;

// Identifiers have to start with a letter and can continue with letters, numbers or '_'
ID  :	('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

// Java-like comments are allowed (and will be ignored when parsing)
COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

// Whitespace characters such as tabs and linefeeds
WS  :   ( ' '
    | '\t'
    | '\r'
    | '\n'
        ) {$channel=HIDDEN;}
    ;
