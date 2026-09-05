<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đăng xuất</title>
</head>
<body>

<div class="container">
    <h2>Đăng xuất</h2>
    <p>Bạn muốn đăng xuất khỏi tài khoản hiện tại?</p>

    <c:url value="/logout" var="logoutUrl"/>
    <c:url value="/user/home" var="homeUrl"/>

    <form action="${logoutUrl}" method="post">
        <input type="hidden"
               name="csrfToken"
               value="<c:out value='${sessionScope.csrfToken}'/>">

        <button type="submit" class="btn btn-primary">
            Đăng xuất
        </button>

        <a href="${homeUrl}" class="btn btn-default">
            Quay lại
        </a>
    </form>
</div>

</body>
</html>