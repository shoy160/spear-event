package cn.spear.event.core.exception;

/**
 * @author luoyong
 * @date 2022/11/8
 */
public class ConnectorRuntimeException extends RuntimeException {

    public ConnectorRuntimeException() {
    }

    public ConnectorRuntimeException(String message) {
        super(message);
    }

    public ConnectorRuntimeException(Throwable throwable) {
        super(throwable);
    }

    public ConnectorRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
