<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quên mật khẩu</title>
</head>
<body>

<div class="container">
    <div class="row">
        <div class="col-md-6 col-md-offset-3">

            <h2>Quên mật khẩu</h2>

            <c:if test="${not empty notice}">
                <div class="alert alert-info">
                    <c:out value="${notice}"/>
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <c:url value="/forgetpassword" var="resetUrl"/>
            <c:url value="/login" var="loginUrl"/>

            <h3>1. Nhận mã OTP</h3>

            <form action="${resetUrl}" method="post">

                <input type="hidden"
                       name="csrfToken"
                       value="<c:out value='${sessionScope.csrfToken}'/>">

                <input type="hidden" name="action" value="request">

                <div class="form-group">
                    <label for="email">Email đã đăng ký</label>
                    <input type="email"
                           id="email"
                           name="email"
                           class="form-control"
                           required
                           maxlength="100"
                           autocomplete="email"
                           value="<c:out value='${sessionScope.resetEmail}'/>">
                </div>

                <button type="submit" class="btn btn-default">
                    Gửi / gửi lại OTP
                </button>
            </form>

            <c:if test="${not empty sessionScope.resetEmail}">
                <hr>

                <h3>2. Đặt mật khẩu mới</h3>

                <p>
                    Dùng OTP gửi tới
                    <strong>
                        <c:out value="${sessionScope.resetEmail}"/>
                    </strong>.
                    Mã có hiệu lực trong 5 phút.
                </p>

                <form action="${resetUrl}" method="post">

                    <input type="hidden"
                           name="csrfToken"
                           value="<c:out value='${sessionScope.csrfToken}'/>">

                    <input type="hidden" name="action" value="reset">

                    <div class="form-group">
                        <label for="otp">OTP gồm 6 chữ số</label>
                        <input type="text"
                               id="otp"
                               name="otp"
                               class="form-control"
                               required
                               pattern="[0-9]{6}"
                               maxlength="6"
                               inputmode="numeric"
                               autocomplete="one-time-code">
                    </div>

                    <div class="form-group">
                        <label for="password">Mật khẩu mới</label>
                        <input type="password"
                               id="password"
                               name="password"
                               class="form-control"
                               required
                               minlength="3"
                               maxlength="128"
                               autocomplete="new-password">
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword">
                            Nhập lại mật khẩu mới
                        </label>

                        <input type="password"
                               id="confirmPassword"
                               name="confirmPassword"
                               class="form-control"
                               required
                               minlength="3"
                               maxlength="128"
                               autocomplete="new-password">
                    </div>

                    <button type="submit" class="btn btn-primary">
                        Xác nhận và đổi mật khẩu
                    </button>
                </form>
            </c:if>

            <hr>

            <a href="${loginUrl}">Quay lại đăng nhập</a>

        </div>
    </div>
</div>

</body>
</html>