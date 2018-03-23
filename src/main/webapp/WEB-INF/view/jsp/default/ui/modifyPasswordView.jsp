<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
  <head>
	<title>设置密码</title>
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
  $('#setpswd').submit(function() {
    if ($('#password').val() != $('#passwordagain').val()) {
	  alert('两次输入的密码不一致，请重新输入密码');
	  $('#password').val('');
	  $('#passwordagain').val('');
	  $('#password').focus();
	  return false;
	}
	else {
	  return true;
	}
  });
  $('#password').focus();
});
	</script>
  </head>
  <body>
	<form id="setpswd" action="setpswd" method="POST">
	  <input type="hidden" name="a" value="<c:out value="${account}"/>"/>
	  <input type="hidden" name="c" value="<c:out value="${code}"/>"/>
	  <input type="hidden" name="s" value="<c:out value="${service}"/>"/>
	  <div id="edit">
		<div id="info">请输入新密码：</div>
		<div>登录账号：<c:out value="${account}"/></div>
		<div>输入密码：<input type="password" id="password" name="password" class="input"/></div>
		<div>确认密码：<input type="password" id="passwordagain" class="input"/></div>
		<div><input type="submit" value="确定" class="button"/></div>
	  </div>
	</form>
  </body>
</html>
