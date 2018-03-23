<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
  <head>
	<title>忘记密码</title>
	<meta charset="utf-8"/>
	<style type="text/css">
	  body{width:700px;margin:auto;font-size:14px;}
	  #edit{width:350px;margin:40px auto;}
	  #edit div{margin:10px;}
	  .input{width:200px;}
	  .button{margin-left:150px;}
	</style>
	<script type="text/javascript" src="javascript/jquery-1.10.2.js"></script>
	<script type="text/javascript">
$(function() {
  $('#getpswd').submit(function() {
	if ($('#account').val() == '') {
	  $('#info').text('请输入账号');
	  $('#info').css('color', 'red');
	  $('#account').focus();
	  return false;
	}
	else if ($('#email').val() == '') {
	  $('#info').text('请输入邮箱');
	  $('#info').css('color', 'red');
	  $('#email').focus();
	  return false;
	}
	return true;
  });
  $('#account').focus();
  <c:if test="${errors != null}">
  $('#info').text('<c:out value="${errors}"/>');
  $('#info').css('color', 'red');
  </c:if>
});
	</script>
  </head>
  <body>
	<form id="getpswd" action="getpswd" method="POST">
	<input type="hidden" name="service" value="<c:out value="${param.s}"/>"/>
	  <div id="edit">
		<div id="info">请输入以下信息，然后通过邮箱获取密码：</div>
		<div>登录账号：<input type="text" id="account" name="account" class="input"/></div>
		<div>电子邮箱：<input type="text" id="email" name="email" class="input"/></div>
		<div><input type="submit" value="确定" class="button"/> <a href="login?service=<c:out value="${param.s}"/>" style="color:blue;">返回登录页</a></div>
	  </div>
	</form>
  </body>
</html>
