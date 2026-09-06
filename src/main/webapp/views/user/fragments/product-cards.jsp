<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="vi_VN"/>

<c:choose>
    <c:when test="${empty products}">
        <p class="alert alert-info">
            Chưa có sản phẩm đang hiển thị.
        </p>
    </c:when>

    <c:otherwise>
        <div class="row">
            <c:forEach items="${products}" var="item" varStatus="stt">

                <c:url value="/product/detail" var="detailUrl">
                    <c:param name="id" value="${item.id}"/>
                </c:url>

                <div class="col-md-4 col-sm-6">
                    <div class="thumbnail">

                        <a href="<c:out value='${detailUrl}'/>">
                            <c:choose>
                                <c:when test="${not empty item.images}">
                                    <c:url value="/image" var="imageUrl">
                                        <c:param name="fname"
                                                 value="${item.images}"/>
                                    </c:url>

                                    <img src="<c:out value='${imageUrl}'/>"
                                         alt="<c:out value='${item.name}'/>"
                                         style="width: 100%; height: 220px; object-fit: contain;">
                                </c:when>

                                <c:otherwise>
                                    <div style="height: 220px; display: flex;
                                                align-items: center;
                                                justify-content: center;
                                                background: #f5f5f5;">
                                        Chưa có ảnh
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </a>

                        <div class="caption">
                            <h3 style="min-height: 52px; overflow-wrap: anywhere;">
                                <a href="<c:out value='${detailUrl}'/>">
                                    <c:out value="${item.name}"/>
                                </a>
                            </h3>

                            <p>
                                Danh mục:
                                <c:out value="${item.category.categoryname}"/>
                            </p>

                            <p class="text-danger">
                                <strong>
                                    <fmt:formatNumber value="${item.price}"
                                                      maxFractionDigits="2"/>
                                    đ
                                </strong>
                            </p>

                            <p>
                                ${item.quantity > 0 ? 'Còn hàng' : 'Hết hàng'}
                            </p>

                            <a href="<c:out value='${detailUrl}'/>"
                               class="btn btn-primary">
                                Xem chi tiết
                            </a>
                        </div>
                    </div>
                </div>

                <c:if test="${stt.count % 3 == 0}">
                    <div class="clearfix visible-md-block visible-lg-block"></div>
                </c:if>

                <c:if test="${stt.count % 2 == 0}">
                    <div class="clearfix visible-sm-block"></div>
                </c:if>

            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>