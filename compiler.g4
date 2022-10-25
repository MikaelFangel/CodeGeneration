grammar compiler;

/* A small imperative language */

start   : decs+=decl* cs+=command* EOF ;

decl    : t=TYPE x=ID ';'                    #VarDecl
	| 'array' '[' t=TYPE ']' x=ID '[' INT ']' ';' # ArrayDecl
	;

program : c=command                          # SingleCommand
	| '{' cs+=command* '}'               # MultipleCommands
	;
	
command : x=ID '=' e=expr ';'                # Assignment
	| a=ID '[' i=expr ']' '=' e=expr ';' # ArrayAssignment
	| 'output' e=expr ';'                # Output
	| 'if' '(' c=cond ')' p1=program 'else' p2=program # If
	| 'while' '(' c=cond ')' p=program      # While
	| 'for' '(' x=ID '=' e1=expr '..' e2=expr ')' p=program # ForLoop
	;

expr	: e1=expr op=('*'|'/') e2=expr       # Multiplication
	| e1=expr op=('+'|'-') e2=expr       # Addition
	| c=INT                		     # IntConstant
	| c=DOUBLE             		     # DoubleConstant
	| x=ID            		     # Variable
	| '(' e=expr ')'  		     # Parenthesis
	| x=ID '[' e=expr ']'		     # Array
	;

cond    : e1=expr op=('=='|'<'|'>'|'<='|'>='|'!=') e2=expr # Comparison
	| '!' c=cond 			    # Negation
	| c1=cond '&&' c2=cond 		    # Conjunction
	| c1=cond '||' c2=cond 		    # Disjunction
	| '(' c=cond ')' 		    # ParenthesisCondition
	;

TYPE : 'int' | 'double' ;

INT        : [0-9]+ ;
DOUBLE     : [0-9]+ '.' [0-9]+ ; 
ID         : ('A'..'Z'|'a'..'z'|'_')+ ;
WHITESPACE : [ \n\t\r]+ -> skip;
COMMENT    : '//'~[\n]+'\n' -> skip;
COMMENT2   : '/*' (~[*] | '*'~[/]  )*   '*/'  -> skip;
