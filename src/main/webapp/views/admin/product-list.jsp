<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý sản phẩm</title>
</head>
<body>

<fmt:setLocale value="vi_VN"/>

<h2>Quản lý sản phẩm</h2>

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

<c:url value="/admin/product/add" var="addUrl"/>
<c:url value="/admin/product/delete" var="deleteUrl"/>

<p>
    <a href="${addUrl}" class="btn btn-primary">
        Thêm sản phẩm
    </a>
</p>

<div class="table-responsive">
    <table class="table table-bordered table-striped">
        <thead>
            <tr>
                <th>ID</th>
                <th>Ảnh</th>
                <th>Tên sản phẩm</th>
                <th>Danh mục</th>
                <th>Giá</th>
                <th>Số lượng</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
        </thead>

        <tbody>
            <c:choose>
                <c:when test="${empty products}">
                    <tr>
                        <td colspan="8" class="text-center">
                            Chưa có sản phẩm.
                        </td>
                    </tr>
                </c:when>

                <c:otherwise>
                    <c:forEach items="${products}" var="product">
                        <tr>
                            <td>${product.id}</td>

                            <td>
                                <c:choose>
                                    <c:when test="${empty product.images}">
                                        Chưa có ảnh
                                    </c:when>

                                    <c:otherwise>
                                        <c:url value="/image" var="imageUrl">
                                            <c:param name="fname"
                                                     value="${product.images}"/>
                                        </c:url>

                                        <img src="<c:out value='${imageUrl}'/>"
                                             alt="Ảnh sản phẩm"
                                             width="100"
                                             height="80"
                                             style="object-fit: cover;">
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <c:out value="${product.name}"/>
                            </td>

                            <td>
                                <c:out value="${product.category.categoryname}"/>
                            </td>

                            <td>
                                <fmt:formatNumber value="${product.price}"
                                                  maxFractionDigits="2"/>
                                đ
                            </td>

                            <td>${product.quantity}</td>

                            <td>
                                <c:choose>
                                    <c:when test="${product.active == 1}">
                                        <span class="label label-success">
                                            Hiển thị
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="label label-default">
                                            Ẩn
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <c:url value="/admin/product/edit" var="editUrl">
                                    <c:param name="id" value="${product.id}"/>
                                </c:url>

                                <a href="<c:out value='${editUrl}'/>"
                                   class="btn btn-warning btn-sm">
                                    Sửa
                                </a>

                                <form action="${deleteUrl}"
                                      method="post"
                                      style="display: inline-block;"
                                      onsubmit="return confirm('Bạn có chắc muốn xóa sản phẩm này?');">

                                    <input type="hidden"
                                           name="id"
                                           value="${product.id}">

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