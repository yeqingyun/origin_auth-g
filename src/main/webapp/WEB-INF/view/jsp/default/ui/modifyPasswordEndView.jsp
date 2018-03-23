<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
  <head>
	<title>完成修改密码</title>
	<meta charset="utf-8"/>
	<style type="text/css">
	  body{width:600px;margin:auto;font-size:14px;}
	  #info{margin:40px auto;}
	</style>
	<script type="text/javascript" src="javascript/jquery-1.10.2.js"></script>
	<script type="text/javascript">
$(function() {
  var time = 5;
  function decTime() {
	time--;
	if (time <= 0) {
	  window.location.href='<c:out value="${service}"/>';
	}
	else {
	  $('#timer').text(time);
	  setTimeout(decTime, 1000);
	}
  }
  setTimeout(decTime, 1000);
});
	</script>
  </head>
  <body>
    <div id="info">
	  <p>您已经成功修改密码，系统将于 <span id="timer">5</span> 秒钟后返回登录页。</p>
	  <p>您也可以直接点击<a href="<c:out value="${service}"/>" style="color:blue;">返回登录页</a></p>
	</div>
  </body>
</html>
