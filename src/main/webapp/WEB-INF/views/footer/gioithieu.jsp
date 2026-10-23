<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isELIgnored="false" %>

<%--Header--%>
<jsp:include page="/WEB-INF/includes/header.jsp"/>

<main class="gioithieu-main">
    <section class="gioithieu-hero">
        <h1>VỀ CHÚNG TÔI</h1>

        <div class="gioithieu-divider" aria-hidden="true">
            <span></span>
            <span class="gioithieu-symbol">✣</span>
            <span></span>
        </div>

        <p class="gioithieu-slogan">
            Với mong muốn nâng cao chất lượng khám chữa bệnh, đồng thời giúp người dân dễ dàng tiếp cận các dịch vụ y tế hiện đại và chất lượng cao.
        </p>
    </section>

    <section class="gioithieu-content">
        <div class="gioithieu-image">
            <img src="${pageContext.request.contextPath}/images/footer/gioithieu.png"
                 alt="Đội ngũ nhân viên y tế">
        </div>

        <div class="gioithieu-text">

            <p>
                Phòng khám chúng tôi được thành lập với sứ mệnh nâng cao chất lượng khám chữa bệnh, mang đến cho người dân những dịch vụ y tế chuyên nghiệp, hiện đại và thuận tiện. Luôn tâm niệm: "Lương y như từ mẫu - Thầy thuốc như mẹ hiền", phòng khám luôn chú trọng xây dựng môi trường khám chữa bệnh an toàn, tận tâm và hiệu quả. Xem khách hàng là trung tâm, phòng khám không ngừng cải tiến chất lượng dịch vụ, ứng dụng công nghệ hiện đại và nâng cao năng lực đội ngũ y tế. Qua đó, chúng tôi mong muốn trở thành địa chỉ chăm sóc sức khỏe đáng tin cậy, góp phần nâng cao chất lượng cuộc sống và mang đến những trải nghiệm khám chữa bệnh tốt nhất cho cộng đồng.
            </p>

        </div>
    </section>
</main>

<%--footer--%>
<jsp:include page="/WEB-INF/includes/footer.jsp"/>
