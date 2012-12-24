<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ page import="com.livegameengine.model.GameType" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Live Game Engine</title>
</head>

<body>


<h1>Live Game Engine</h1>

<h2>Game Types</h2>

<form action="type/create" method="POST">
<input type="submit" value="Create Type" />
</form>

<ul>
	<c:forEach var="type" items="${types}">
		<li>
			<a href="">${type.typeName}</a>
			<form action="type/-/<%= KeyFactory.keyToString(((GameType)pageContext.getAttribute("type")).getKey()) %>/create" method="POST">
				<input type="submit" value="Create Game" />
			</form>
		</li>
	</c:forEach>
</ul>

<h2>Games</h2>


<footer>
	<c:choose>
		<c:when test="${authenticated}">
			<a href="${logout_url}">logout</a>
		</c:when>
		<c:otherwise>
			<a href="${login_url}">login</a>
		</c:otherwise>
	</c:choose>
</footer>


</body>
</html>
