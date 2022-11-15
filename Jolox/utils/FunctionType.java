package Jolox.utils;

public enum FunctionType {
    FUNCTION("function"),
    INITIALIZER("init"),
    METHOD("method"),
    NONE("none");
    public final String type;
    FunctionType(String type) { this.type = type; }
}
