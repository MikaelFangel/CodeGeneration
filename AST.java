import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;

class faux{ // collection of non-OO fauxiliary functions (currently just error)
    public static void error(String msg){
        System.err.println("Compiler error: "+msg);
        System.exit(-1);
    }
}

enum Type{
    INTTYPE, DOUBLETYPE, INTARRAYTYPE, DOUBLEARRAYTYPE
}

class Environment {
    private HashMap<String,Type> variableValues = new HashMap<String,Type>();
    int varcounter=0;
    int labelcounter=0;
    public String newvar(){ return "%"+ ++varcounter; }
    public String newlabel(){ return "Label"+ ++labelcounter; }
    public String activevar(){ return "%"+varcounter; }

    public Environment() { }	
    public void setVariable(String name, Type t) {
        variableValues.put(name, t);
    }
    public boolean hasVariable(String name){
        return variableValues.get(name)!=null;
    }
    public Type getVariable(String name){
        Type value = variableValues.get(name); 
        if (value == null) faux.error("Variable not defined: "+name); 
        return value;
    }
}

abstract class AST{}

abstract class Command extends AST{
    abstract public void typecheck(Environment env);
    abstract public String compile(Environment env);
}

class NOP extends Command{
    public void typecheck(Environment env){};
    public String compile(Environment env){ return ""; };
}

class Sequence extends Command{
    Command c1,c2;
    Sequence(Command c1, Command c2){this.c1=c1; this.c2=c2;}
    public void typecheck(Environment env){
        c1.typecheck(env);
        c2.typecheck(env);
    };
    public String compile(Environment env){
        return c1.compile(env)+c2.compile(env);
    }
}

class Assignment extends Command{
    public String x;
    public Expr e;
    Assignment(String x, Expr e){ this.x=x; this.e=e; }
    public void typecheck(Environment env){
        if (env.getVariable(x)!=Type.INTTYPE||
                e.typecheck(env)!=Type.INTTYPE)
            faux.error("Implement me for this type.\n");
    }
    public String compile(Environment env){
        String s=e.compile(env);
        String v=env.activevar();
        return s+"store i64 "+v+", i64* %"+x+"\n";
    }
}

class Alloc extends Command{
    String v;
    Alloc(String v){this.v=v;}
    public void typecheck(Environment env){
        if (env.getVariable(v)!=Type.INTTYPE)
            faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        return
            "%"+v+" = alloca i64\n"+
            "store i64 0, i64* %"+v+"\n";
    }
}

class ArrayAssignment extends Command{
    String v;
    Expr i;
    Expr e;
    ArrayAssignment(String v, Expr i, Expr e){
        this.v=v; this.i=i; this.e=e;
    }
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}

class Output extends Command{
    Expr e;
    Output(Expr e){
        this.e=e;
    }
    public void typecheck(Environment env){

    }
    public String compile(Environment env){
        return
            e.compile(env)+
            "call void @print(i64 "+env.activevar()+")\n";
    }
}

class Conditional extends Command{
    public Cond c;
    public Command thenp;
    public Command elsep;
    Conditional(Cond c, Command thenp, Command elsep){ this.c=c; this.thenp=thenp; this.elsep=elsep; }
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}

class While extends Command{
    public Cond c;
    public Command body;
    While(Cond c, Command body){this.c=c; this.body=body;}
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}

abstract class Expr extends AST{
    abstract public Type typecheck(Environment env);
    abstract public String compile(Environment env);
} 
class Add extends Expr{
    public Expr e1,e2;
    Add(Expr e1, Expr e2){ this.e1=e1; this.e2=e2; }
    public Type typecheck(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}
class Mult extends Expr{
    public Expr e1,e2;
    Mult(Expr e1, Expr e2){ this.e1=e1; this.e2=e2; }
    public Type typecheck(Environment env){
        if (e1.typecheck(env)!=Type.INTTYPE||
                e2.typecheck(env)!=Type.INTTYPE)
            faux.error("Implement me.\n");
        return Type.INTTYPE;
    }
    public String compile(Environment env){
        String s1=e1.compile(env);
        String v1=env.activevar();
        String s2=e2.compile(env);
        String v2=env.activevar();
        return
            s1+
            s2+
            env.newvar()+"= mul nsw i64 "+v1+", "+v2+"\n";
    }
}
class Sub extends Expr{
    public Expr e1,e2;
    Sub(Expr e1, Expr e2){ this.e1=e1; this.e2=e2; }
    public Type typecheck(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}
class Div extends Expr{
    public Expr e1,e2;
    Div(Expr e1, Expr e2){ this.e1=e1; this.e2=e2; }
    public Type typecheck(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}

class IntConst extends Expr{
    public Integer v;
    IntConst(Integer v){this.v=v;}
    public Type typecheck(Environment env){
        return Type.INTTYPE;
    }
    public String compile(Environment env){
        return env.newvar()+"= add i64 0, "+v+"\n";
    }
}
class DoubleConst extends Expr{
    public Double v;
    DoubleConst(Double v){this.v=v;}
    public Type typecheck(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}

class Variable extends Expr{
    public String varname;
    Variable(String varname){this.varname=varname;}
    public Type typecheck(Environment env){
        if (env.getVariable(varname)!=Type.INTTYPE)
            faux.error("Only integer supported\n");
        return Type.INTTYPE;
    }
    public String compile(Environment env){
        return env.newvar()+"= load i64, i64* %"+varname+"\n";
    }
}

class Array extends Expr{
    String varname;
    Expr e;
    Array(String varname, Expr e){this.varname=varname; this.e=e; }
    public Type typecheck(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}


abstract class Cond extends AST{
    abstract public String compile(Environment env);
    abstract public void typecheck(Environment env);
}

class Equals extends Cond{
    public Expr e1,e2;
    Equals(Expr e1, Expr e2){ this.e1=e1; this.e2=e2; }
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}
class Smaller extends Cond{
    public Expr e1,e2;
    Smaller(Expr e1, Expr e2){ this.e1=e1; this.e2=e2; }
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}
class Greater extends Cond{
    public Expr e1,e2;
    Greater(Expr e1, Expr e2){ this.e1=e1; this.e2=e2; }
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}

class SmallerEqual extends Cond{
    public Expr e1,e2;
    SmallerEqual(Expr e1, Expr e2){ this.e1=e1; this.e2=e2; }
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}
class GreaterEqual extends Cond{
    public Expr e1,e2;
    GreaterEqual(Expr e1, Expr e2){ this.e1=e1; this.e2=e2; }
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}
class Unequal extends Cond{
    public Expr e1,e2;
    Unequal(Expr e1, Expr e2){ this.e1=e1; this.e2=e2; }
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}
class Not extends Cond{
    public Cond c;
    Not(Cond c){ this.c=c;}
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}
class And extends Cond{
    public Cond c1,c2;
    And(Cond c1, Cond c2){ this.c1=c1; this.c2=c2; }
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}
class Or extends Cond{
    public Cond c1,c2;
    Or(Cond c1, Cond c2){ this.c1=c1; this.c2=c2; }
    public void typecheck(Environment env){
        faux.error("Implement me.\n");
    }
    public String compile(Environment env){
        faux.error("Implement me.\n");
        return null;
    }
}
