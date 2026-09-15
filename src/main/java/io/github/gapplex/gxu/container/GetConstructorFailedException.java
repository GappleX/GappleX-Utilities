package io.github.gapplex.gxu.container;

public final class GetConstructorFailedException extends RuntimeException {
    public GetConstructorFailedException(String message) {
        super(message);
    }
    public GetConstructorFailedException(Throwable t) {
        super(t);
    }
    public GetConstructorFailedException(String message, Throwable t) {
        super(message, t);
    }
}
