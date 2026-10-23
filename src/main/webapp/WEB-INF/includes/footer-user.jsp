<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<footer class="patient-footer">
    <div class="patient-footer-content">
        <div class="patient-footer-intro">
            <a class="patient-footer-logo" href="${pageContext.request.contextPath}/main"><img class="ui-logo" src="${pageContext.request.contextPath}/images/icons/ui/brand-mark.png" alt="" aria-hidden="true">UCare</a>
            <p>Đặt lịch khám và theo dõi thông tin sức khỏe của bạn tại UCare.</p>
            <a class="patient-footer-booking" href="${pageContext.request.contextPath}/main#booking-title">Đặt lịch khám <img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-right.png" alt="" aria-hidden="true"></a>
        </div>
        <nav aria-label="Liên kết cuối trang">
            <h2>Khám phá UCare</h2>
            <ul>
                <li><a href="${pageContext.request.contextPath}/chuyenkhoa">Chuyên khoa</a></li>
                <li><a href="${pageContext.request.contextPath}/bacsi">Đội ngũ bác sĩ</a></li>
                <li><a href="${pageContext.request.contextPath}/tintuc">Tin tức &amp; sự kiện</a></li>
                <li><a href="${pageContext.request.contextPath}/main#huong-dan">Hướng dẫn đặt khám</a></li>
            </ul>
        </nav>
        <div class="patient-footer-contact">
            <h2>Liên hệ &amp; hỗ trợ</h2>
            <p>215 Hồng Bàng, Phường Chợ Lớn,<br>TP. Hồ Chí Minh</p>
            <a href="tel:19008386">1900 8386</a>
            <a href="mailto:support@ucare.com">support@ucare.com</a>
            <a href="${pageContext.request.contextPath}/lienhe">Thông tin liên hệ <img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-right.png" alt="" aria-hidden="true"></a>
        </div>
    </div>
    <div class="patient-footer-bottom">
        <small>© 2026 UCare.</small>
        <a href="${pageContext.request.contextPath}/gioithieu">Về UCare</a>
    </div>
</footer>
</body>
</html>
