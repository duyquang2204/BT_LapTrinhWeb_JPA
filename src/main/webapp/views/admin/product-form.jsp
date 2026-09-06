<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${editing ? 'Sửa sản phẩm' : 'Thêm sản phẩm'}</title>
</head>
<body>

<h2>${editing ? 'Sửa sản phẩm' : 'Thêm sản phẩm'}</h2>

<c:if test="${not empty error}">
    <div class="alert alert-danger">
        <c:out value="${error}"/>
    </div>
</c:if>

<c:if test="${empty categories}">
    <div class="alert alert-warning">
        Bạn cần tạo danh mục trước khi thêm sản phẩm.
    </div>
</c:if>

<c:choose>
    <c:when test="${editing}">
        <c:url value="/admin/product/update" var="saveUrl"/>
    </c:when>
    <c:otherwise>
        <c:url value="/admin/product/insert" var="saveUrl"/>
    </c:otherwise>
</c:choose>

<c:url value="/admin/products" var="listUrl"/>

<form action="${saveUrl}"
      method="post"
      enctype="multipart/form-data">

    <input type="hidden"
           name="csrfToken"
           value="<c:out value='${sessionScope.csrfToken}'/>">

    <input type="hidden"
           name="id"
           value="<c:out value='${productId}'/>">

    <div class="form-group">
        <label for="name">Tên sản phẩm</label>
        <input type="text"
               id="name"
               name="name"
               class="form-control"
               required
               maxlength="200"
               value="<c:out value='${name}'/>">
    </div>

    <div class="form-group">
        <label for="categoryId">Danh mục</label>
        <select id="categoryId"
                name="categoryId"
                class="form-control"
                required>
            <option value="">-- Chọn danh mục --</option>

            <c:forEach items="${categories}" var="category">
                <option value="${category.categoryid}"
                        ${categoryId == category.categoryid ? 'selected' : ''}>
                    <c:out value="${category.categoryname}"/>
                    ${category.status == 0 ? ' (đang khóa)' : ''}
                </option>
            </c:forEach>
        </select>

        <p class="help-block">
            Sản phẩm thuộc danh mục đang khóa sẽ không hiện ở trang khách.
        </p>
    </div>

    <div class="form-group">
        <label for="description">Mô tả</label>
        <textarea id="description"
                  name="description"
                  class="form-control"
                  rows="5"
                  maxlength="5000"><c:out value="${description}"/></textarea>
    </div>

    <div class="form-group">
        <label for="price">Giá (VNĐ)</label>
        <input type="number"
               id="price"
               name="price"
               class="form-control"
               required
               min="0"
               max="9999999999999.99"
               step="0.01"
               value="<c:out value='${price}'/>">
    </div>

    <div class="form-group">
        <label for="quantity">Số lượng</label>
        <input type="number"
               id="quantity"
               name="quantity"
               class="form-control"
               required
               min="0"
               max="2147483647"
               step="1"
               value="<c:out value='${quantity}'/>">
    </div>

    <div class="form-group">
        <label for="active">Trạng thái</label>
        <select id="active"
                name="active"
                class="form-control"
                required>
            <option value="1" ${active == '1' ? 'selected' : ''}>
                Hiển thị
            </option>
            <option value="0" ${active == '0' ? 'selected' : ''}>
                Ẩn
            </option>
        </select>
    </div>

    <div class="form-group">
        <label for="images">Ảnh sản phẩm</label>

        <c:choose>
            <c:when test="${not empty currentImage}">
                <c:url value="/image" var="imageUrl">
                    <c:param name="fname" value="${currentImage}"/>
                </c:url>

                <p>
                    <img id="imagess"
                         src="<c:out value='${imageUrl}'/>"
                         alt="Ảnh sản phẩm"
                         width="180"
                         height="150"
                         style="display: none; object-fit: contain;">
                </p>
            </c:when>

            <c:otherwise>
                <p>Chọn ảnh bên dưới để xem trước. Bấm Lưu sản phẩm để lưu ảnh.</p>
                <img id="imagess"
                     alt="Ảnh xem trước"
                     width="180"
                     height="150"
                     style="object-fit: contain;">
            </c:otherwise>
        </c:choose>

        <input type="file"
               id="images"
               name="images"
               accept="image/jpeg,image/png"
               onchange="chooseFile(this)">

        <p class="help-block">
            JPG/PNG, tối đa 5 MB và 16 triệu điểm ảnh.
            Khi sửa, không chọn ảnh mới thì giữ ảnh cũ.
        </p>
    </div>

    <button type="submit"
            class="btn btn-primary"
            ${empty categories ? 'disabled' : ''}>
        Lưu sản phẩm
    </button>

    <a href="${listUrl}" class="btn btn-default">
        Quay lại
    </a>
</form>

</body>
</html>