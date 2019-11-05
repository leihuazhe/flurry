package com.yunji.gateway.util;

/**
 * desc: DapengMeshCode ErrorCodeEnum
 *
 * @author hz.lei
 * @since 2018年08月27日 下午11:20
 */
public enum GateWayErrorCode {
    SUCCESS(0, "操作成功！"),

    ERROR(-1, "系统异常！"),
    FAIL(1, "操作失败！"),
    PARAM_ERROR(-1000, "参数异常！"),

    /**
     * IllegalRequest
     */
    IllegalRequest(1501, "请求url不合法"),

    ProcessReqFailed(1502, "网关错误,处理请求异常"),

    RequestUrlNotSupport(1503, "网关不支持该请求Url"),

    RequestTypeNotSupport(1504, "网关不支持该请求类型"),

    MeshUnknownError(1505, "网关服务器出现未知错误"),

    MeshShutdownSoon(1506, "health check is yellow,container will shutdown soon"),

    AuthSecretError(1507, "网关鉴权失败,可能原因是不正确的apiKey或加密格式"),

    OpenAuthEnableError(1508, "网关接口需要鉴权,未开放无需鉴权功能"),

    EchoUnknowEx(1509, "Echo接口请求url格式不正确"),

    MeshUnknowEx(1510, "ApiMesh未知异常"),

    AuthParameterEx(1511, "请求校验参数为空,请检查 api-key,timestamp"),

    AuthSecretEx(1512, "请求校验参数secret,secret2 至少有一个不为空"),

    ParameterError(1513, "请求参数 Request 部分参数不能为空"),

    IllegalParams(1514, "缺失请求参数"),

    RemotingError(1550, "当前请求出现远程remote错误"),

    RemotingNullError(1551, "服务端返回空指针异常"),

    TimeOutError(1555, "当前请求超时,请重试.");


    private int code;
    private String msg;

    GateWayErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String toString() {
        return code + ":" + msg;
    }

}
