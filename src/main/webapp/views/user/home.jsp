<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Trang chủ</title>
</head>
<body>

<div class="container">
    <h2>Sản phẩm mới nhất</h2>

    <jsp:include page="/views/user/fragments/product-cards.jsp"/>

    <c:url value="/product" var="productsUrl"/>

    <p class="text-center">
        <a href="${productsUrl}" class="btn btn-default">
            Xem tất cả sản phẩm
        </a>
    </p>
</div>

</body>
</html>