antlr4 = java org.antlr.v4.Tool
SRCFILES = main.java AST.java
GENERATED = compilerListener.java compilerBaseListener.java compilerParser.java compilerBaseVisitor.java compilerVisitor.java compilerLexer.java 

all:
	make main.class

main.class:	$(SRCFILES) $(GENERATED) compiler.g4
	javac  $(SRCFILES) $(GENERATED)

compilerListener.java:	compiler.g4
	$(antlr4) -visitor compiler.g4

test:	main.class
	java main compiler_input.txt > compiler_output.ll
	clang -O3 compiler_output.ll
	./a.out

clean:
	rm -rf *.class *.tokens *.interp compiler*.java
