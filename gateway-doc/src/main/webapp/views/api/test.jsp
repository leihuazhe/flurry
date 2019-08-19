<%--suppress ALL --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <jsp:include page="../core/resource.jsp"/>
    <style type="text/css">
        .tree li {
            list-style-type: none;
        }
    </style>
    <link rel="stylesheet" href="${basePath}/js/json/json.format.css">
    <link rel="stylesheet" href="${basePath}/css/jquery.datetimepicker.css">
    <script src="${basePath}/js/api/test.js"></script>
    <script src="${basePath}/js/json/json.format.js"></script>
    <script src="${basePath}/js/formatmarked.js"></script>
    <script src="${basePath}/js/jquery.datetimepicker.full.min.js"></script>
    <script src="${basePath}/js/laydate/laydate.js"></script>
    <script>
        Date.prototype.pattern = function (fmt) {
            var o;
            o = {
                "M+": this.getMonth() + 1, //月份
                "d+": this.getDate(), //日
                "h+": this.getHours() % 24 === 0 ? 00 : this.getHours() % 24, //小时
                "H+": this.getHours(), //小时
                "m+": this.getMinutes(), //分
                "s+": this.getSeconds(), //秒
                "q+": Math.floor((this.getMonth() + 3) / 3), //季度
                "S": this.getMilliseconds() //毫秒
            };
            var week = {
                "0": "/u65e5",
                "1": "/u4e00",
                "2": "/u4e8c",
                "3": "/u4e09",
                "4": "/u56db",
                "5": "/u4e94",
                "6": "/u516d"
            };
            if (/(y+)/.test(fmt)) {
                fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
            }
            if (/(E+)/.test(fmt)) {
                fmt = fmt.replace(RegExp.$1, ((RegExp.$1.length > 1) ? (RegExp.$1.length > 2 ? "/u661f/u671f" : "/u5468") : "") + week[this.getDay() + ""]);
            }
            for (var k in o) {
                if (new RegExp("(" + k + ")").test(fmt)) {
                    fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
                }
            }
            return fmt;
        }
        $(function () {

            var url = window.basePath + "/api/findService/" + "${service.namespace}.${service.name}" + "/" + "${service.meta.version}" + ".htm";
            var settings = {type: "get", url: url, dataType: "json"};
            $.ajax(settings).done(function (result) {

                var methodName = "${method.name}";
                for (var i = 0; i < result.methods.length; i++) {

                    if (result.methods[i].name == methodName) {

                        //生成参数输入列表
                        var method = result.methods[i];

                        for (var index = 0; index < method.request.fields.length; index++) {

                            var field = method.request.fields[index];
                            var li = getDataTypeElement(field.dataType, field.name, result, field.optional, field.doc);
                            $('#tree').append(li);
                        }

                        //生成示范报文
                        var parameter = {};
                        for (var index = 0; index < method.request.fields.length; index++) {
                            var field = method.request.fields[index];
                            parameter[field.name] = getJsonSample(field.dataType, result);
                        }
                        Process("requestSampleData", parameter);

                        //生成示范请求
                        var respParameter = {};
                        // console.info(method.response)
                        for (var index = 0; index < method.response.fields.length; index++) {
                            var field = method.response.fields[index];
                            respParameter[field.name] = getJsonSample(field.dataType, result);
                        }

                        respParameter.status = 1;
                        Process("responseSampleData", respParameter);
                        //console.info(respParameter)

                    }
                }

                $('input.datetimepicker').datetimepicker();
            });

            // 获取请求模版
            getRequestTemplate('${service.namespace}.${service.name}', '${service.meta.version}', '${method.name}');
            laydate.render({
                elem: '#selected-date',
                type: 'datetime'
            });
        });

        // 获取时间戳
        function dateToUnixTimestamp() {
            var selectedDateTime = $("#selected-date").val();
            if (selectedDateTime === undefined || selectedDateTime.trim() === "") {
                return;
            }
            try {
                var date = new Date(selectedDateTime);
                var unixtimesteamp = date.getTime();
                $("#unix-timesteamp-text").val(unixtimesteamp);
            } catch (e) {
                alert("时间戳转换异常");
                return;
            }
        }

        // 时间戳转时间
        function unixTimestampToDate() {
            var inputUnixTimestamp = $("#input-unix-timesteamp").val();
            if (inputUnixTimestamp === undefined || inputUnixTimestamp.trim() === "") {
                return;
            }
            try {
                var unixTimestamp = new Date((inputUnixTimestamp / 1000) * 1000);
                commonTime = unixTimestamp.pattern("yyyy-MM-dd hh:mm:ss");
                $("#convert-datetime").val(commonTime);
            } catch (e) {
                alert("时间转换异常");
                return
            }
        }
    </script>
</head>
<body>
<jsp:include page="../core/scroll-top.jsp"/>
<jsp:include page="../core/header.jsp"/>

<div class="bs-docs-content container">
    <div class="row mt5">
        <ol class="breadcrumb">
            <li><a href="${basePath}/">首页</a></li>
            <li><a href="${basePath}/api/index.htm">API</a></li>
            <li><a href="${basePath}/api/service/${service.namespace}.${service.name}/${service.meta.version}.htm">${service.name}</a></li>
            <li>
                <a href="${basePath}/api/method/${service.namespace}.${service.name}/${service.meta.version}/${method.name}.htm">${method.name}</a>
            </li>
            <li><a class="active">在线测试</a></li>
        </ol>
    </div>
    <div class="row">
        <div class="col-sm-3 col-md-3">
            <div class="list-group">
                <c:forEach var="s" items="${services}">
                    <a class="list-group-item ${s == service ? 'active' : ''}"
                       href="${basePath}/api/service/${s.name}/${s.meta.version}.htm">
                        <span class="glyphicon glyphicon-chevron-right"></span>
                        <c:choose>
                            <c:when test="${empty s.doc}">
                                <c:out value="${s.name}API"/>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${s.doc}API"/>
                            </c:otherwise>
                        </c:choose>
                    </a>
                </c:forEach>
            </div>
        </div>
        <div class="col-sm-9 col-md-9">
            <div class="page-header mt5">
                <h1 class="mt5">在线测试工具
                </h1>
            </div>

            <form class="form-horizontal">
                <div class="form-group">
                    <label for="serviceName" class="col-sm-2 control-label">服务名</label>

                    <div class="col-sm-10">
                        <input type="text" class="form-control" id="serviceName"
                               value="${service.namespace}.${service.name}"
                               disabled="disabled">
                    </div>
                </div>
                <div class="form-group">
                    <label for="version" class="col-sm-2 control-label">版本号</label>

                    <div class="col-sm-10">
                        <input type="text" class="form-control" id="version" value="${service.meta.version}"
                               disabled="disabled">
                    </div>
                </div>
                <div class="form-group">
                    <label for="methodName" class="col-sm-2 control-label">方法名</label>

                    <div class="col-sm-10">
                        <input type="text" class="form-control" id="methodName" value="${method.name}"
                               disabled="disabled">
                    </div>
                </div>
            </form>
            <hr>
            <%--<h4>小工具</h4>--%>
            <%--<hr>--%>
            <%--<div class="form-inline">--%>
                <%--时间转Unix时间戳(毫秒):--%>
                <%--<div class="input-group">--%>
                    <%--<input type="text" class="form-control" placeholder="选择时间" id="selected-date">--%>
                    <%--<span class="input-group-btn">--%>
                        <%--<button type="button" class="btn btn-info" onclick="dateToUnixTimestamp()">时间=>Unix时间戳</button>--%>
                    <%--</span>--%>
                <%--</div>--%>
                <%--<div class="input-group">--%>
                    <%--<input type="text" class="form-control" id="unix-timesteamp-text">--%>
                <%--</div>--%>
            <%--</div>--%>
            <%--<br>--%>
            <%--<div class="form-inline">--%>
                <%--Unix时间戳(毫秒)转时间:--%>
                <%--<div class="input-group">--%>
                    <%--<input type="text" class="form-control" placeholder="输入unix时间戳毫秒值" id="input-unix-timesteamp">--%>
                    <%--<span class="input-group-btn">--%>
                        <%--<button type="button" class="btn btn-info" onclick="unixTimestampToDate()">Unix时间戳=>时间</button>--%>
                    <%--</span>--%>
                <%--</div>--%>
                <%--<div class="input-group">--%>
                    <%--<input type="text" class="form-control" id="convert-datetime">--%>
                <%--</div>--%>
            <%--</div>--%>
            <%--<hr>--%>
            <h4>请求参数</h4>

            <div style="border: 1px solid #95B8E7">
                <ul id="tree" class="tree">

                </ul>
            </div>

            <br>
            <button type="button" class="btn btn-info"
                    onclick="applyTest('${service.namespace}.${service.name}', '${service.meta.version}', '${method.name}');">
                提交请求
            </button>
            <br>
            <hr>

            <div style="height:400px;padding:10px;border:solid 1px #95B8E7;border-radius:0;resize: none;position: relative">
                <ul id="myTabs" class="nav nav-tabs" role="tablist">
                    <li role="presentation" class="active">
                        <a href="#requestData" id="requestData-tab" role="tab" data-toggle="tab"
                           aria-controls="requestData" aria-expanded="true"
                           onclick="$('#apply-template-button-group').show()">请求数据</a>
                    </li>
                    <li role="presentation">
                        <a href="#requestSample" id="requestSample-tab" role="tab" data-toggle="tab"
                           aria-controls="requestSample" onclick="$('#apply-template-button-group').hide()">示范报文</a>
                    </li>
                    <li role="presentation">
                        <a href="#requestPaste" id="requestPaste-tab" role="tab" data-toggle="tab"
                           aria-controls="requestPaste" onclick="$('#apply-template-button-group').hide()">json请求</a>
                    </li>
                    <li role="presentation">
                        <a href="#responseSample" id="responseSample-tab" role="tab" data-toggle="tab"
                           aria-controls="responseSample" onclick="$('#apply-template-button-group').hide()">模拟响应数据</a>
                    </li>
                </ul>

                <div id="myTabContent" class="tab-content" style="height:330px;overflow-y:auto;">
                    <div role="tabpane1" class="tab-pane fade in active" id="requestData"
                         aria-labelledby="requestData-tab">
                        <div id="json-request"></div>
                    </div>
                    <div role="tabpane1" class="tab-pane fade" id="requestSample" aria-labelledby="requestData-tab">
                        <div id="requestSampleData"></div>
                    </div>

                    <div role="tabpane1" class="tab-pane fade" id="responseSample" aria-labelledby="responseData-tab">
                        <div id="responseSampleData"></div>
                    </div>

                    <div role="tabpane1" class="tab-pane fade" id="requestPaste" aria-labelledby="requestData-tab">
                        <div id="requestPasteData">
                            <p style="color: red">tip：粘贴或者书写请求json提交请求</p>
                            <textarea style="width: 100%;height: 240px;resize: none;" id="pasteJsonBox"></textarea>
                            <button type="button" class="btn btn-success"
                                    onclick=applyTestForJsonStr('${service.namespace}.${service.name}','${service.meta.version}','${method.name}')>
                                提交请求
                            </button>
                        </div>
                    </div>
                </div>
                <%--保存请求模版--%>
                <div id="apply-template-button-group">
                    <button type="button" id="applyTemplateBut" style="position: absolute;left: 10px;bottom: 10px;"
                            class="btn btn-info" onclick=openLabelInput()>保存模版
                    </button>
                    <div id="label-submit-box" style="display: none;position: absolute;left: 10px;bottom: 10px;">
                        <div class="row">
                            <div class="col-lg-5">
                                <div class="input-group">
                                    <input id="template-label-text" type="text" class="form-control"
                                           placeholder="模版名称，五字以内！">
                                    <span class="input-group-btn">
                            <button class="btn btn-info" type="button"
                                    onclick=applyTemplate('${service.namespace}.${service.name}','${service.meta.version}','${method.name}')>提交</button>
                        </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <h4>返回数据</h4>

            <div id="json-result"
                 style="height:300px;padding:20px;border:solid 1px #95B8E7;border-radius:0;resize: none;overflow-y:auto;"></div>


        </div>
    </div>
</div>

<jsp:include page="../core/footer.jsp"/>
</body>
</html>