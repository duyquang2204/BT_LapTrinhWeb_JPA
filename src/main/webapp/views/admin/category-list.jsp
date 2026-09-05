<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý danh mục</title>
</head>
<body>

<h2>Quản lý danh mục</h2>

<c:if test="${not empty success}">
    <div class="alert alert-success">
        <c:out value="${success}"/>
    </div>
</c:if>

<c:if test="${not empty error}">
    <div class="alert alert-danger">
        <c:out value="${error}"/>
    </div>
</c:if>

<c:url value="/admin/category/add" var="addUrl"/>
<c:url value="/admin/category/search" var="searchUrl"/>
<c:url value="/admin/categories" var="listUrl"/>
<c:url value="/admin/category/delete" var="deleteUrl"/>

<p>
    <a href="${addUrl}" class="btn btn-primary">
        Thêm danh mục
    </a>
</p>

<form action="${searchUrl}"
      method="get"
      class="form-inline">

    <div class="form-group">
        <label for="keyword">Tên danh mục</label>

        <input type="text"
               class="form-control"
               id="keyword"
               name="keyword"
               value="<c:out value='${keyword}'/>"
               placeholder="Nhập tên cần tìm">
    </div>

    <button type="submit" class="btn btn-default">
        Tìm kiếm
    </button>

    <a href="${listUrl}" class="btn btn-default">
        Xem tất cả
    </a>
</form>

<br>

<div class="table-responsive">
    <table class="table table-bordered table-striped">
        <thead>
            <tr>
                <th>STT</th>
                <th>Hình ảnh</th>
                <th>Mã danh mục</th>
                <th>Tên danh mục</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
        </thead>

        <tbody>
            <c:choose>
                <c:when test="${empty listcate}">
                    <tr>
                        <td colspan="6" class="text-center">
                            Không có danh mục phù hợp.
                        </td>
                    </tr>
                </c:when>

                <c:otherwise>
                    <c:forEach items="${listcate}"
                               var="cate"
                               varStatus="stt">

                        <tr>
                            <td>${stt.count}</td>

                            <td>
                                <c:choose>
                                    <c:when test="${empty cate.images}">
                                        <span>Chưa có ảnh</span>
                                    </c:when>

                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${fn:startsWith(cate.images, 'https://')
                                                          or fn:startsWith(cate.images, 'http://')}">
                                                <c:set var="imgUrl"
                                                       value="${cate.images}"/>
                                            </c:when>

                                            <c:otherwise>
                                                <c:url value="/image"
                                                       var="imgUrl">
                                                    <c:param name="fname"
                                                             value="${cate.images}"/>
                                                </c:url>
                                            </c:otherwise>
                                        </c:choose>

                                        <img src="<c:out value='${imgUrl}'/>"
                                             alt="Ảnh danh mục"
                                             width="140"
                                             height="100"
                                             class="img-thumbnail"
                                             style="object-fit: cover;">
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <c:out value="${cate.categoryid}"/>
                            </td>

                            <td>
                                <c:out value="${cate.categoryname}"/>
                            </td>

                            <td>
                                <c:choose>
                                    <c:when test="${cate.status == 1}">
                                        <span class="label label-success">
                                            Hoạt động
                                        </span>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="label label-default">
                                            Khóa
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <c:url value="/admin/category/edit"
                                       var="editUrl">
                                    <c:param name="id"
                                             value="${cate.categoryid}"/>
                                </c:url>

                                <a href="<c:out value='${editUrl}'/>"
                                   class="btn btn-warning btn-sm">
                                    Sửa
                                </a>

                                <form action="${deleteUrl}"
                                      method="post"
                                      style="display: inline-block;"
                                      onsubmit="return confirm('Bạn có chắc muốn xóa danh mục này?');">

                                    <input type="hidden"
                                           name="id"
                                           value="<c:out value='${cate.categoryid}'/>">

                                    <input type="hidden"
                                           name="csrfToken"
                                           value="<c:out value='${sessionScope.csrfToken}'/>">

                                    <button type="submit"
                                            class="btn btn-danger btn-sm">
                                        Xóa
                                    </button>
                                </form>
                            </td>
                        </tr>

                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

</body>
</html>