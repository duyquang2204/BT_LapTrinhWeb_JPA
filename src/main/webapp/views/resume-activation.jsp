<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tiếp tục kích hoạt tài khoản</title>
</head>
<body>

<div class="container">
    <div class="row">
        <div class="col-md-6 col-md-offset-3">

            <h2>Tiếp tục kích hoạt tài khoản</h2>

            <p>
                Nếu đã đăng ký nhưng chưa xác nhận email,
                hãy nhập thông tin tài khoản để tiếp tục.
            </p>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <c:url value="/activate/resume" var="resumeUrl"/>
            <c:url value="/login" var="loginUrl"/>

            <form action="${resumeUrl}" method="post">

                <input type="hidden"
                       name="csrfToken"
                       value="<c:out value='${sessionScope.csrfToken}'/>">

                <div class="form-group">
                    <label for="username">Tên đăng nhập</label>

                    <input type="text"
                           id="username"
                           name="username"
                           class="form-control"
                           required
                           maxlength="30"
                           autocomplete="username"
                           value="<c:out value='${username}'/>">
                </div>

                <div class="form-group">
                    <label for="password">Mật khẩu đã đăng ký</label>

                    <input type="password"
                           id="password"
                           name="password"
                           class="form-control"
                           required
                           maxlength="128"
                           autocomplete="current-password">
                </div>

                <button type="submit" class="btn btn-primary">
                    Tiếp tục
                </button>

                <a href="${loginUrl}" class="btn btn-default">
                    Về đăng nhập
                </a>
            </form>

        </div>
    </div>
</div>

</body>
</html>