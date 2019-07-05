package org.apache.dubbo.demo;

import java.io.Serializable;

public class Result implements Serializable {

    private final int responseId;

    private final String detail;

    public Result(int responseId, String detail) {
        this.responseId = responseId;
        this.detail = detail;
    }

    public int getResponseId() {
        return responseId;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return "Result{" +
                "responseId=" + responseId +
                ", detail='" + detail + '\'' +
                '}';
    }
}
