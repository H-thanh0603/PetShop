<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
{
<c:forEach var="user" items="${users}" varStatus="status">
  "${user.id}": {
    "id": "${user.id}",
    "username": "<c:out value='${user.username}'/>",
    "fullname": "<c:out value='${user.fullname}'/>",
    "email": "<c:out value='${user.email}'/>",
    "phone": "<c:out value='${user.phone}'/>",
    "address": "<c:out value='${user.address}'/>",
    "role": "${user.role}",
    "status": "${user.status != null ? user.status : 'active'}",
    "petCount": ${user.petCount},
    "appointmentCount": ${user.appointmentCount},
    "createdAt": "<fmt:formatDate value='${user.createdAt}' pattern='dd/MM/yyyy HH:mm'/>"
  }<c:if test="${!status.last}">,</c:if>
</c:forEach>
}
