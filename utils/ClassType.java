package utils;

public enum ClassType {

    NONE("None"),
    CLASS("CLASS"),
    SUBCLASS("SUBCLASS");

    public final String type;
    ClassType(String type) { this.type = type; }
}
