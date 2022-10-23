package exceptions;

public class Return extends RuntimeException {
    final Object value;

    public Object getValue() {
        return this.value;
    }

    public Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
