package org.apache.dubbo.jsonserializer.util;

/**
 * @author Leihuazhe
 */
public class JException extends Exception {

    private static final long serialVersionUID = 1L;

    public JException() {
        super();
    }

    public JException(String message) {
        super(message);
    }

    public JException(Throwable cause) {
        super(cause);
    }

    public JException(String message, Throwable cause) {
        super(message, cause);
    }
}