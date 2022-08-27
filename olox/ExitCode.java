package olox;

public enum ExitCode {
    COMMAND_LINE_USAGE_ERROR(64),
    USER_DATA_INCORRECT(65);

    public final int code;

    ExitCode(int code) {
        this.code = code;
    }
}
