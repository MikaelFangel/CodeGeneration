import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;


    
public class main {
    public static void main(String[] args) throws IOException{

	// we expect exactly one argument: the name of the input file
	if (args.length!=1) {
	    System.err.println("\n");
	    System.err.println("Simple imperative language compiler\n");
	    System.err.println("===================================\n\n");
	    System.err.println("Please give as argument a filename\n");
	    System.exit(-1);
	}
	String filename=args[0];

	// open the input file
	CharStream input = CharStreams.fromFileName(filename);
	    //new ANTLRFileStream (filename); // depricated
	
	// create a lexer/scanner
	compilerLexer lex = new compilerLexer(input);
	
	// get the stream of tokens from the scanner
	CommonTokenStream tokens = new CommonTokenStream(lex);
	
	// create a parser
	compilerParser parser = new compilerParser(tokens);
	
	// and parse anything from the grammar for "start"
	ParseTree parseTree = parser.start();

	// Construct an interpreter and run it on the parse tree
	ASTmaker mk = new ASTmaker();
	AST ast=mk.visit(parseTree);
	Environment env=mk.env;
	((Command) ast).typecheck(env);
	
	String header=
	    "; ModuleID = 'generatedLLVMcode'\n\n"+
	    "@.str = private constant [4 x i8] c\"%d\\0A\\00\"\n\n"+
	    "@.str2 = private constant [4 x i8] c\"%f\\0A\\00\"\n\n"+
	    "define void @print(i64 %d) nounwind ssp {\n"+
	    "  %1 = alloca i64\n"+
	    "  store i64 %d, i64* %1\n"+
	    "  %2 = load i64, i64* %1\n"+
	    "  %cast210 = getelementptr inbounds [4 x i8], [4 x i8]* @.str, i64 0, i64 0\n"+
	    "  %3 = call i64 (i8*, ...) @printf(i8* %cast210, i64 %2)\n"+
	    "  ret void\n"+
	    "}\n\n"+
	    "define void @printd(double %d) nounwind ssp {\n"+
	    "  %1 = alloca double\n"+
	    "  store double %d, double* %1\n"+
	    "  %2 = load double, double* %1\n"+
	    "  %cast210 = getelementptr inbounds [4 x i8], [4 x i8]* @.str2, i64 0, i64 0\n"+
	    "  %3 = call i64 (i8*, ...) @printf(i8* %cast210, double %2)\n"+
	    "  ret void\n"+
	    "}\n\n"+
	    "declare i64 @printf(i8*, ...)\n"+
	    "declare i8* @malloc(i64) #1\n\n"+
	    "define i64 @main() nounwind ssp {\n\n";
	String trailer=
	    "ret i64 0\n"  // the main routine just returns 0
	    +"}\n";

	
	System.out.println(header+
			   ((Command) ast).compile(env)+
			   trailer);
    }
}



class ASTmaker extends AbstractParseTreeVisitor<AST> implements compilerVisitor<AST> {
    public static Environment env=new Environment();
    
    public AST visitStart(compilerParser.StartContext ctx){
	Command program=new NOP();
	for(compilerParser.DeclContext dec:ctx.decs)
	    program=new Sequence(program,(Command)visit(dec));
	for(compilerParser.CommandContext c:ctx.cs)
	    program=new Sequence(program,(Command)visit(c));
	return program;
    };

    public AST visitVarDecl(compilerParser.VarDeclContext ctx){
	Type t;
	String varname=ctx.x.getText();
	if (ctx.t.getText().equals("int"))
	    t=Type.INTTYPE;
	else 
	    t=Type.DOUBLETYPE;
	env.setVariable(varname,t);
	return new Alloc(varname);
    }
     public AST visitArrayDecl(compilerParser.ArrayDeclContext ctx){
	Type t;
	String varname=ctx.x.getText();
	if (ctx.t.getText().equals("int"))
	    t=Type.INTARRAYTYPE;
	else 
	    t=Type.DOUBLEARRAYTYPE;
	env.setVariable(varname,t);
	return new Alloc(varname);
    }
    
    public AST visitSingleCommand(compilerParser.SingleCommandContext ctx){
	return visit(ctx.c);
    }
    public AST visitMultipleCommands(compilerParser.MultipleCommandsContext ctx){
	Command program=new NOP();
	for(compilerParser.CommandContext c:ctx.cs)
	    program=new Sequence(program,(Command)visit(c));
	return program;
    }

    public AST visitAssignment(compilerParser.AssignmentContext ctx){
	return new Assignment(ctx.x.getText(),(Expr) visit(ctx.e));
    };
    
    public AST visitArrayAssignment(compilerParser.ArrayAssignmentContext ctx){
	String v=ctx.a.getText();
 	Expr i=(Expr)visit(ctx.i);
 	Expr e=(Expr)visit(ctx.e);
	return new ArrayAssignment(v,i,e);
    }
    
    public AST visitOutput(compilerParser.OutputContext ctx){
	Expr e=(Expr)visit(ctx.e);
	return new Output(e);
    }

    public AST visitIf(compilerParser.IfContext ctx){
	return new Conditional((Cond)visit(ctx.c),
			       (Command)visit(ctx.p1),
			       (Command)visit(ctx.p2));
    };

    public AST visitWhile(compilerParser.WhileContext ctx){
	return new While((Cond)visit(ctx.c),
			 (Command)visit(ctx.p));
    };

   public AST visitForLoop(compilerParser.ForLoopContext ctx){
	String v=ctx.x.getText();
	Expr e1=(Expr)visit(ctx.e1);
	Expr e2=(Expr)visit(ctx.e2);
	Command body=(Command)visit(ctx.p);
	return
        new Sequence(
          new Assignment(v,e1),
	  new While(
	    new Unequal(new Variable(v),e2),
            new Sequence(body,
	      new Assignment(v,
		new Add(
		  new Variable(v),
		  new IntConst(Integer.valueOf(1)))))));
    }

    

    public AST visitParenthesis(compilerParser.ParenthesisContext ctx){
	return visit(ctx.e);
    };
    
    public AST visitVariable(compilerParser.VariableContext ctx){
	return new Variable(ctx.getText());
    };

    public AST visitAddition(compilerParser.AdditionContext ctx){
	if (ctx.op.getText().equals("+"))
	    return new Add((Expr) visit(ctx.e1), (Expr)visit(ctx.e2));
	else return new Sub((Expr)visit(ctx.e1),(Expr)visit(ctx.e2));
    };

    public AST visitMultiplication(compilerParser.MultiplicationContext ctx){
	if (ctx.op.getText().equals("*"))
	    return new Mult((Expr) visit(ctx.e1), (Expr)visit(ctx.e2));
	else
	    return new Div((Expr) visit(ctx.e1), (Expr)visit(ctx.e2));
    };

    public AST visitIntConstant(compilerParser.IntConstantContext ctx){
	return new IntConst(Integer.parseInt(ctx.getText())); 
    };
    public AST visitDoubleConstant(compilerParser.DoubleConstantContext ctx){
	return new DoubleConst(Double.parseDouble(ctx.getText())); 
    };

    public AST visitArray(compilerParser.ArrayContext ctx){
	Expr e=(Expr)visit(ctx.e);
	return new Array(ctx.x.getText(),e);
    };

    public AST visitConjunction(compilerParser.ConjunctionContext ctx){
	return new And((Cond)visit(ctx.cond(0)) , (Cond)visit(ctx.cond(1)) );
    };

    public AST visitDisjunction(compilerParser.DisjunctionContext ctx){
	return new Or((Cond)visit(ctx.cond(0)) , (Cond)visit(ctx.cond(1)) );
    };

    public AST visitNegation(compilerParser.NegationContext ctx){
	return new Not((Cond)visit(ctx.cond()) );
    };

    public AST visitComparison(compilerParser.ComparisonContext ctx){
	Expr e1=(Expr)visit(ctx.e1);
	Expr e2=(Expr)visit(ctx.e2);
	if (ctx.op.getText().equals("=="))
	    return new Equals(e1,e2);
	else if (ctx.op.getText().equals("<"))
	    return new Smaller(e1,e2);
	else if (ctx.op.getText().equals(">"))
	    return new Greater(e1,e2);
	else if (ctx.op.getText().equals("<="))
	    return new SmallerEqual(e1,e2);
	else if (ctx.op.getText().equals(">="))
	    return new GreaterEqual(e1,e2);
	else //if (ctx.op.getText().equals("!="))
	    return new Unequal(e1,e2);
    };

    public AST visitParenthesisCondition(compilerParser.ParenthesisConditionContext ctx){
	return visit(ctx.c);
    };


}
