<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isELIgnored="false" %>

<%--Header--%>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="bodyClass" value="patient-site patient-public"/><jsp:param name="pageTitle" value="Tin tức | UCare"/></jsp:include>

<main class="news-main">
    <section class="news-page">

        <div class="public-heading"><span class="public-eyebrow">BẢN TIN UCARE</span><h1>Tin tức &amp; sự kiện</h1><p>Thông tin y tế và những hoạt động chăm sóc sức khỏe.</p></div>

        <!-- ================= BÀI VIẾT LỚN ================= -->
        <article class="featured-news">
            <a class="featured-image" href="${pageContext.request.contextPath}/chitiet-tintuc?id=1">
                <img src="${pageContext.request.contextPath}/images/footer/nhanvienytechamsocnguoibenh.png"
                     alt="Nhân viên y tế chăm sóc người bệnh">
            </a>

            <div class="featured-content">
                <span class="news-source-badge">VnExpress</span>
                <time datetime="2026-09-11">11/09/2026</time>

                <h2>
                    <a href="${pageContext.request.contextPath}/chitiet-tintuc?id=1">
                        Nhân viên y tế được phụ cấp ưu đãi nghề tối đa 80%
                    </a>
                </h2>

                <p>
                    Theo VnExpress, Nghị định 350 quy định chế độ phụ cấp ưu đãi
                    nghề đối với nhân viên y tế với các mức từ 30% đến 80%,
                    tùy vị trí và tính chất công việc.
                </p>

                <a class="btn-detail" href="${pageContext.request.contextPath}/chitiet-tintuc?id=1">
                    Xem chi tiết
                </a>
            </div>
        </article>

        <!-- ================= 2 BÀI VIẾT NHỎ ================= -->
        <div class="news-grid">

            <!-- BÀI 2 -->
            <article class="news-card">
                <a class="news-card-image" href="${pageContext.request.contextPath}/chitiet-tintuc?id=2">
                    <img src="${pageContext.request.contextPath}/images/footer/benhkhonglaynhiem.png"
                         alt="Chăm sóc sức khỏe">
                </a>

                <div class="news-card-body">
                    <span class="news-source-badge">Sức khỏe và Đời sống</span>
                    <time datetime="2026-09-17">17/09/2026</time>

                    <h3>
                        <a href="${pageContext.request.contextPath}/chitiet-tintuc?id=2">
                            Bệnh không lây nhiễm chiếm 74% ca tử vong toàn cầu:
                            Cần chăm sóc an toàn suốt đời
                        </a>
                    </h3>

                    <p>
                        Bài viết đề cập gánh nặng bệnh không lây nhiễm và yêu cầu
                        bảo đảm an toàn trong toàn bộ quá trình chăm sóc người bệnh.
                    </p>

                    <a class="read-more" href="${pageContext.request.contextPath}/chitiet-tintuc?id=2">
                        Xem chi tiết <img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-right.png" alt="" aria-hidden="true">
                    </a>
                </div>
            </article>

            <!-- BÀI 3 -->
            <article class="news-card">
                <a class="news-card-image" href="${pageContext.request.contextPath}/chitiet-tintuc?id=3">
                    <img src="${pageContext.request.contextPath}/images/footer/20xecapcuu2banh.png"
                         alt="Kỷ niệm ngày 30 tháng 4 năm 2025">
                </a>

                <div class="news-card-body">
                    <span class="news-source-badge">Sở Y tế TP.HCM</span>
                    <time datetime="2025-04-28">28/04/2025</time>

                    <h3>
                        <a href="${pageContext.request.contextPath}/chitiet-tintuc?id=3">
                            20 xe cấp cứu 2 bánh, 64 xe cứu thương của mạng lưới
                            cấp cứu ngoại viện sẵn sàng ứng trực phục vụ lễ 30/4
                        </a>
                    </h3>

                    <p>
                        TP.HCM huy động lực lượng cấp cứu ngoại viện, xe cứu thương
                        và các điểm sơ cứu để sẵn sàng hỗ trợ người dân trong dịp lễ.
                    </p>

                    <a class="read-more" href="${pageContext.request.contextPath}/chitiet-tintuc?id=3">
                        Xem chi tiết <img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-right.png" alt="" aria-hidden="true">
                    </a>
                </div>
            </article>

        </div>

    </section>
</main>

<%--footer--%>
<jsp:include page="/WEB-INF/includes/footer-user.jsp"/>
