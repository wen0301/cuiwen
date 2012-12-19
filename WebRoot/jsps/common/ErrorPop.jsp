<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ include file="/jsps/common/jstl.jsp"%>
<div class="dialogBox3">
	<a href="javascript:void(0);" class="closeButton iconClose1" title="关闭" onclick="jQuery.xyDialog.close()"></a>
	<h2 class="dialogTitle">请求失败</h2>
	<div class="dialog promptError">
		<span class="icon iconPromptError"></span><br>
		<span class="info">数据请求失败，请稍后再试。*^_^*</span>
	</div>
	<div class="dialogButton">
		<button class="mainButton" type="button" onclick="jQuery.xyDialog.close()">确定</button>
	</div>
</div>