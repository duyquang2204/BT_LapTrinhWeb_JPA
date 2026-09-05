<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>

<form action="${pageContext.request.contextPath}/admin/category/update" method="post" enctype="multipart/form-data">
	<input type="hidden" id="categoryid" name="categoryid" value="${cate.categoryid}"><br>
	
	<label for="categoryname">Category Name:</label><br>
	<input type="text" id="categoryname" name="categoryname" value="${cate.categoryname}"><br>
	
	<label for="images">Images:</label><br>
	<c:choose>
		<c:when test="${not empty cate.images && fn:startsWith(cate.images, 'https')}">
			<c:url value="${cate.images}" var="imgUrl"></c:url>
		</c:when>
		<c:otherwise>
			<c:url value="/image?fname=${cate.images}" var="imgUrl"></c:url>
		</c:otherwise>
	</c:choose>
	
	<img id="imagess" height="150" width="200" src="${imgUrl}" /><br>
	<input type="file" id="images" name="images"><br>
	
	<label>Status:</label><br>
	<input type="radio" id="status1" name="status" value="1" ${cate.status == 1 ? "checked" : ""}>
	<label for="status1">Đang hoạt động</label><br>

	<input type="radio" id="status0" name="status" value="0" ${cate.status == 0 ? "checked" : ""}>
	<label for="status0">Khóa</label><br>

	<br>
	<input type="submit" value="Update">
</form>