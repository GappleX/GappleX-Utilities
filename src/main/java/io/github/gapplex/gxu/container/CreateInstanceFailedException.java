package io.github.gapplex.gxu.container;

public final class CreateInstanceFailedException extends RuntimeException {
    public CreateInstanceFailedException(String message) {
        super(message);
    }
    public CreateInstanceFailedException(String message, Throwable t) {
        super(message, t);
    }
    public CreateInstanceFailedException(Throwable t) {
        super(t);
    }
}
