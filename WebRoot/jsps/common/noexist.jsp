<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<%@ page language="java" contentType="text/html; charset=UTF-8" %>
	<head>
		<title>该页面不存在 | 星云网 XINGYUN.CN</title>
		<%@ include file="/jsps/common/PageCommon.jsp"%>
		<meta http-equiv="refresh" content="5;url=<%=com.xingyun.constant.XingyunLine.XINGYUN_CN%>">
	</head>
	<body>
		<div id="main" class="container" role="main">
   			<div id="single" class="mainBody">
				<section class="content" title="404error">
			        <div class="errorPage">
			        	<img width="164" height="163" src="${IMG_DOMAIN}/images/n.gif">
			        	<h1 class="nopageExist">该页面不存在...</h1>
			          	<p><span>5</span>秒钟后自动返回<a href="<c:url value="<%=com.xingyun.constant.XingyunLine.XINGYUN_CN%>"/> ">XINGYUN</a>首页</p>
			        </div>
			    </section>
			</div>
		</div>
		<c:import url="/jsps/common/bottom.jsp" />
	</body>
</html>