# C-to-Mips Compiler

This individual project(2023 Fall) is a compiler that translates modified C language code into executable MIPS assembly language. The compiler is implemented entirely in Java and does not require additional dependencies or tools to run.

## Branches

The project is organized into the following branches, each representing a new feature or module added to the compiler:

- **`1_Lexer`**: The lexer module for lexical analysis, responsible for tokenizing the input C code.
- **`2_Parser`**: The parser module, which parses the tokenized input and builds an Abstract Syntax Tree (AST).
- **`3_MidCode`**: The intermediate code generation module, responsible for converting the AST into an intermediate representation using a custom-created four-address code (quadruples).
- **`4_ErrHandler`**: The error handling module, which manages syntax and semantic errors during compilation.
- **`5_Target`**: The target code generation module, which translates the intermediate representation into MIPS assembly code.
- **`6_Optimizer`**: The optimization module, which applies optimizations to the MIPS code to improve performance.

## Features

- **Lexical Analysis**: The lexer identifies keywords, operators, identifiers, and other language constructs in the C code.
- **Syntax Parsing**: The parser checks the syntax of the C code and generates an Abstract Syntax Tree (AST).
- **Intermediate Code**: Converts the AST into a custom four-address code (quadruples) for further processing.
- **Error Handling**: Provides detailed error messages to help identify and fix issues in the C code.
- **MIPS Code Generation**: Generates MIPS assembly code that can be executed on a MIPS processor.
- **Code Optimization**: Optimizes the generated MIPS code to enhance execution efficiency.
