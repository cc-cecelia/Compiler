package Parser;

import Lexer.LexType;
import Lexer.Lexer;
import Lexer.Token;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class Parser {
    private static Parser parser = new Parser();
    public FileWriter fileWriter;
    HashMap<Character, String> errors = new HashMap<>();
    Lexer lexer;

    private boolean isLVal = false;

    private Parser() {
        errors.put('i', ";");
        errors.put('j', ")");
        errors.put('k', "]");
    }

    public static Parser getInstance() {
        return parser;
    }

    public void setLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    public void error() {

    }

    public void parseProgram() throws IOException {
        lexer.next();
        compUnit();
    }

    //CompUnit → {Decl} {FuncDef} MainFuncDef
    public void compUnit() throws IOException {
        while (lexer.now != null && (lexer.now.type == LexType.INTTK || lexer.now.type == LexType.CONSTTK || lexer.now.type == LexType.VOIDTK)) {
            if (lexer.now.type == LexType.CONSTTK) {
                decl();
            } else if (lexer.now.type == LexType.VOIDTK) {
                funcDef();
            } else {
                Token preRead = lexer.preRead();
                // int main()
                if (preRead.type == LexType.MAINTK) {
                    mainFuncDef();
                } else if (preRead.type == LexType.IDENFR) {
                    preRead = lexer.preRead(preRead);
                    // int a=1
                    if (preRead.type == LexType.ASSIGN) {
                        decl();
                    }
                    // int func()
                    else if (preRead.type == LexType.LPARENT) {
                        lexer.next();
                        funcDef();
                    } else {
                        error();
                    }
                } else {
                    error();
                }
            }
            lexer.next();
        }
        fileWriter.write("<CompUnit>\n");
        System.out.println("<CompUnit>");
    }

    //MainFuncDef → 'int' 'main' '(' ')' Block
    public void mainFuncDef() throws IOException {
        if (lexer.now.type == LexType.INTTK) {
            lexer.next();
            if (lexer.now.type == LexType.MAINTK) {
                lexer.next();
                if (lexer.now.type == LexType.LPARENT) {
                    lexer.next();
                    if (lexer.now.type == LexType.RPARENT) {
                        lexer.next();
                        block();
                        fileWriter.write("<MainFuncDef>\n");
                        System.out.println("<MainFuncDef>");
                    } else {
                        error();
                    }
                } else {
                    error();
                }
            } else {
                error();
            }
        } else {
            error();
        }
    }
    //  Decl → ConstDecl | VarDecl
    public void decl() throws IOException {
        // Decl → ConstDecl
        if (lexer.now.type == LexType.CONSTTK) {
            constDecl();
        }
        //  Decl → VarDecl
        else if (lexer.now.type == LexType.INTTK) {
            varDecl();
        }
//        fileWriter.write("<Decl>\n");
//        System.out.println("<Decl>");
    }

    // VarDecl → BType VarDef { ',' VarDef } ';'
    public void varDecl() throws IOException {
        lexer.next();
        varDef();
        while (lexer.now.type == LexType.COMMA) {
            lexer.next();
            varDef();
        }
        if (lexer.now.type != LexType.SEMICN) {
            error();
        }
        fileWriter.write("<VarDecl>\n");
        System.out.println("<VarDecl>");
    }

    // VarDef → Ident { '[' ConstExp ']' }
    //          Ident { '[' ConstExp ']' } '=' InitVal
    public void varDef() throws IOException {
        if (lexer.now.type != LexType.IDENFR) {
            error();
        }
        Token preRead = lexer.preRead();
        while (true) {
            if (preRead.type == LexType.LBRACK) {
                lexer.next();
                constExp();
                if (lexer.now.type != LexType.RBRACK) {
                    error();
                }
                preRead = lexer.preRead();
            } else {
                break;
            }
        }
        if (preRead.type == LexType.ASSIGN) {
            lexer.next();
            lexer.next();
            initVal();
        }
        fileWriter.write("<VarDef>\n");
        System.out.println("<VarDef>");
        lexer.next();

        //lexer.next();
//        Token preRead = lexer.preRead();
//        if (preRead.type == LexType.LBRACK) {
//            lexer.next(); // now = '['
//            while (lexer.now.type == LexType.LBRACK) {
//                lexer.next();
//                constExp();
//                if (lexer.now.type != LexType.RBRACK) {
//                    error();
//                }
//                lexer.next();
//            }
//        }

    }
    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'/
    public void initVal() throws IOException {
        if (Judge.isExp(lexer.now)) {
            exp();
        } else if (lexer.now.type == LexType.LBRACE) {
            lexer.next();
            initVal();

            Token preRead = lexer.preRead();
            while (preRead.type == LexType.COMMA) {
                lexer.next();
                lexer.next();
                initVal();
                preRead = lexer.preRead();
            }
            lexer.next();
            if (lexer.now.type != LexType.RBRACE) {
                error();
            }
            lexer.next();
        }
        fileWriter.write("<InitVal>\n");
        System.out.println("<InitVal>");
    }
    // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    public void constDecl() throws IOException {
        lexer.next();
        if (lexer.now.type != LexType.INTTK) {
            error();
        }
        lexer.next();
        constDef();
        Token preRead = lexer.preRead();
        while (preRead.type == LexType.COMMA) {
            lexer.next();
            lexer.next();
            constDef();
            preRead = lexer.preRead();
        }
        lexer.next();
        if (lexer.now.type != LexType.SEMICN) {
            error();
        }
        fileWriter.write("<ConstDecl>\n");
        System.out.println("<ConstDecl>");
    }
    // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    public void constDef() throws IOException {
        lexer.next();
        while (lexer.now.type == LexType.LBRACK) {
            lexer.next();
            constExp();
            if (lexer.now.type != LexType.RBRACK) {
                error();
            }
            lexer.next();
        }
        if (lexer.now.type != LexType.ASSIGN) {
            error();
        }
        lexer.next();
        constInitVal();
        fileWriter.write("<ConstDef>\n");
        System.out.println("<ConstDef>");
    }

    public void constExp() throws IOException {
        addExpModified();
        fileWriter.write("<ConstExp>\n");
        System.out.println("<ConstExp>");
    }

    public void funcType() throws IOException {
        if (!Judge.isFuncType(lexer.now)) {
            error();
        }
        fileWriter.write("<FuncType>\n");
        System.out.println("<FuncType>");
    }

    // 函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public void funcDef() throws IOException {
        funcType();
        lexer.next();
        if (lexer.now.type != LexType.IDENFR) {
            error();
        }
        lexer.next();
        if (lexer.now.type != LexType.LPARENT) {
            error();
        }
        Token preRead = lexer.preRead();
        if (preRead.type != LexType.RPARENT) {
            lexer.next();
            funcParams();
            if (lexer.now.type != LexType.RPARENT) {
                error();
            }
        } else {
            lexer.next(); // now = )
            lexer.next(); // now = {
        }
        block();
        fileWriter.write("<FuncDef>\n");
        System.out.println("<FuncDef>");
    }

    //ConstInitVal → ConstExp
    //              '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    public void constInitVal() throws IOException {
        if (lexer.now.type == LexType.LBRACE) {
            Token preRead = lexer.preRead();
            if (preRead.type != LexType.RBRACE) {
                lexer.next();
                constInitVal();
                while (lexer.now.type == LexType.COMMA) {
                    lexer.next();
                    constInitVal();
                }
                if (lexer.now.type != LexType.RBRACE) {
                    error();
                }
            } else {
                lexer.next();
            }
        } else if (Judge.isExp(lexer.now)) {
            constExp();
        } else {
            error();
        }
        fileWriter.write("<ConstInitVal>\n");
        System.out.println("<ConstInitVal>");
    }

    // Block → '{' { BlockItem } '}'
    public void block() throws IOException {
        if (lexer.now.type == LexType.LBRACE) {
            lexer.next();
            while (Judge.isBlockItem(lexer.now)) {
                blockItem();
                lexer.next();
            }
            if (lexer.now.type == LexType.RBRACE) {
                fileWriter.write("<Block>\n");
                System.out.println("<Block>");
            } else {
                error();
            }
        } else {
            error();
        }
    }

    // BlockItem → Decl | Stmt
    public void blockItem() throws IOException {
        // Decl
        if (Judge.isDecl(lexer.now)) {
            decl();
        }
        // Stmt
        else if (Judge.isStmt(lexer.now)) {
            stmt();
        } else {
            error();
            return;
        }
//        fileWriter.write("<BlockItem>\n");
//        System.out.println("<BlockItem>");
    }

    public void stmt() throws IOException {
        // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        if (lexer.now.type == LexType.IFTK) {
            _ifStmt();
        }
        // for
        else if (lexer.now.type == LexType.FORTK) {
            _forStmt();
        }
        // break; | continue;
        else if (lexer.now.type == LexType.BREAKTK || lexer.now.type == LexType.CONTINUETK) {
            lexer.next();
            if (lexer.now.type != LexType.SEMICN) {
                error();
            }
        }
        // return
        else if (lexer.now.type == LexType.RETURNTK) {
            _returnStmt();
        }
        // printf
        else if (lexer.now.type == LexType.PRINTFTK) {
            _printfStmt();
        }
        // block
        else if (lexer.now.type == LexType.LBRACE) {
            block();
        }
        // LVal '=' *2 | Exp;
        else if (lexer.now.type == LexType.IDENFR) {
            //lexer.next();
            isLVal = false;
            lexer.trace = new LinkedList<>();
            _exp();
            if (isLVal) {
//                fileWriter.write("<LVal>\n");
//                System.out.println("<LVal>");
//                lexer.next();
            }
            if (lexer.now.type == LexType.SEMICN) {
                // 回溯 然后重新解析 exp
                lexer.traceBack();
                exp();
//                fileWriter.write("<Exp>\n");
//                System.out.println("<Exp>");
            } else if (lexer.now.type == LexType.ASSIGN) {
//                LVal '=' Exp ';'
//                LVal '=' 'getint''('')'';'
                lexer.next();
                if (Judge.isExp(lexer.now)) {
                    exp();
                    if (lexer.now.type != LexType.SEMICN) {
                        error();
                    }
                } else if (lexer.now.type == LexType.GETINTTK) {
                    lexer.next();
                    if (lexer.now.type != LexType.LPARENT) {
                        error();
                    }
                    lexer.next();
                    if (lexer.now.type != LexType.RPARENT) {
                        error();
                    }
                    lexer.next();
                    if (lexer.now.type != LexType.SEMICN) {
                        error();
                    }
                }
            }
        } else if (lexer.now.type == LexType.SEMICN) {

        } else {
            error();
            return;
        }
        fileWriter.write("<Stmt>\n");
        System.out.println("<Stmt>");
    }

    public void _traceBack() {

    }

    public boolean _LVal() throws IOException {
        boolean flag = false;
        while (lexer.now.type == LexType.LBRACK) {
            flag = true;
            lexer.next();
            exp();
            if (lexer.now.type != LexType.RBRACK) {
                error();
            }
            lexer.next();
        }
//        if (!flag) {
//            lexer.next();
//        }
        return flag;
    }

    //LVal → Ident {'[' Exp ']'}
    public void lVal() throws IOException {
        boolean flag = _LVal();
        fileWriter.write("<LVal>\n");
        System.out.println("<LVal>");
//        if (!flag) {
//            lexer.next();
//        }
    }

    // ifStmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public void _ifStmt() throws IOException {
        if (lexer.now.type == LexType.IFTK) {
            lexer.next();
            if (lexer.now.type != LexType.LPARENT) {
                error();
            }
            lexer.next();
            cond();
            if (lexer.now.type != LexType.RPARENT) {
                error();
            }
            lexer.next();
            stmt();
            Token preRead = lexer.preRead();
            if (preRead.type == LexType.ELSETK) {
                lexer.next();
                stmt();
            }
        } else {
            error();
        }
    }

    //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    public void _forStmt() throws IOException {
        Token preRead;
        if (lexer.now.type == LexType.FORTK) {
            lexer.next();
            if (lexer.now.type != LexType.LPARENT) {
                error();
            }
            preRead = lexer.preRead(); // now = (
            if (preRead.type == LexType.SEMICN) {
                // 无 ForStmt
                lexer.next(); // now = 第一个;
                preRead = lexer.preRead(); //
                if (preRead.type == LexType.SEMICN) {
                    // 无condition
                    lexer.next();
                    preRead = lexer.preRead(); // now = 第二个;
                    if (preRead.type == LexType.RPARENT) {
                        // 无ForStmt
                        lexer.next();
                        // now = )
                        lexer.next();
                        stmt();
                    } else {
                        // now = 第二个;
                        // 有ForStmt
                        lexer.next();
                        forStmt();
                        lexer.next(); // ？
                        if (lexer.now.type != LexType.RPARENT) {
                            error();
                        }
                        lexer.next();
                        stmt();
                    }
                } else {
                    // 有condition
                    // now = 第一个;
                    lexer.next();
                    cond();
                    lexer.next(); // ?
                    if (lexer.now.type != LexType.SEMICN) {
                        error();
                    }
                    preRead = lexer.preRead();
                    if (preRead.type == LexType.RPARENT) {
                        // 无 ForStmt
                        // now = ;
                        lexer.next();
                        lexer.next();
                        stmt();
                    } else {
                        // 有 ForStmt
                        // now = ;
                        lexer.next();
                        forStmt();
                        lexer.next(); // ?
                        if (lexer.now.type != LexType.RPARENT) {
                            error();
                        }
                        lexer.next();
                        stmt();
                    }
                }
            } else {
                // now = (
                //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                lexer.next();
                forStmt();
                lexer.next(); // ?
                if (lexer.now.type != LexType.SEMICN) {
                    error();
                }
                preRead = lexer.preRead();
                if (preRead.type == LexType.SEMICN) {
                    // no condition
                    lexer.next();
                    // now = second ;
                    preRead = lexer.preRead();
                    if (preRead.type == LexType.RPARENT) {
                        // no ForStmt
                        lexer.next();
                        lexer.next();
                        stmt();
                    } else {
                        // hava ForStmt
                        lexer.next();
                        forStmt();
                        lexer.next(); // ?
                        if (lexer.now.type != LexType.RPARENT) {
                            error();
                        }
                        lexer.next();
                        stmt();
                    }
                } else {
                    // has condition
                    // now = first ;
                    //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                    lexer.next();
                    cond();
                    lexer.next(); // ?
                    if (lexer.now.type != LexType.SEMICN) {
                        error();
                    }
                    preRead = lexer.preRead();
                    if (preRead.type == LexType.RPARENT) {
                        // no ForStmt
                        // now = ;
                        lexer.next();
                        lexer.next();
                        stmt();
                    } else {
                        lexer.next();
                        forStmt();
                        lexer.next();//?
                        if (lexer.now.type != LexType.RPARENT) {
                            error();
                        }
                        lexer.next();
                        stmt();
                    }
                }
            }
        }
    }

    //ForStmt → LVal '=' Exp
    public void forStmt() throws IOException {
        if (lexer.now.type != LexType.IDENFR) {
            error();
        }
        lVal();
        if (lexer.now.type != LexType.ASSIGN) {
            error();
        }
        lexer.next();
        exp();
        fileWriter.write("<ForStmt>\n");
        System.out.println("<ForStmt>");
    }

    //'return' [Exp] ';'
    public void _returnStmt() throws IOException {
        Token preRead = lexer.preRead();
        if (preRead.type != LexType.SEMICN) {
            lexer.next();
            exp();
            lexer.next();
            if (lexer.now.type != LexType.SEMICN) {
                error();
            }
        }
    }

    //'printf''('FormatString{ ',' Exp}')'';'
    public void _printfStmt() throws IOException {
        lexer.next();
        if (lexer.now.type != LexType.LPARENT) {
            error();
        }
        lexer.next();
//        formatString();
        Token preRead = lexer.preRead();
        if (preRead.type == LexType.COMMA) {
            while (preRead.type == LexType.COMMA) {
                lexer.next(); // now = ,
                lexer.next();
                exp();
                preRead = lexer.preRead();
            }
        }
        lexer.next();
        if (lexer.now.type != LexType.RPARENT) {
            error();
        }
        lexer.next();
        if (lexer.now.type != LexType.SEMICN) {
            error();
        }
    }

    public void exp() throws IOException {
        //_exp();
        addExpModified();
        fileWriter.write("<Exp>\n");
        System.out.println("<Exp>");
        //lexer.next();
    }

    public void exp1() {
        _addExp();
    }

    public void _exp() throws IOException {
        _addExpModified();
    }

    // Cond → LOrExp
    public void cond() throws IOException {
        lOrExpModified();
        fileWriter.write("<Cond>\n");
        System.out.println("<Cond>");
    }

    public void lOrExpModified() throws IOException {
        boolean flag = false;
        lAndExpModified();
        Token preRead = lexer.preRead();
        while(preRead.type == LexType.OR) {
            fileWriter.write("<LOrExp>\n");
            System.out.println("<LOrExp>");
            flag = true;
            lexer.next();
            lexer.next();
            addExpModified();
            preRead = lexer.preRead();
        }
        fileWriter.write("<LOrExp>\n");
        System.out.println("<LOrExp>");
    }

    public void lAndExpModified() throws IOException {
        boolean flag = false;
        eqExp();
        Token preRead = lexer.preRead();
        while(preRead.type == LexType.AND) {
            fileWriter.write("<LAndExp>\n");
            System.out.println("<LAndExp>");
            flag = true;
            lexer.next();
            lexer.next();
            addExpModified();
            preRead = lexer.preRead();
        }
        fileWriter.write("<LAndExp>\n");
        System.out.println("<LAndExp>");
    }

    public void eqExp() throws IOException {
        boolean flag = false;
        relExp();
        Token preRead = lexer.preRead();
        while(preRead.type == LexType.EQL || preRead.type == LexType.NEQ) {
            fileWriter.write("<EqExp>\n");
            System.out.println("<EqExp>");
            flag = true;
            lexer.next();
            lexer.next();
            addExpModified();
            preRead = lexer.preRead();
        }
        fileWriter.write("<EqExp>\n");
        System.out.println("<EqExp>");
    }

    public void relExp() throws IOException {
        boolean flag = false;
        addExpModified();
        Token preRead = lexer.preRead();
        while(Judge.isCompare(preRead)) {
            fileWriter.write("<RelExp>\n");
            System.out.println("<RelExp>");
            flag = true;
            lexer.next();
            lexer.next();
            addExpModified();
            preRead = lexer.preRead();
        }
        fileWriter.write("<RelExp>\n");
        System.out.println("<RelExp>");
    }

    public void _mulExp(){
        if (Judge.isAddAndMul(lexer.now)) {
            try {
                _unaryExp();
            } catch (IOException e) {
                System.out.println(1);
            }
            _mul2Exp();
        } else {
            error();
        }
    }

    public void _mul2Exp() {
        if (Judge.isTerm(lexer.now)) {
            lexer.next();
            try {
                _unaryExp();
            } catch (IOException e) {
                System.out.println(1);
            }
            lexer.next();
            _mul2Exp();
        } else if (lexer.now.type == LexType.PLUS || lexer.now.type == LexType.MINU) {

        } else {
            error();
        }
    }

    public void _addExp() {
        if (Judge.isAddAndMul(lexer.now)) {
            _mulExp();
            lexer.next();
            _add2Exp();lexer
        } else {
            error();
        }
    }

    public void _add2Exp() {
        if (lexer.now.type == LexType.PLUS || lexer.now.type == LexType.MINU) {
            lexer.next();
            _mulExp();
            _add2Exp();
        } else {
            error();
        }
    }

    public void _addExpModified() throws IOException {
        _mulExpModified();
        while (lexer.now.type == LexType.PLUS || lexer.now.type == LexType.MINU) {
//            fileWriter.write("<AddExp>\n");
//            System.out.println("<AddExp>");
            lexer.next();
            mulExpModified();
            lexer.next();
        }
//        fileWriter.write("<MulExp>\n");
//        System.out.println("<MulExp>");
    }

    public void addExpModified() throws IOException {
        boolean flag = false;
        mulExpModified();
        Token preRead = lexer.preRead();
        while (preRead.type == LexType.PLUS || preRead.type == LexType.MINU) {
            fileWriter.write("<AddExp>\n");
            System.out.println("<AddExp>");
            flag = true;
            lexer.next(); // now = + | -
            lexer.next();
            mulExpModified();
            preRead = lexer.preRead();
        }
        fileWriter.write("<AddExp>\n");
        System.out.println("<AddExp>");
    }

    public void _mulExpModified() throws IOException {
        _unaryExp();
        Token preRead = lexer.preRead();
        while (preRead.type == LexType.MULT ||
                preRead.type == LexType.DIV ||
                preRead.type == LexType.MOD) {
//            fileWriter.write("<MulExp>\n");
//            System.out.println("<MulExp>");
            lexer.next();
            lexer.next();
            unaryExp();
            preRead = lexer.preRead();
        }
//        fileWriter.write("<UnaryExp>\n");
//        System.out.println("<UnaryExp>");
    }

    public void mulExpModified() throws IOException {
        boolean flag = false;
        unaryExp();
        Token preRead = lexer.preRead();

        while (preRead.type == LexType.MULT ||
                preRead.type == LexType.DIV ||
                preRead.type == LexType.MOD) {
            flag = true;
//            fileWriter.write("<MulExp>\n");
//            System.out.println("<MulExp>");
            lexer.next();
            lexer.next();
            unaryExp();
            preRead = lexer.preRead();
        }
        if (flag) {
            fileWriter.write("<UnaryExp>\n");
            System.out.println("<UnaryExp>");
        }
        fileWriter.write("<MulExp>\n");
        System.out.println("<MulExp>");
    }

    public void _unaryExp() throws IOException {
        if (Judge.isUnaryOp(lexer.now)) {
            lexer.next();
            _unaryExp();
        }
        // PrimaryExp
        else if (lexer.now.type == LexType.LPARENT || lexer.now.type == LexType.INTCON) {
            _primaryExp();
        } else if (lexer.now.type == LexType.IDENFR) {
            Token preRead = lexer.preRead();
            if (preRead.type == LexType.LPARENT) {
                lexer.next(); // now = (
                preRead = lexer.preRead();
                if (preRead.type != LexType.RPARENT) {
                    lexer.next();
                    funcParams();
                }
            } else {
                _primaryExp();
            }
        } else {
            error();
        }
    }

    public void unaryExp() throws IOException {
        // UnaryOp UnaryExp
        if (Judge.isUnaryOp(lexer.now)) {
            lexer.next();
            _unaryExp();
        }
        // PrimaryExp
        else if (lexer.now.type == LexType.LPARENT || lexer.now.type == LexType.INTCON) {
            primaryExp();
        } else if (lexer.now.type == LexType.IDENFR) {
            Token preRead = lexer.preRead();
            if (preRead.type == LexType.LPARENT) {
                lexer.next(); // now = (
                preRead = lexer.preRead();
                if (preRead.type != LexType.RPARENT) {
                    lexer.next();
                    funcParams();
                }
            } else {
                primaryExp();
            }
        } else {
            error();
        }
        fileWriter.write("<UnaryExp>\n");
        System.out.println("<UnaryExp>");
    }

    public void _primaryExp() throws IOException {
        if (lexer.now.type == LexType.LPARENT) {
            lexer.next();
            exp();
            if (lexer.now.type != LexType.RPARENT) {
                error();
            }
        } else if (lexer.now.type == LexType.IDENFR) {
            _LVal();
            isLVal = true;
        } else if (lexer.now.type == LexType.INTCON) {

        } else {
            error();
        }
        if (isLVal) {
            fileWriter.write("<LVal>\n");
            System.out.println("<LVal>");
            lexer.next();
        }
    }

    //PrimaryExp → '(' Exp ')' | LVal | Number
    public void primaryExp() throws IOException {
        if (lexer.now.type == LexType.LPARENT) {
            lexer.next();
            exp();
            if (lexer.now.type != LexType.RPARENT) {
                error();
            }
        } else if (lexer.now.type == LexType.IDENFR) {
            lVal();
            isLVal = true;
        } else if (lexer.now.type == LexType.INTCON) {
            fileWriter.write("<Number>\n");
            System.out.println("<Number>");
        } else {
            error();
        }
        fileWriter.write("<PrimaryExp>\n");
        System.out.println("<PrimaryExp>");
        // lexer.next();
    }

    public void funcParams() throws IOException {
        exp();
        while (lexer.now.type == LexType.COMMA) {
            lexer.next();
            exp();
        }
        fileWriter.write("<FuncParams>\n");
        System.out.println("<FuncParams>");
    }
}
