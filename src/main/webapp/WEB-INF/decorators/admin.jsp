<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title><sitemesh:write property="title"/></title>

    <link rel="stylesheet"
          href="<c:url value='/assets/global/plugins/bootstrap/css/bootstrap.min.css'/>">

    <sitemesh:write property="head"/>
</head>

<body>
    <nav class="navbar navbar-default">
        <div class="container">
            <div class="navbar-header">
                <a class="navbar-brand"
                   href="<c:url value='/admin/home'/>">
                    Quản trị
                </a>
            </div>

            <ul class="nav navbar-nav">
                <li>
                    <a href="<c:url value='/admin/categories'/>">
                        Danh mục
                    </a>
                </li>
                <li>
    				<a href="${pageContext.request.contextPath}/admin/products">
        				Sản phẩm
    				</a>
				</li>
                <li>
                    <a href="<c:url value='/user/home'/>">
                        Trang chủ
                    </a>
                </li>
            </ul>

            <ul class="nav navbar-nav navbar-right">
                <li>
                    <a href="<c:url value='/logout'/>">
                        Đăng xuất
                    </a>
                </li>
            </ul>
        </div>
    </nav>

    <main class="container">
        <sitemesh:write property="body"/>
    </main>

    <script src="<c:url value='/assets/global/plugins/jquery.min.js'/>"></script>

    <script src="<c:url value='/assets/global/plugins/bootstrap/js/bootstrap.min.js'/>"></script>

    <script>
    function chooseFile(fileInput) {
        const preview = document.getElementById("imagess");

        if (!preview) {
            return;
        }

        // Ghi nhớ ảnh gốc để khôi phục khi bỏ lựa chọn file.
        if (!preview.hasAttribute("data-original-src")) {
            preview.dataset.originalSrc =
                preview.getAttribute("src") || "";
        }

        const file = fileInput.files && fileInput.files[0];

        // Mỗi lần thay đổi lựa chọn có một mã riêng,
        // tránh lần đọc cũ ghi đè lần chọn mới.
        const selectionId =
                String(Number(preview.dataset.selectionId || "0") + 1);

        preview.dataset.selectionId = selectionId;

        function restoreOriginal() {
            const original = preview.dataset.originalSrc;

            if (original) {
                preview.src = original;
                preview.style.display = "inline-block";
            } else {
                preview.removeAttribute("src");
                preview.style.display = "none";
            }
        }

        if (!file) {
            restoreOriginal();
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            alert("Ảnh không được vượt quá 5 MB.");
            fileInput.value = "";
            restoreOriginal();
            return;
        }

        const reader = new FileReader();

        reader.onload = function (event) {
            if (preview.dataset.selectionId !== selectionId) {
                return;
            }

            preview.src = event.target.result;
            preview.style.display = "inline-block";
        };

        reader.onerror = function () {
            if (preview.dataset.selectionId !== selectionId) {
                return;
            }

            fileInput.value = "";
            restoreOriginal();
            alert("Không thể đọc file đã chọn.");
        };

        reader.readAsDataURL(file);
    }
</script>
</body>
</html>