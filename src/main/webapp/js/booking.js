(function ()
{
    //lấy các thành phần trong jsp
    const form = document.getElementById("booking-form");
    const dateSelect = document.getElementById("appointment-date");
    const specialtySelect = document.getElementById("chuyenkhoa");
    const doctorSelect = document.getElementById("doctor");
    const timeSlotList = document.getElementById("time-slot-list");

    if (!form || !dateSelect || !specialtySelect || !doctorSelect || !timeSlotList) {
        return;
    }

    const contextPath = form.dataset.contextPath;
    dateSelect.min = new Date().toISOString().split("T")[0];

    //reset lại danh sách bác sĩ nếu ng dùng chưa chọn ngày hoặc chuyên khoa
    function resetDoctors(message)
    {
        doctorSelect.innerHTML = '<option value="">' + message + '</option>';
        doctorSelect.disabled = true;
        resetSchedules("-- Chọn bác sĩ trước --");
    }

    //xoá các khung giờ hiện tại thay bằng một thông báo
    function resetSchedules(message)
    {
        timeSlotList.innerHTML = '<p class="empty-time-slots">' + message + '</p>';
    }

    //tải danh sách bác sĩ theo ngày vào chuyên khoa phục vụ cho đăng ký khám bệnh
    async function loadDoctors()
    {
        const workDate = dateSelect.value;
        const specialtyId = specialtySelect.value;

        if (!workDate || !specialtyId) {
            resetDoctors("Chọn ngày và chuyên khoa trước");
            return;
        }

        resetDoctors("Đang tải bác sĩ");

        try {
            const parameters = new URLSearchParams({
                action: "doctors",
                workDate: workDate,
                specialtyId: specialtyId
            });
            const response = await fetch(contextPath + "/booking-options?" + parameters);
            if (!response.ok) throw new Error("Không thể tải bác sĩ");

            doctorSelect.innerHTML = await response.text();
            doctorSelect.disabled = false;
        } catch (error) {
            resetDoctors("-- Không thể tải bác sĩ --");
            console.error(error);
        }
    }

    //tải khung giờ khả thi của bác sĩ
    async function loadSchedules() {
        const doctorId = doctorSelect.value;
        const workDate = dateSelect.value;

        if (!doctorId || !workDate) {
            resetSchedules("-- Chọn bác sĩ trước --");
            return;
        }

        resetSchedules("-- Đang tải khung giờ --");

        try {
            const parameters = new URLSearchParams({
                action: "schedules",
                workDate: workDate,
                doctorId: doctorId
            });
            const response = await fetch(contextPath + "/booking-options?" + parameters);
            if (!response.ok) throw new Error("Không thể tải khung giờ");

            timeSlotList.innerHTML = await response.text();
        } catch (error) {
            resetSchedules("-- Không thể tải khung giờ --");
            console.error(error);
        }
    }

    dateSelect.addEventListener("change", loadDoctors);
    specialtySelect.addEventListener("change", loadDoctors);
    doctorSelect.addEventListener("change", loadSchedules);
})();
