<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đăng nhập</title>
</head>
<body>

<div class="container">
    <div class="row">
        <div class="col-md-6 col-md-offset-3">

            <h2>Đăng nhập</h2>

            <c:if test="${not empty alert}">
                <div class="alert alert-danger" role="alert">
                    <c:out value="${alert}"/>
                </div>
            </c:if>

            <c:url value="/login" var="loginUrl"/>
            <c:url value="/user/home" var="homeUrl"/>

            <form action="${loginUrl}" method="post">

                <input type="hidden"
                       name="loginToken"
                       value="<c:out value='${sessionScope.loginToken}'/>">

                <div class="form-group">
                    <label for="username">Tên đăng nhập</label>

                    <input type="text"
                           id="username"
                           name="username"
                           class="form-control"
                           required
                           autocomplete="username"
                           value="<c:out value='${username}'/>">
                </div>

                <div class="form-group">
                    <label for="password">Mật khẩu</label>

                    <input type="password"
                           id="password"
                           name="password"
                           class="form-control"
                           required
                           autocomplete="current-password">
                </div>

                <button type="submit" class="btn btn-primary">
                    Đăng nhập
                </button>

                <a href="${homeUrl}" class="btn btn-default">
                    Về trang chủ
                </a>

            </form>

        </div>
    </div>
</div>

</body>
</html>