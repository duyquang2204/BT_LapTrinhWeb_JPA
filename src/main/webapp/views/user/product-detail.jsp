<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title><c:out value="${product.name}"/></title>
</head>
<body>

<fmt:setLocale value="vi_VN"/>

<div class="container">
    <c:url value="/product" var="listUrl"/>

    <p>
        <a href="${listUrl}">← Danh sách sản phẩm</a>
    </p>

    <div class="row">
        <div class="col-md-5">
            <c:choose>
                <c:when test="${not empty product.images}">
                    <c:url value="/image" var="imageUrl">
                        <c:param name="fname" value="${product.images}"/>
                    </c:url>

                    <img src="<c:out value='${imageUrl}'/>"
                         alt="<c:out value='${product.name}'/>"
                         class="img-responsive img-thumbnail"
                         style="width: 100%; max-height: 450px; object-fit: contain;">
                </c:when>

                <c:otherwise>
                    <div class="well text-center">
                        Sản phẩm chưa có ảnh.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="col-md-7">
            <h2><c:out value="${product.name}"/></h2>

            <p>
                Danh mục:
                <strong>
                    <c:out value="${product.category.categoryname}"/>
                </strong>
            </p>

            <h3 class="text-danger">
                <fmt:formatNumber value="${product.price}"
                                  maxFractionDigits="2"/>
                đ
            </h3>

            <c:choose>
                <c:when test="${product.quantity > 0}">
                    <p>Còn ${product.quantity} sản phẩm.</p>
                </c:when>
                <c:otherwise>
                    <p class="text-danger">Hết hàng.</p>
                </c:otherwise>
            </c:choose>

            <hr>

            <h3>Mô tả</h3>

            <c:choose>
                <c:when test="${not empty product.description}">
                    <div style="white-space: pre-wrap; overflow-wrap: anywhere;"><c:out value="${product.description}"/></div>
                </c:when>

                <c:otherwise>
                    <p>Chưa có mô tả.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

</body>
</html>