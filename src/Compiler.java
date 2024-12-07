import Lexer.Lexer;
import Parser.Parser;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Compiler {
    public static void main(String[] args) {
        Lexer lexer = Lexer.getInstance();
        Parser parser = Parser.getInstance();
        parser.setLexer(lexer);
        try {
            lexer.src = new String(Files.readAllBytes(Paths.get("testfile.txt")));
        } catch (IOException e) {
            System.out.println("no such file!");
        }
        lexer.analysis();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("output.txt");
            parser.fileWriter = fileWriter;
            lexer.fileWriter = fileWriter;
        } catch (IOException e) {
            System.out.println("no such file!");
        }
        try {
            parser.parseProgram();
        } catch (IOException e) {
            System.out.println("Write fail!");
        }
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException e) {
            System.out.println("Close fail!");
        }

        // lexer.print(); LAB1-Lexer
    }
}