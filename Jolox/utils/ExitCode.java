package Jolox.utils;

public enum ExitCode {
    COMMAND_LINE_USAGE_ERROR(64),
    USER_DATA_INCORRECT(65),
    RUNTIME_ERROR(70);

    public final int code;

    ExitCode(int code) {
        this.code = code;
    }
}
