package utils;

public enum PackageType {
    EXPRESSION("parser"),
    STMT("interpreter");
    public final String type;
    PackageType(String type) { this.type = type; }
}
