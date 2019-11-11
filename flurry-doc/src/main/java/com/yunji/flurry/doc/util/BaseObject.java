package com.yunji.flurry.doc.util;

import java.io.Serializable;

/**
 * @author Denim.leihz 2019-09-10 6:20 PM
 */
public class BaseObject implements Serializable {
    private long errorCode = 0;
    private String errorMessage = "";

    private Object data;

    public BaseObject() {
        this(0, "", null);
    }

    public BaseObject(long errorCode) {
        this(errorCode, "", null);
    }

    public BaseObject(long errorCode, String message) {
        this(errorCode, message, null);
    }

    public BaseObject(long errorCode, String errorMessage, Object data) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public long getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(long errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
