<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ include file="/jsps/common/jstl.jsp"%>
<c:if test="${param.tag != null && (param.tag == 'profile' || param.tag == 'post' || param.tag == 'recommend')}">
	<div class="bottomPersonInfo">
		<span class="name">${userHeaderBean.nickName}</span><span class="xyName">XINGYUN</span>人才个人网站<span>www.xingyun.cn${userHeaderBean.userHref}</span>
	</div>
</c:if>
<footer>
	<p class="xyLinks">
		<a href="${HTML_DOMAIN}/html/about/index.html">关于星云</a>|<a href="${HTML_DOMAIN}/html/about/contact.html">联络我们</a>|<a href="${HTML_DOMAIN}/html/about/joinus.html">加入我们</a>|<a href="${HTML_DOMAIN}/html/about/team.html">团队简介</a>|<a href="${HTML_DOMAIN}/html/about/wishes.html">明星祝福</a>|<a href="${HTML_DOMAIN}/html/about/invitation.html">星云邀请</a>|<a href="${HTML_DOMAIN}/html/about/help.html">星云帮助</a>|<a href="${HTML_DOMAIN}/html/about/protocol.html">服务条款</a>
	</p>
	<p>Copyright&copy;2012&nbsp;XINGYUN(星云网)Network&nbsp;Company&nbsp;All&nbsp;rights&nbsp;reserved.</p>
	<p>The&copy;XINGYUN&nbsp;brand&nbsp;and&nbsp;logo&nbsp;are&nbsp;trademarks&nbsp;of&nbsp;XINGYUN&nbsp;Co.</p>
</footer>
<c:set var="level_youke" value="<%=com.xingyun.constant.XingyunCommonConstant.USER_LEVEL_YOUKE %>"/>
<c:if test="${user != null && user.lid != level_youke && user.wkey == ''}">
	<script type="text/javascript">jQuery.xy.common.setNickNameAndWkeyPop('${user.nickName}')</script>
</c:if>
<script type="text/javascript">jQuery.scrollTop();</script>
<script src="http://hm.baidu.com/h.js?96cb8b2abc5c8ef98d1efcfa01e349a2"></script>