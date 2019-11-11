package com.yunji.gateway.util;

public class GatewayException extends RuntimeException {

    private int code;
    private String msg;

    public GatewayException(Exception err) {
        super(err);
        this.code = GateWayErrorCode.SUCCESS.getCode();
        this.msg = err.getMessage();
    }

    public GatewayException(String msg) {
        this.msg = msg;
    }

    public GatewayException(int code, String msg) {
        super(code + ":" + msg);

        this.code = code;
        this.msg = msg;
    }

    public GatewayException(int code, String msg, Throwable cause) {
        super(cause);
        this.code = code;
        this.msg = msg;
    }

    public GatewayException(GateWayErrorCode errorCode) {
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