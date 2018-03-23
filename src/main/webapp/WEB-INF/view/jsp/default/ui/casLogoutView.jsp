<%
if (request.getParameter("redirect") != null) {
	response.sendRedirect("http://" + request.getParameter("redirect"));
}
else {
    response.sendRedirect("login");
}
%>
