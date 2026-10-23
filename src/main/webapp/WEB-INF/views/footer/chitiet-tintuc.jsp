<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isELIgnored="false" %>

<%
    String id = request.getParameter("id");
    if (id == null || id.trim().isEmpty()) {
        id = "1";
    }
%>

<%--Header--%>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="bodyClass" value="patient-site patient-public"/><jsp:param name="pageTitle" value="Chi tiết tin tức | UCare"/></jsp:include>

<main class="detail-main">
    <section class="detail-page">
        <% if ("1".equals(id)) { %>
        <!-- ================= BÀI 1 ================= -->
        <article class="news-detail">
            <a class="back-link" href="${pageContext.request.contextPath}/tintuc"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Quay lại tin tức</a>

            <div class="detail-meta">
                <span>VnExpress</span>
                <time>11/09/2026</time>
            </div>

            <h1>Nhân viên y tế được phụ cấp ưu đãi nghề tối đa 80%</h1>

            <p class="detail-lead">
                Nhân viên y tế tại bệnh viện công lập, trạm y tế cấp xã và y tế
                trường học được hưởng phụ cấp ưu đãi nghề từ 30% đến 80%, tùy
                vị trí và tính chất công việc.
            </p>

            <img class="detail-image"
                 src="${pageContext.request.contextPath}/images/footer/nhanvienytechamsocnguoibenh.png"
                 alt="Nhân viên y tế chăm sóc người bệnh">

            <h2>Mức phụ cấp cao nhất 80%</h2>
            <p>
                Theo Nghị định 350, mức phụ cấp ưu đãi nghề cao nhất là 80%.
                Mức này áp dụng cho viên chức thường xuyên, trực tiếp thực hiện
                một số công việc có tính chất đặc thù như khám, điều trị, xét
                nghiệm và chăm sóc người bệnh tâm thần; làm pháp y, pháp y tâm
                thần; hồi sức cấp cứu, cấp cứu, hồi sức tích cực, chống độc và
                giải phẫu bệnh.
            </p>

            <p>
                Nhân viên làm chuyên môn y tế tại trạm y tế cấp xã hoặc cơ sở
                y tế dự phòng ở khu vực biên giới, hải đảo, xã khó khăn hoặc
                đặc biệt khó khăn thuộc vùng đồng bào dân tộc thiểu số và miền
                núi cũng thuộc nhóm được hưởng mức 80%.
            </p>

            <h2>Các mức phụ cấp khác</h2>
            <p>
                Ngoài mức 80%, chính sách còn quy định các mức 70%, 60%, 50%,
                40% và 30% cho những nhóm công việc khác nhau. Mức hưởng được
                xác định dựa trên vị trí công tác, mức độ đặc thù và điều kiện
                làm việc của nhân viên y tế.
            </p>

            <p>
                Trong đó, mức 70% áp dụng cho một số nhóm trực tiếp khám,
                điều trị, chăm sóc người bệnh mắc bệnh phong, lao, bệnh truyền
                nhiễm nhóm A, HIV/AIDS và một số công việc có yêu cầu đặc thù.
                Các mức 60%, 50% và 40% cũng được áp dụng cho những nhóm công
                việc chuyên môn được quy định cụ thể trong nghị định.
            </p>

            <h2>Thời điểm thực hiện</h2>
            <p>
                Nghị định 350 có hiệu lực từ ngày 09/09/2026. Các mức phụ cấp
                ưu đãi nghề được thực hiện từ ngày 01/01/2026. Phụ cấp được trả
                cùng kỳ lương hằng tháng và không dùng để tính đóng, hưởng bảo
                hiểm xã hội.
            </p>

            <div class="source-box">
                <strong>Nguồn:</strong>
                <a href="https://vnexpress.net/nhan-vien-y-te-duoc-phu-cap-uu-dai-nghe-toi-da-80-5119156.html"
                   target="_blank" rel="noopener">
                    VnExpress
                </a>
            </div>
        </article>

        <% } else if ("2".equals(id)) { %>
        <!-- ================= BÀI 2 ================= -->
        <article class="news-detail">
            <a class="back-link" href="${pageContext.request.contextPath}/tintuc"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Quay lại tin tức</a>

            <div class="detail-meta">
                <span>Sức khỏe và Đời sống</span>
                <time>17/09/2026</time>
            </div>

            <h1>
                Bệnh không lây nhiễm chiếm 74% ca tử vong toàn cầu:
                Cần chăm sóc an toàn suốt đời
            </h1>

            <p class="detail-lead">
                Bệnh không lây nhiễm tạo ra gánh nặng lớn đối với sức khỏe cộng
                đồng và đặt ra yêu cầu về chăm sóc, theo dõi và quản lý sức khỏe
                liên tục trong thời gian dài.
            </p>

            <img class="detail-image"
                 src="${pageContext.request.contextPath}/images/footer/benhkhonglaynhiem.png"
                 alt="Hình ảnh minh họa chăm sóc sức khỏe">

            <h2>Gánh nặng của bệnh không lây nhiễm</h2>
            <p>
                Bệnh không lây nhiễm bao gồm nhiều nhóm bệnh mạn tính như bệnh
                tim mạch, ung thư, bệnh hô hấp mạn tính và đái tháo đường.
                Những bệnh này thường diễn biến trong thời gian dài, vì vậy
                việc chăm sóc sức khỏe không chỉ tập trung vào thời điểm người
                bệnh xuất hiện triệu chứng.
            </p>

            <p>
                Quản lý bệnh hiệu quả cần gắn với phát hiện sớm, theo dõi định
                kỳ, điều trị phù hợp và duy trì các biện pháp chăm sóc trong
                cộng đồng. Đây là cơ sở để giảm nguy cơ biến chứng và góp phần
                nâng cao chất lượng cuộc sống của người bệnh.
            </p>

            <h2>Chăm sóc an toàn suốt đời</h2>
            <p>
                Với các bệnh mạn tính, quá trình chăm sóc có thể kéo dài nhiều
                năm. Người bệnh có thể cần tái khám, sử dụng thuốc, kiểm soát
                các yếu tố nguy cơ và phối hợp với nhân viên y tế trong quá
                trình theo dõi sức khỏe.
            </p>

            <p>
                Vì vậy, an toàn người bệnh cần được đặt ra xuyên suốt quá trình
                chăm sóc, từ cơ sở y tế đến gia đình và cộng đồng. Việc cung cấp
                thông tin đầy đủ, theo dõi liên tục và phối hợp giữa người bệnh,
                gia đình và nhân viên y tế có vai trò quan trọng.
            </p>

            <h2>Chủ động phòng ngừa và quản lý</h2>
            <p>
                Bên cạnh điều trị, phòng ngừa và phát hiện sớm là những nội dung
                quan trọng trong kiểm soát bệnh không lây nhiễm. Việc duy trì
                lối sống phù hợp, kiểm tra sức khỏe và tuân thủ hướng dẫn điều
                trị giúp người bệnh chủ động hơn trong quá trình chăm sóc lâu dài.
            </p>

            <div class="source-box">
                <strong>Nguồn:</strong>
                <a href="https://suckhoedoisong.vn/benh-khong-lay-nhiem-chiem-74-ca-tu-vong-toan-cau-can-cham-soc-an-toan-suot-doi-169260917182424937.htm"
                   target="_blank" rel="noopener">
                    Sức khỏe và Đời sống
                </a>
            </div>
        </article>

        <% } else if ("3".equals(id)) { %>
        <!-- ================= BÀI 3 ================= -->
        <article class="news-detail">
            <a class="back-link" href="${pageContext.request.contextPath}/tintuc"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Quay lại tin tức</a>

            <div class="detail-meta">
                <span>Sở Y tế TP.HCM</span>
                <time>28/04/2025</time>
            </div>

            <h1>
                20 xe cấp cứu 2 bánh, 64 xe cứu thương sẵn sàng ứng trực
                phục vụ lễ 30/4
            </h1>

            <p class="detail-lead">
                Ngành Y tế TP.HCM huy động mạng lưới cấp cứu ngoại viện gồm
                20 xe cấp cứu 2 bánh, 64 xe cứu thương và 146 điểm sơ cứu
                để sẵn sàng phục vụ người dân trong dịp lễ 30/4.
            </p>

            <img class="detail-image"
                 src="${pageContext.request.contextPath}/images/footer/20xecapcuu2banh.png"
                 alt="Kỷ niệm ngày 30 tháng 4 năm 2025">

            <h2>Huy động mạng lưới cấp cứu ngoại viện</h2>
            <p>
                Trung tâm Cấp cứu 115 được giao làm đầu mối triển khai phương án
                cấp cứu tổng thể xuyên suốt các hoạt động. Lực lượng cấp cứu
                được bố trí theo ba vùng gồm khu vực tại chỗ, vùng đệm và vùng
                dự phòng, phù hợp với đặc điểm của từng khu vực tổ chức sự kiện.
            </p>

            <p>
                Cùng với 64 xe cứu thương của mạng lưới cấp cứu ngoại viện,
                ngành y tế huy động thêm 20 xe cấp cứu 2 bánh để ứng trực tại
                vùng đệm. Phương tiện hai bánh giúp tiếp cận nhanh hơn tại
                những khu vực đông người hoặc nơi xe cứu thương khó tiếp cận.
            </p>

            <h2>44 trạm cấp cứu vệ tinh và 146 điểm sơ cứu</h2>
            <p>
                Trung tâm Cấp cứu 115 cùng 44 trạm cấp cứu vệ tinh chuẩn bị xe
                cứu thương, trang thiết bị và thuốc cấp cứu để phục vụ người dân
                trong cộng đồng.
            </p>

            <p>
                Bên cạnh đó, 146 điểm sơ cứu của Hội Chữ thập đỏ được bố trí
                sẵn sàng phối hợp xử lý các tình huống y tế. Phương án này nhằm
                bảo đảm người gặp vấn đề sức khỏe có thể được hỗ trợ và chuyển
                đến cơ sở y tế phù hợp một cách nhanh chóng, an toàn.
            </p>

            <h2>Phương án cấp cứu theo ba vùng</h2>
            <p>
                Việc tổ chức cấp cứu theo khu vực tại chỗ, vùng đệm và vùng dự
                phòng giúp lực lượng y tế chủ động điều phối trong điều kiện
                đông người và giao thông phức tạp. Các luồng di chuyển cấp cứu
                được thiết kế phù hợp với hệ thống giao thông và cơ sở y tế
                tiếp nhận.
            </p>

            <div class="source-box">
                <strong>Nguồn:</strong>
                <a href="https://medinet.hochiminhcity.gov.vn/tin-tuc-su-kien/so-y-te-tphcm-20-xe-cap-cuu-2-banh-64-xe-cuu-thuong-cua-mang-luoi-cap-cuu-ngoai-cmobile1780-73089.aspx"
                   target="_blank" rel="noopener">
                    Sở Y tế TP.HCM / Medinet
                </a>
            </div>
        </article>

        <% } else { %>
        <article class="news-detail">
            <h1>Không tìm thấy bài viết</h1>
            <a class="back-link" href="${pageContext.request.contextPath}/tintuc"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Quay lại tin tức</a>
        </article>
        <% } %>

    </section>
</main>

<%--footer--%>
<jsp:include page="/WEB-INF/includes/footer-user.jsp"/>
