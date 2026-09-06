<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Kích hoạt tài khoản</title>
</head>
<body>

<div class="container">
    <div class="row">
        <div class="col-md-6 col-md-offset-3">
            <h2>Kích hoạt tài khoản</h2>

            <p>
                Nhập mã 6 chữ số được gửi tới email đăng ký.
                Mã có hiệu lực trong 5 phút.
            </p>

            <c:if test="${not empty notice}">
                <div class="alert alert-success">
                    <c:out value="${notice}"/>
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <c:url value="/activate" var="activateUrl"/>

            <form action="${activateUrl}" method="post">
                <input type="hidden"
                       name="csrfToken"
                       value="<c:out value='${sessionScope.csrfToken}'/>">

                <input type="hidden" name="action" value="verify">

                <div class="form-group">
                    <label for="otp">Mã OTP</label>
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

                <button type="submit" class="btn btn-primary">
                    Xác nhận
                </button>
            </form>

            <hr>

            <p>
                Chưa nhận được thư? Kiểm tra thư rác.
                Gửi lại phải cách lần trước ít nhất 60 giây.
            </p>

            <form action="${activateUrl}" method="post">
                <input type="hidden"
                       name="csrfToken"
                       value="<c:out value='${sessionScope.csrfToken}'/>">

                <input type="hidden" name="action" value="resend">

                <button type="submit" class="btn btn-default">
                    Gửi lại OTP
                </button>
            </form>
        </div>
    </div>
</div>

</body>
</html>