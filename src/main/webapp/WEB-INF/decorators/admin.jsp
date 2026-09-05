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
            const file = fileInput.files && fileInput.files[0];

            if (!preview || !file) {
                return;
            }

            const reader = new FileReader();

            reader.onload = function (event) {
                preview.src = event.target.result;
            };

            reader.readAsDataURL(file);
        }
    </script>
</body>
</html>