# Olox
My version of the Lox language introduced in the Crafting Interpreters book by Bob Nystrom. 
This repo contains implementations for both Jolox and Colox (implementations of Olox in Java and C respectively). 
At their core, the resultant Olox language is the same, however the approach in each is essentially different.

It is designed to be a bare-bones, easy to use, largely educational language that is dynamically typed and follows an 
imperative paradigm.

## Jolox

Jolox follows an Abstract Syntax Tree-walking method which is fairly simple. By defining core Expressions 
to the language, each representing a single tree node. A tree-walker interpreter can simply parse and evaluate 
those expressions by parsing that tree in a traversal similar to an LRV traversal for binary trees (ASTs are N-ary trees).

This offers a simpler approach but depends largely on the JVM for type resolution and memory management 
(A source of overhead). This along with the inherent inefficiency of tree-walk interpreters make Jolox a less efficient 
implementation compared to its Colox counterpart.

## Colox

Colox incorporates a pipeline of a parser, which parses source code into its associated language tokens and provides 
them to the compiler. The compiler itself is responsible for generating bytecode according to the different combinations
of tokens it receives. Bytecode instructions are instructions that can later be interpreted by the Colox Virtual Machine
using a recursive stack-based descent.

The C-implementation of Olox offers more raw deep dives and optimizations into features that the JVM provided for free 
to Jolox. Features like garbage collection using live object tracking and scoping, call frames, and string interning
help in optimizing Colox in a more customized way to the language's implementation.
