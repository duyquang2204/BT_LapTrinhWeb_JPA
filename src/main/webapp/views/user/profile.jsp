<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thông tin cá nhân</title>
</head>
<body>

<div class="container">
    <h2>Thông tin cá nhân</h2>

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

    <c:url value="/user/profile" var="saveUrl"/>
    <c:url value="/user/home" var="homeUrl"/>

    <form action="${saveUrl}"
          method="post"
          enctype="multipart/form-data"
          class="col-md-6">

        <input type="hidden"
               name="csrfToken"
               value="<c:out value='${sessionScope.csrfToken}'/>">

        <div class="form-group">
            <label>Tên đăng nhập</label>
            <p class="form-control-static">
                <c:out value="${profile.username}"/>
            </p>
        </div>

        <div class="form-group">
            <label>Email</label>
            <p class="form-control-static">
                <c:out value="${profile.email}"/>
            </p>
        </div>

        <div class="form-group">
            <label for="fullname">Họ tên</label>

            <input type="text"
                   class="form-control"
                   id="fullname"
                   name="fullname"
                   required
                   maxlength="100"
                   autocomplete="name"
                   value="<c:out value='${profile.fullname}'/>">
        </div>

        <div class="form-group">
            <label for="phone">Số điện thoại</label>

            <input type="tel"
                   class="form-control"
                   id="phone"
                   name="phone"
                   required
                   maxlength="10"
                   pattern="0[0-9]{9}"
                   title="Gồm 10 chữ số, bắt đầu bằng 0"
                   autocomplete="tel"
                   value="<c:out value='${profile.phone}'/>">
        </div>

        <div class="form-group">
            <label for="images">Ảnh đại diện</label>

            <c:choose>
                <c:when test="${not empty profile.images}">
                    <c:choose>
                        <c:when test="${fn:startsWith(profile.images, 'https://')
                                      or fn:startsWith(profile.images, 'http://')}">
                            <c:set var="avatarUrl"
                                   value="${profile.images}"/>
                        </c:when>

                        <c:otherwise>
                            <c:url value="/image" var="avatarUrl">
                                <c:param name="fname"
                                         value="${profile.images}"/>
                            </c:url>
                        </c:otherwise>
                    </c:choose>

                    <p>
                        <img id="profilePreview"
                             src="<c:out value='${avatarUrl}'/>"
                             alt="Ảnh đại diện"
                             width="160"
                             height="160"
                             class="img-thumbnail"
                             style="object-fit: cover;">
                    </p>
                </c:when>

                <c:otherwise>
                    <p>Bạn chưa có ảnh đại diện.</p>

                    <img id="profilePreview"
                         alt="Ảnh xem trước"
                         width="160"
                         height="160"
                         class="img-thumbnail"
                         style="display: none; object-fit: cover;">
                </c:otherwise>
            </c:choose>

            <input type="file"
                   id="images"
                   name="images"
                   accept="image/jpeg,image/png"
                   onchange="previewProfileImage(this)">

            <p class="help-block">
                JPG hoặc PNG, tối đa 5 MB và 16 triệu điểm ảnh.
                Không chọn ảnh mới thì giữ ảnh cũ.
            </p>
        </div>

        <button type="submit" class="btn btn-primary">
            Lưu thay đổi
        </button>

        <a href="${homeUrl}" class="btn btn-default">
            Về trang chủ
        </a>
    </form>
</div>

<script>
    function previewProfileImage(input) {
        const file = input.files && input.files[0];
        const preview = document.getElementById("profilePreview");

        if (!file || !preview) {
            return;
        }

        const reader = new FileReader();

        reader.onload = function (event) {
            preview.src = event.target.result;
            preview.style.display = "block";
        };

        reader.readAsDataURL(file);
    }
</script>

</body>
</html>