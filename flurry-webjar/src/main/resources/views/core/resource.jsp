<%--
  Created by IntelliJ IDEA.
  User: craneding
  Date: 15/9/29
  Time: 下午1:12
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("basePath", request.getContextPath());
%>
<meta charset="utf-8"/>
<%
    if (request.getAttribute("DEFAULT_TITLE") == null) {
%>
<title>云集 Api文档</title>
<%
    } else {
%>
<title>云集 Api文档</title>
<%
    }
%>
<meta name="author" content="云集">
<meta name="description" content="云集 Api文档，是一个云集内部的接口文档站点！">
<meta name="keywords" content="api,thrift,zookeeper,netty,redis,mysql">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<meta name="format-detection" content="telephone=no">
<meta name="format-detection" content="email=no">

<link rel="shortcut icon" href="${basePath}/images/yjtp_1.png" type="image/x-icon" />
<link rel="stylesheet" href="${basePath}/css/model/struct.css">
<!-- 新 Bootstrap 核心 CSS 文件 -->
<link rel="stylesheet" href="${basePath}/css/bootstrap/3.3.5/bootstrap.min.css">
<!-- 可选的Bootstrap主题文件（一般不用引bootstrap-theme.min.css入） -->
<link rel="stylesheet" href="${basePath}/css/bootstrap/3.3.5/bootstrap-theme.min.css">
<link rel="stylesheet" href="${basePath}/css/default.css" type="text/css"/>
<link rel="stylesheet" href="${basePath}/css/comment.css" type="text/css"/>
<link rel="stylesheet" href="${basePath}/css/model/scroll-top.css" type="text/css"/>
<!-- jQuery文件。务必在bootstrap.min.js 之前引入 -->
<script src="${basePath}/js/jquery/1.11.3/jquery.min.js"></script>
<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
<script src="${basePath}/js/bootstrap/3.3.5/bootstrap.min.js"></script>
<!-- marked -->
<script src="${basePath}/js/marked/0.3.5/marked.min.js"></script>
<script>
    window.basePath = "${basePath}";
</script>