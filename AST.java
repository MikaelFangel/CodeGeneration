import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

class faux { // collection of non-OO fauxiliary functions (currently just error)
    public static void error(String msg) {
        System.err.println("Compiler error: " + msg);
        System.exit(-1);
    }
}

enum Type { INTTYPE, DOUBLETYPE, INTARRAYTYPE, DOUBLEARRAYTYPE }

class Environment {
    private HashMap<String, Type> variableValues = new HashMap<String, Type>();
    int varcounter = 0;
    int labelcounter = 0;
    public String newvar() { return "%" + ++varcounter; }
    public String newlabel() { return "Label" + ++labelcounter; }
    public String activevar() { return "%" + varcounter; }

    public Environment() {}
    public void setVariable(String name, Type t) { variableValues.put(name, t); }
    public boolean hasVariable(String name) {
        return variableValues.get(name) != null;
    }
    public Type getVariable(String name) {
        Type value = variableValues.get(name);
        if (value == null)
            faux.error("Variable not defined: " + name);
        return value;
    }
}

abstract class AST {}

abstract class Command extends AST {
    abstract public void typecheck(Environment env);
    abstract public String compile(Environment env);
}

class NOP extends Command {
    public void typecheck(Environment env){};
    public String compile(Environment env) { return ""; };
}

class Sequence extends Command {
    Command c1, c2;
    Sequence(Command c1, Command c2) {
        this.c1 = c1;
        this.c2 = c2;
    }
    public void typecheck(Environment env) {
        c1.typecheck(env);
        c2.typecheck(env);
    };
    public String compile(Environment env) {
        return c1.compile(env) + c2.compile(env);
    }
}

class Assignment extends Command {
    public String x;
    public Expr e;
    private Type type;
    Assignment(String x, Expr e) {
        this.x = x;
        this.e = e;
    }
    public void typecheck(Environment env) {
        Type xVar = env.getVariable(x);
        Type eType = e.typecheck(env);

        if ((xVar != Type.INTTYPE && xVar != Type.DOUBLETYPE) || (eType != Type.INTTYPE && eType != Type.DOUBLETYPE))
            faux.error("Assignment: must for either integer or double \n");

        if (xVar != eType)
            faux.error("Assignment: must be of the same type.\n");

        type = xVar;
    }
    public String compile(Environment env) {
        String s = e.compile(env);
        String active = env.activevar();

        return switch(type) {
            case INTTYPE -> s + "store i64 " + active + ", i64* %" + x + "\n";
            case DOUBLETYPE -> s + "store f64 " + active + ", f64* %" + x + "\n";
                default -> null;
        };
    }
}

class Alloc extends Command {
    String v;
    Integer size;
    Type type;
    Alloc(String v) {
        this.v = v;
        this.size = 0;
    }
    Alloc(String v, Integer size) {
        this.v = v;
        this.size = size;
    }
    public void typecheck(Environment env) {
        Type vType = env.getVariable(v);
        if (vType != Type.INTTYPE && vType != Type.INTARRAYTYPE &&
                vType != Type.DOUBLETYPE && vType != Type.DOUBLEARRAYTYPE)
            faux.error("Currenty implemented only for INT and DOUBLE.\n");

        type = vType;
    }
    public String compile(Environment env) {
        return switch(type) {
            case INTTYPE -> "%" + v + " = alloca i64\n" + "store i64 0, i64* %" + v + "\n";
                case INTARRAYTYPE -> compileArrString(env);
            case DOUBLETYPE -> "%" + v + " = alloca f64\n" + "store f64 0, f64* %" + v + "\n";
                case DOUBLEARRAYTYPE -> compileArrString(env);
                default -> null;
        };
    }

    private String compileArrString(Environment env) {
        String tmp = env.newvar();
        return switch(type) {
            case INTARRAYTYPE -> tmp + " = call i8* @malloc(i64 " + 8 * size + ")\n" + "%" + v + " = bitcast i8* " + tmp + " to i64*\n";
            case DOUBLEARRAYTYPE -> tmp + " = call f8* @malloc(f64 " + 8 * size + ")\n" + "%" + v + " = bitcast f8* " + tmp + " to f64*\n";
                default -> null;
        };
    }
}

class ArrayAssignment extends Command {
    String v;
    Expr i;
    Expr e;
    ArrayAssignment(String v, Expr i, Expr e) {
        this.v = v;
        this.i = i;
        this.e = e;
    }
    public void typecheck(Environment env) {
        if (env.getVariable(v) != Type.INTARRAYTYPE)
            faux.error("This should be an integer array");
        if (i.typecheck(env) != Type.INTTYPE)
            faux.error("The index must be an int");
        if (e.typecheck(env) != Type.INTTYPE)
            faux.error("The expression should be integer.");
    }
    public String compile(Environment env) {
        String ix = i.compile(env);
        String ixv = env.activevar();
        String ex = e.compile(env);
        String exv = env.activevar();

        return ix + ex + env.newvar() + " = getelementptr inbounds i64, i64* %" +
            v + ", i64 " + ixv + "\n"
            + "store i64 " + exv + ", i64* " + env.activevar() + "\n";
    }
}

class Output extends Command {
    Expr e;
    Type eType;
    Output(Expr e) { this.e = e; }
    public void typecheck(Environment env) {
        eType = e.typecheck(env);
        if (eType != Type.INTTYPE && eType != Type.DOUBLETYPE)
            faux.error("Must be int or double for now.\n");
    }
    public String compile(Environment env) {
        String s = e.compile(env);
        return switch(eType) {
            case INTTYPE -> s + "call void @print(i64 " + env.activevar() + ")\n";
            case DOUBLETYPE -> s + "call void @print(f64 " + env.activevar() + ")\n";
                default -> null;
        };
    }
}

class Conditional extends Command {
    public Cond c;
    public Command thenp;
    public Command elsep;
    Conditional(Cond c, Command thenp, Command elsep) {
        this.c = c;
        this.thenp = thenp;
        this.elsep = elsep;
    }
    public void typecheck(Environment env) {
        faux.error("If-then-else: Implement me.\n");
    }
    public String compile(Environment env) {
        faux.error("Implement me.\n");
        return null;
    }
}

class While extends Command {
    public Cond c;
    public Command body;
    While(Cond c, Command body) {
        this.c = c;
        this.body = body;
    }
    public void typecheck(Environment env) {
        c.typecheck(env);
        body.typecheck(env);
    }
    public String compile(Environment env) {
        String l1 = env.newlabel();
        String l2 = env.newlabel();
        String l3 = env.newlabel();
        String condition_code = c.compile(env);
        String cv = env.activevar();

        return "br label %" + l1 + "\n\n" + l1 + ":\n" + condition_code + "br i1 " +
            cv + ", label %" + l2 + ", label %" + l3 + "\n\n" + l2 + ":\n" +
            body.compile(env) + "br label %" + l1 + "\n\n" + l3 + ":\n";
    }
}

abstract class Expr extends AST {
    abstract public Type typecheck(Environment env);
    abstract public String compile(Environment env);
}
class Add extends Expr {
    public Expr e1, e2;
    private Type type;
    Add(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Type typecheck(Environment env) {
        Type e1Type = e1.typecheck(env);
        Type e2Type = e2.typecheck(env);

        if ((e1Type != Type.INTTYPE && e1Type != Type.DOUBLETYPE) || 
                (e2Type != Type.DOUBLETYPE && e2Type != Type.INTTYPE))
            faux.error("Implement me. ADDITION\n");

        type = e1Type;
        // TODO Implement in a more generic way.
        return e1Type;
    }
    public String compile(Environment env) {
        String s1 = e1.compile(env);
        String v1 = env.activevar();
        String s2 = e2.compile(env);
        String v2 = env.activevar();
        return switch(type) {
            case INTTYPE -> s1 + s2 + env.newvar() + "= add nsw i64 " + v1 + ", " + v2 + "\n";
            case DOUBLETYPE -> s1 + s2 + env.newvar() + "= add nsw f64 " + v1 + ", " + v2 + "\n";
                default -> null;
        };
    }
}
class Mult extends Expr {
    public Expr e1, e2;
    private Type type;
    Mult(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Type typecheck(Environment env) {
        Type e1Type = e1.typecheck(env);
        Type e2Type = e2.typecheck(env);

        if ((e1Type != Type.INTTYPE && e1Type != Type.DOUBLETYPE) || 
                (e2Type != Type.DOUBLETYPE && e2Type != Type.INTTYPE))
            faux.error("Implement me. MULTIPLICATION\n");

        type = e1Type;
        // TODO Implement in a more generic way.
        return e1Type;
    }
    public String compile(Environment env) {
        String s1 = e1.compile(env);
        String v1 = env.activevar();
        String s2 = e2.compile(env);
        String v2 = env.activevar();
        return switch(type) {
            case INTTYPE -> s1 + s2 + env.newvar() + "= mul nsw i64 " + v1 + ", " + v2 + "\n";
            case DOUBLETYPE -> s1 + s2 + env.newvar() + "= mul nsw f64 " + v1 + ", " + v2 + "\n";
                default -> null;
        };
    }
}
class Sub extends Expr {
    public Expr e1, e2;
    private Type type;
    Sub(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Type typecheck(Environment env) {
        Type e1Type = e1.typecheck(env);
        Type e2Type = e2.typecheck(env);

        if ((e1Type != Type.INTTYPE && e1Type != Type.DOUBLETYPE) || 
                (e2Type != Type.DOUBLETYPE && e2Type != Type.INTTYPE))
            faux.error("Implement me. SUBSTITUTION\n");

        type = e1Type;
        // TODO Implement in a more generic way.
        return e1Type;
    }
    public String compile(Environment env) {
        String s1 = e1.compile(env);
        String v1 = env.activevar();
        String s2 = e2.compile(env);
        String v2 = env.activevar();
        return switch(type) {
            case INTTYPE -> s1 + s2 + env.newvar() + "= sub nsw i64 " + v1 + ", " + v2 + "\n";
            case DOUBLETYPE -> s1 + s2 + env.newvar() + "= sub nsw f64 " + v1 + ", " + v2 + "\n";
                default -> null;
        };
    }
}
class Div extends Expr {
    public Expr e1, e2;
    Div(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Type typecheck(Environment env) {
        faux.error("Div: Implement me.\n");
        return null;
    }
    public String compile(Environment env) {
        faux.error("Implement me.\n");
        return null;
    }
}

class IntConst extends Expr {
    public Integer v;
    IntConst(Integer v) { this.v = v; }
    public Type typecheck(Environment env) { return Type.INTTYPE; }
    public String compile(Environment env) {
        return env.newvar() + "= add i64 0, " + v + "\n";
    }
}
class DoubleConst extends Expr {
    public Double v;
    DoubleConst(Double v) { this.v = v; }
    public Type typecheck(Environment env) {
        return Type.DOUBLETYPE;
    }
    public String compile(Environment env) {
        return env.newvar() + "= add f64 0, " + v + "\n";
    }
}

class Variable extends Expr {
    public String varname;
    Variable(String varname) { this.varname = varname; }
    public Type typecheck(Environment env) {
        Type varType = env.getVariable(varname);
        if (varType != Type.INTTYPE && varType != Type.DOUBLETYPE)
            faux.error("Only integer and double supported\n");
        return varType;
    }
    public String compile(Environment env) {
        return env.newvar() + "= load i64, i64* %" + varname + "\n";
    }
}

class Array extends Expr {
    String varname;
    Expr e;
    Array(String varname, Expr e) {
        this.varname = varname;
        this.e = e;
    }
    public Type typecheck(Environment env) {
        if (env.getVariable(varname) != Type.INTARRAYTYPE)
            faux.error("This should be an integer array");
        if (e.typecheck(env) != Type.INTTYPE)
            faux.error("The index must be an int");
        return Type.INTTYPE;
    }
    public String compile(Environment env) {
        String ix = e.compile(env);
        String ixv = env.activevar();
        String pointer = env.newvar();

        return ix + pointer + " = getelementptr inbounds i64, i64* %" + varname +
            ", i64 " + ixv + "\n" + env.newvar() + " = load i64 , i64* " + pointer +
            "\n";
    }
}

abstract class Cond extends AST {
    abstract public String compile(Environment env);
    abstract public void typecheck(Environment env);
}

class Equals extends Cond {
    public Expr e1, e2;
    Equals(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public void typecheck(Environment env) {
        faux.error("Equals: Implement me.\n");
    }
    public String compile(Environment env) {
        faux.error("Implement me.\n");
        return null;
    }
}
class Smaller extends Cond {
    public Expr e1, e2;
    Smaller(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public void typecheck(Environment env) {
        faux.error("Smaller: Implement me.\n");
    }
    public String compile(Environment env) {
        faux.error("Implement me.\n");
        return null;
    }
}
class Greater extends Cond {
    public Expr e1, e2;
    private Type type;
    Greater(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public void typecheck(Environment env) {
        Type e1Type = e1.typecheck(env);
        Type e2Type = e2.typecheck(env);

        if ((e1Type != Type.INTTYPE && e1Type != Type.DOUBLETYPE) || 
                (e2Type != Type.DOUBLETYPE && e2Type != Type.INTTYPE))
            faux.error("Implement me. GREATER\n");

        type = e1Type;
    }
    public String compile(Environment env) {
        String s1 = e1.compile(env);
        String v1 = env.activevar();
        String s2 = e2.compile(env);
        String v2 = env.activevar();
        return switch(type) {
            case INTTYPE -> s1 + s2 + env.newvar() + "= icmp sgt i64 " + v1 + ", " + v2 + "\n";
            case DOUBLETYPE -> s1 + s2 + env.newvar() + "= fcmp sgt f64 " + v1 + ", " + v2 + "\n";
                default -> null;
        };
    }
}

class SmallerEqual extends Cond {
    public Expr e1, e2;
    SmallerEqual(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public void typecheck(Environment env) {
        faux.error("SmallerEq: Implement me.\n");
    }
    public String compile(Environment env) {
        faux.error("Implement me.\n");
        return null;
    }
}
class GreaterEqual extends Cond {
    public Expr e1, e2;
    GreaterEqual(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public void typecheck(Environment env) {
        faux.error("GreaterEq: Implement me.\n");
    }
    public String compile(Environment env) {
        faux.error("Implement me.\n");
        return null;
    }
}
class Unequal extends Cond {
    public Expr e1, e2;
    Unequal(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public void typecheck(Environment env) {
        faux.error("Unequal: Implement me.\n");
    }
    public String compile(Environment env) {
        faux.error("Implement me.\n");
        return null;
    }
}
class Not extends Cond {
    public Cond c;
    Not(Cond c) { this.c = c; }
    public void typecheck(Environment env) { faux.error("Not: Implement me.\n"); }
    public String compile(Environment env) {
        faux.error("Implement me.\n");
        return null;
    }
}
class And extends Cond {
    public Cond c1, c2;
    And(Cond c1, Cond c2) {
        this.c1 = c1;
        this.c2 = c2;
    }
    public void typecheck(Environment env) { faux.error("Implement me.\n"); }
    public String compile(Environment env) {
        faux.error("Implement me.\n");
        return null;
    }
}
class Or extends Cond {
    public Cond c1, c2;
    Or(Cond c1, Cond c2) {
        this.c1 = c1;
        this.c2 = c2;
    }
    public void typecheck(Environment env) { faux.error("Implement me.\n"); }
    public String compile(Environment env) {
        faux.error("Implement me.\n");
        return null;
    }
}
