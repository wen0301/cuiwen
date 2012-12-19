<!doctype html>
<!--[if lt IE 7]><html class="no-js ie lt-ie9 lt-ie8 lt-ie7" lang="zh-CN"><![endif]-->
<!--[if lt IE 8]><html class="no-js ie lt-ie9 lt-ie8" lang="zh-CN"><![endif]-->
<!--[if lt IE 9]><html class="no-js ie lt-ie9" lang="zh-CN"><![endif]-->
<!--[if IE 9]><html class="no-js ie" lang="zh-CN"><![endif]-->
<!--[if gt IE 9]><!--><html class="no-js" lang="zh-CN"><!--<![endif]-->
<head>
	<%@ page language="java" contentType="text/html; charset=UTF-8"%>
	<%@ taglib prefix="xingyun" uri="/WEB-INF/xingyun.tld"%>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
	<meta name="viewport" content="width=device-width, user-scalable=yes">
	<meta http-equiv="windows-Target" contect="_top">
	<meta name="description" content="" />
	<meta name="keywords" content="" />
	<meta name="author" content="" />
	<meta name="Robots" content="all">
	<title>崔文的个人网页</title>
	<%@ include file="/jsps/common/jstl.jsp"%>
	<link rel="stylesheet" href="<%=com.xingyun.constant.XingyunNginx.getCss("index.css")%>" media="screen">
	<script type="text/javascript" src="<%=com.xingyun.constant.XingyunNginx.getScript("jquery-1.8.0.min.js") %>"></script>
</head>
<body>
	<div class="header"><span>梦想屠宰场</span><span>IE6催命</span></div>
	<div class="content">
	    <p><a href="http://www.ie6countdown.com/" target="_blank" class="countDown"><img src="/images/countdown.jpg" /></a></p>
	    <div class="sample">
	        <span>
	            <a class="btn" href="javascript:void(0);" >样式一：Person like this guy who still use IE6! U are one of them</a>
	            <img class="fuckIE6" src="/images/sunhao.jpg" >
	        </span>
	        <span>
	            <a class="btn" href="javascript:void(0);">样式二：我代表程序员联盟问候你的祖宗十八代</a>
	            <img class="fuckIE6" src="/images/fuck.jpg" >
	        </span>
	        <span>
	            <a class="btn" href="javascript:void(0);">样式三：不用IE6你会死啊你会死啊！</a>
	            <img class="fuckIE6" src="/images/angryman.png" >
	        </span>
	    </div>
	</div>
</body>
</html>
