<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
   String service = request.getParameter("service");
   if (service != null && service.startsWith("http://crm.gionee.com")) {
%>
<c:import url="crm.jsp"/>
<%
   }
   else if (service != null && service.startsWith("http://eoa.gionee.com")) {
%>
<c:import url="eoa.jsp"/>
<%
   }
   else if (service != null && service.startsWith("http://xst.gionee.com")) {
%>
<c:import url="xst.jsp"/>
<%
   }
   else if (service != null && service.startsWith("http://os.gionee.com")) {
%>
<c:import url="amg.jsp"/>
<%
   }
   else if (service != null && service.startsWith("http://assp.gionee.com")) {
%>
<c:import url="assp.jsp"/>
<%
   }
   else if (service != null && (service.startsWith("http://mes.gionee.com")
     ||service.startsWith("http://mes2.gionee.com")
     ||service.startsWith("http://mes1.gionee.com"))) {
%>
<c:import url="mes.jsp"/>
<%
   }
   else if (service != null && (service.startsWith("http://mewms.gionee.com")
     ||service.startsWith("http://mewms2.gionee.com"))) {
%>
<c:import url="mewms.jsp"/>
<%
   }
   else {
%>
<c:import url="default.jsp"/>
<%
   }
%>
</html>

