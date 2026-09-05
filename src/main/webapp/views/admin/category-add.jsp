<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thêm danh mục</title>
</head>
<body>

<h2>Thêm danh mục</h2>

<c:if test="${not empty error}">
    <div class="alert alert-danger">
        <c:out value="${error}"/>
    </div>
</c:if>

<c:url value="/admin/category/insert" var="saveUrl"/>
<c:url value="/admin/categories" var="listUrl"/>

<form action="${saveUrl}"
      method="post"
      enctype="multipart/form-data">

    <input type="hidden"
           name="csrfToken"
           value="<c:out value='${sessionScope.csrfToken}'/>">

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

        <input type="file"
               id="images"
               name="images"
               accept="image/jpeg,image/png"
               onchange="chooseFile(this)">

        <p class="help-block">
            JPG hoặc PNG, tối đa 5 MB và 16 triệu điểm ảnh.
            Có thể để trống.
        </p>

        <img id="imagess"
             alt="Ảnh xem trước"
             width="160"
             height="120"
             style="object-fit: contain;">
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
        Lưu danh mục
    </button>

    <a href="${listUrl}" class="btn btn-default">
        Quay lại
    </a>
</form>

</body>
</html>