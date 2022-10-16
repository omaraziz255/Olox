package utils;

public enum packageType {
    EXPRESSION("parser"),
    STMT("interpreter");
    public final String type;
    packageType(String type) { this.type = type; }
}
