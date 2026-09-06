<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đăng ký tài khoản</title>
</head>
<body>

<div class="container">
    <div class="row">
        <div class="col-md-6 col-md-offset-3">
            <h2>Đăng ký tài khoản</h2>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <c:url value="/register" var="registerUrl"/>
            <c:url value="/login" var="loginUrl"/>

            <form action="${registerUrl}" method="post">
                <input type="hidden"
                       name="csrfToken"
                       value="<c:out value='${sessionScope.csrfToken}'/>">

                <div class="form-group">
                    <label for="username">Tên đăng nhập</label>
                    <input id="username"
                           name="username"
                           class="form-control"
                           required
                           minlength="3"
                           maxlength="30"
                           pattern="[A-Za-z0-9_]{3,30}"
                           autocomplete="username"
                           value="<c:out value='${username}'/>">
                    <p class="help-block">
                        3–30 ký tự: chữ không dấu, số hoặc dấu gạch dưới.
                    </p>
                </div>

                <div class="form-group">
                    <label for="fullname">Họ tên</label>
                    <input id="fullname"
                           name="fullname"
                           class="form-control"
                           required
                           maxlength="100"
                           autocomplete="name"
                           value="<c:out value='${fullname}'/>">
                </div>

                <div class="form-group">
                    <label for="email">Email nhận OTP</label>
                    <input type="email"
                           id="email"
                           name="email"
                           class="form-control"
                           required
                           maxlength="100"
                           autocomplete="email"
                           value="<c:out value='${email}'/>">
                </div>

                <div class="form-group">
                    <label for="password">Mật khẩu</label>
                    <input type="password"
                           id="password"
                           name="password"
                           class="form-control"
                           required
                           minlength="3"
                           maxlength="128"
                           autocomplete="new-password">
                    <p class="help-block">Từ 3 đến 128 ký tự.</p>
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Nhập lại mật khẩu</label>
                    <input type="password"
                           id="confirmPassword"
                           name="confirmPassword"
                           class="form-control"
                           required
                           minlength="3"
                           maxlength="128"
                           autocomplete="new-password">
                </div>
				<p>
    			
                <button type="submit" class="btn btn-primary">
                    Đăng ký và nhận OTP
                </button>

                <a href="${loginUrl}" class="btn btn-default">
                    Đăng nhập
                </a>
            </form>
            <a href="${pageContext.request.contextPath}/activate/resume">Đã đăng ký nhưng chưa kích hoạt tài khoản?</a></p>
        </div>
    </div>
</div>

</body>
</html>