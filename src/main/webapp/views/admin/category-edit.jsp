<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Sửa danh mục</title>
</head>
<body>

<h2>Sửa danh mục</h2>

<c:if test="${not empty error}">
    <div class="alert alert-danger">
        <c:out value="${error}"/>
    </div>
</c:if>

<c:url value="/admin/category/update" var="saveUrl"/>
<c:url value="/admin/categories" var="listUrl"/>

<form action="${saveUrl}"
      method="post"
      enctype="multipart/form-data">

    <input type="hidden"
           name="csrfToken"
           value="<c:out value='${sessionScope.csrfToken}'/>">

    <input type="hidden"
           name="categoryid"
           value="<c:out value='${cate.categoryid}'/>">

    <div class="form-group">
        <label>Mã danh mục</label>
        <p class="form-control-static">
            <c:out value="${cate.categoryid}"/>
        </p>
    </div>

    <div class="form-group">
        <label for="categoryname">Tên danh mục</label>

        <input type="text"
               class="form-control"
               id="categoryname"
               name="categoryname"
               maxlength="200"
               required
               value="<c:out value='${cate.categoryname}'/>">
    </div>

    <div class="form-group">
        <label for="images">Ảnh danh mục</label>

        <c:choose>
            <c:when test="${not empty cate.images}">
                <c:choose>
                    <c:when test="${fn:startsWith(cate.images, 'https://')
                                  or fn:startsWith(cate.images, 'http://')}">
                        <c:set var="imgUrl" value="${cate.images}"/>
                    </c:when>

                    <c:otherwise>
                        <c:url value="/image" var="imgUrl">
                            <c:param name="fname" value="${cate.images}"/>
                        </c:url>
                    </c:otherwise>
                </c:choose>

                <p>
                    <img id="imagess"
                         src="<c:out value='${imgUrl}'/>"
                         alt="Ảnh danh mục"
                         width="160"
                         height="120"
                         style="object-fit: contain;">
                </p>
            </c:when>

            <c:otherwise>
                <p>Danh mục chưa có ảnh.</p>

                <img id="imagess"
                     alt="Ảnh xem trước"
                     width="160"
                     height="120"
                     style="object-fit: contain;">
            </c:otherwise>
        </c:choose>

        <input type="file"
               id="images"
               name="images"
               accept="image/jpeg,image/png"
               onchange="chooseFile(this)">

        <p class="help-block">
            Không chọn ảnh mới thì giữ ảnh cũ.
            JPG hoặc PNG, tối đa 5 MB và 16 triệu điểm ảnh.
        </p>
    </div>

    <div class="form-group">
        <label>Trạng thái</label>

        <div class="radio">
            <label>
                <input type="radio"
                       name="status"
                       value="1"
                       required
                       ${cate.status == 1 ? 'checked' : ''}>
                Hoạt động
            </label>
        </div>

        <div class="radio">
            <label>
                <input type="radio"
                       name="status"
                       value="0"
                       ${cate.status == 0 ? 'checked' : ''}>
                Khóa
            </label>
        </div>
    </div>

    <button type="submit" class="btn btn-primary">
        Cập nhật
    </button>

    <a href="${listUrl}" class="btn btn-default">
        Quay lại
    </a>
</form>

</body>
</html>