<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isELIgnored="false" %>

<%--Header--%>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="pageTitle" value="Liên hệ | UCare"/>
    <jsp:param name="bodyClass" value="patient-site patient-public patient-contact"/>
</jsp:include>

<main class="contact-main">
    <section class="contact-section">
        <div class="public-heading">
            <span class="public-eyebrow">LIÊN HỆ UCARE</span>
            <h1>Chúng tôi có thể giúp gì cho bạn?</h1>
            <p>Liên hệ để được hỗ trợ về lịch khám và các dịch vụ tại UCare.</p>
        </div>
        <div class="contact-layout">
        <div class="contact-intro">
            <h2>Thông tin liên hệ</h2>
            <p>Bạn có thể gọi điện hoặc gửi email cho chúng tôi qua các thông tin bên dưới.</p>
            <dl class="contact-details">
                <div><dt>Địa chỉ</dt><dd>215 Hồng Bàng, Phường Chợ Lớn, TP. Hồ Chí Minh</dd></div>
                <div><dt>Điện thoại</dt><dd><a href="tel:19008386">1900 8386</a></dd></div>
                <div><dt>Email hỗ trợ</dt><dd><a href="mailto:support@ucare.com">support@ucare.com</a></dd></div>
            </dl>
            <a class="patient-button patient-button-primary" href="${pageContext.request.contextPath}/main#booking-title">Đặt lịch khám <img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-right-white.png" alt="" aria-hidden="true"></a>
        </div>

        <form class="contact-form" action="${pageContext.request.contextPath}/lienhe" method="post">
            <div><h2>Gửi thông tin liên hệ</h2><p>Các mục có dấu * là bắt buộc.</p></div>
            <div class="contact-row">
                <div class="contact-form-group">
                    <label for="fullName">Họ và tên *</label>
                    <input
                        type="text"
                        id="fullName"
                        name="fullName"
                        placeholder="Họ và tên"
                        required>
                </div>

                <div class="contact-form-group">
                    <label for="address">Địa chỉ</label>
                    <input
                        type="text"
                        id="address"
                        name="address"
                        placeholder="Địa chỉ">
                </div>
            </div>

            <div class="contact-row">
                <div class="contact-form-group">
                    <label for="email">Email *</label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        placeholder="Email"
                        required>
                </div>

                <div class="contact-form-group">
                    <label for="phone">Số điện thoại</label>
                    <input
                        type="tel"
                        id="phone"
                        name="phone"
                        placeholder="Số điện thoại">
                </div>
            </div>

            <div class="contact-form-group contact-full-width">
                <label for="subject">Tiêu đề *</label>
                <input
                    type="text"
                    id="subject"
                    name="subject"
                    placeholder="Tiêu đề"
                    required>
            </div>

            <div class="contact-form-group contact-full-width">
                <label for="message">Nội dung *</label>
                <textarea
                    id="message"
                    name="message"
                    rows="7"
                    placeholder="Nội dung"
                    required></textarea>
            </div>

            <div class="contact-submit-wrapper">
                <button type="submit" class="contact-submit-button">
                    Gửi thông tin
                </button>
            </div>
        </form>
        </div>
    </section>
</main>

<%--footer--%>
<jsp:include page="/WEB-INF/includes/footer-user.jsp"/>
