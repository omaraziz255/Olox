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
