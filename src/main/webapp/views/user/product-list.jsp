<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Sản phẩm</title>
</head>
<body>

<div class="container">
    <h2>Sản phẩm</h2>

    <p>
        Có <c:out value="${totalProducts}"/> sản phẩm.
        Trang ${currentPage}/${totalPages}.
    </p>

    <jsp:include page="/views/user/fragments/product-cards.jsp"/>

    <c:if test="${totalPages > 1}">
        <nav aria-label="Phân trang sản phẩm">
            <ul class="pagination">

                <c:if test="${currentPage > 1}">
                    <c:url value="/product" var="previousUrl">
                        <c:param name="page" value="${currentPage - 1}"/>
                    </c:url>

                    <li>
                        <a href="<c:out value='${previousUrl}'/>">
                            Trước
                        </a>
                    </li>
                </c:if>

                <c:forEach begin="${startPage}"
                           end="${endPage}"
                           var="pageNumber">

                    <c:url value="/product" var="pageUrl">
                        <c:param name="page" value="${pageNumber}"/>
                    </c:url>

                    <li class="${pageNumber == currentPage ? 'active' : ''}">
                        <a href="<c:out value='${pageUrl}'/>">
                            ${pageNumber}
                        </a>
                    </li>
                </c:forEach>

                <c:if test="${currentPage < totalPages}">
                    <c:url value="/product" var="nextUrl">
                        <c:param name="page" value="${currentPage + 1}"/>
                    </c:url>

                    <li>
                        <a href="<c:out value='${nextUrl}'/>">
                            Sau
                        </a>
                    </li>
                </c:if>

            </ul>
        </nav>
    </c:if>
</div>

</body>
</html>