package Jolox.utils;

public enum PackageType {
    EXPRESSION("Jolox/parser"),
    STMT("Jolox/interpreter");
    public final String type;
    PackageType(String type) { this.type = type; }
}
