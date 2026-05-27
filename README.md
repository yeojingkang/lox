This repository contains my implementation of the Lox language described in the book *Crafting Interpreters* by Robert Nystrom.
This is my first foray into programming language implementation outside of my graduate coursework.

Features I added to Lox outside its specifications:
- C-style block comments, with nesting support
- Ternary operator
    ```
    <cond> ? <e1> : <e2> // Returns <e1> if <cond> is truthy, otherwise returns <e2>
    ```
- Comma operator
    ```
    <e1>, <e2> // Evaluates <e1>, then <e2> and returns the value of <e2>
    ```
- String concatenation with non-string datatypes
- `break` statement
- Lambdas
    ```
    fun (<params>) { <statements> }
    ```

## Stretch goals
- Implement Lox in C++
- Implement Lox in OCaml
