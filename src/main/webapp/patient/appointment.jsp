<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Đăng ký khám</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css"></head><body>
<div class="container"><div class="card form-card"><h2>Đăng ký lịch khám</h2>
<form action="${pageContext.request.contextPath}/appointment" method="post">
<div class="form-group"><label>Họ và tên</label><input name="fullName" required></div>
<div class="form-group"><label>Ngày khám</label><input type="date" name="date" required></div>
<div class="form-group"><label>Giờ khám</label><input type="time" name="time" required></div>
<div class="form-group"><label>Bác sĩ</label><select name="doctor"><option>Chọn bác sĩ</option><option>Nguyễn Văn A</option><option>Trần Thị B</option></select></div>
<div class="form-group"><label>Lý do khám</label><textarea name="reason"></textarea></div>
<button class="btn">Xác nhận đặt lịch</button></form></div></div></body></html>