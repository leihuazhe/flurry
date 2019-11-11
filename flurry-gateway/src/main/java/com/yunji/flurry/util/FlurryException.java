package com.yunji.flurry.util;

public class FlurryException extends RuntimeException {

    private int code;
    private String msg;

    public FlurryException(Exception err) {
        super(err);
        this.code = GateWayErrorCode.SUCCESS.getCode();
        this.msg = err.getMessage();
    }

    public FlurryException(String msg) {
        this.msg = msg;
    }

    public FlurryException(int code, String msg) {
        super(code + ":" + msg);

        this.code = code;
        this.msg = msg;
    }

    public FlurryException(int code, String msg, Throwable cause) {
        super(cause);
        this.code = code;
        this.msg = msg;
    }

    public FlurryException(GateWayErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMsg());
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return code + ":" + msg;
    }

}