/* Các thao tác xem thử giao diện, chưa lưu dữ liệu lên máy chủ. */
document.querySelectorAll('[data-open]').forEach(function (button) {
    button.addEventListener('click', function () {
        var dialog = document.getElementById(button.dataset.open);
        dialog.showModal();
    });
});
document.querySelectorAll('[data-close]').forEach(function (button) {
    button.addEventListener('click', function () {
        button.closest('dialog').close();
    });
});
document.querySelectorAll('.dialog-form:not([data-persist])').forEach(function (form) {
    form.addEventListener('submit', function (event) { event.preventDefault(); });
});

/* Hiển thị và chuyển tuần theo ngày được chọn. */
function formatDate(date) {
    return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0');
}

// Tạo ngày mới để không thay đổi ngày gốc.
function addDays(date, days) {
    var result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
}

function startOfWeek(date) {
    var monday = addDays(date, -(date.getDay() + 6) % 7);
    monday.setHours(0, 0, 0, 0);
    return monday;
}

var scheduleDate = document.getElementById('schedule-date');
if (scheduleDate) {
    var scheduleRows = document.getElementById('saved-schedules');
    var calendarCells = document.querySelectorAll('[data-calendar-day]');
    var shiftDate = document.getElementById('shift-date');
    var leaveDate = document.getElementById('leave-date');

    // Dữ liệu minh họa theo tuần hiện tại, không ghi vào database.
    // Danh sách thật (kể cả danh sách rỗng) luôn được ưu tiên.
    if (scheduleRows && scheduleRows.dataset.demo === 'true') {
        scheduleRows.replaceChildren();
        var currentMonday = startOfWeek(new Date());
        var sampleId = 1;
        for (var week = -1; week <= 1; week++) {
            for (var dayIndex = 0; dayIndex < 6; dayIndex++) {
                var workDate = addDays(currentMonday, week * 7 + dayIndex);
                [['08:00', '11:30'], ['13:30', '17:00']].forEach(function (times, shiftIndex) {
                    var row = document.createElement('tr');
                    row.dataset.date = formatDate(workDate);
                    [sampleId++, 'BS001', row.dataset.date, times[0], times[1],
                        shiftIndex === 0 ? 'Phòng khám 101' : 'Phòng khám 102'].forEach(function (value) {
                        var cell = document.createElement('td');
                        cell.textContent = value;
                        row.appendChild(cell);
                    });
                    scheduleRows.appendChild(row);
                });
            }
        }
    }
    function updateWeek() {
        if (!scheduleDate.value) { return; }
        var monday = startOfWeek(new Date(scheduleDate.value + 'T00:00:00'));
        var sunday = addDays(monday, 6);
        document.getElementById('week-label').textContent = monday.toLocaleDateString('vi-VN') + ' – ' + sunday.toLocaleDateString('vi-VN');
        document.querySelectorAll('[data-day]').forEach(function (label) {
            var day = addDays(monday, Number(label.dataset.day));
            label.textContent = day.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
        });
        var cards = document.querySelectorAll('#saved-schedules tr[data-date]');
        var visibleCount = 0;
        calendarCells.forEach(function (cell) { cell.replaceChildren(); });
        cards.forEach(function (row) {
            var date = new Date(row.dataset.date + 'T00:00:00');
            if (date < monday || date > sunday) { return; }
            var cell = document.querySelector('[data-calendar-day="' + ((date.getDay() + 6) % 7) + '"]');
            var card = document.createElement('div');
            card.className = 'shift-card';
            if (row.dataset.status === 'ON_LEAVE' || row.dataset.status === 'REPLACED') {
                card.classList.add('shift-on-leave');
            }
            [row.cells[3].textContent + ' – ' + row.cells[4].textContent,
                row.cells[5].textContent].forEach(function (text) {
                var line = document.createElement('span');
                line.textContent = text;
                card.appendChild(line);
            });
            if (row.dataset.status === 'ON_LEAVE' || row.dataset.status === 'REPLACED') {
                var statusLine = document.createElement('span');
                statusLine.textContent = row.dataset.status === 'ON_LEAVE' ? 'Đã nghỉ · Chờ người thay' : 'Đã bàn giao';
                card.appendChild(statusLine);
            }
            cell.appendChild(card);
            visibleCount++;
        });
        document.getElementById('schedule-empty').hidden = visibleCount > 0;
        if (shiftDate && !shiftDate.value) {
            shiftDate.value = scheduleDate.value;
        }
        if (leaveDate) {
            leaveDate.value = scheduleDate.value;
        }
    }
    function moveWeek(days) {
        if (!scheduleDate.value) { scheduleDate.value = formatDate(new Date()); }
        scheduleDate.value = formatDate(addDays(new Date(scheduleDate.value + 'T00:00:00'), days));
        updateWeek();
    }
    scheduleDate.addEventListener('change', updateWeek);
    document.getElementById('previous-week').addEventListener('click', function () { moveWeek(-7); });
    document.getElementById('next-week').addEventListener('click', function () { moveWeek(7); });
    document.getElementById('today-button').addEventListener('click', function () {
        scheduleDate.value = formatDate(new Date());
        updateWeek();
    });
    if (!scheduleDate.value) {
        var firstSavedSchedule = document.querySelector('#saved-schedules tr[data-date]');
        scheduleDate.value = firstSavedSchedule
            ? firstSavedSchedule.dataset.date
            : formatDate(new Date());
    }
    updateWeek();
}

/* Tìm nhanh bệnh nhân trong danh sách của ngày đã được Servlet tải. */
var patientSearch = document.getElementById('patient-search');
if (patientSearch) {
    function normalizeText(value) {
        return value.toLocaleLowerCase('vi-VN').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/\s+/g, '');
    }
    function filterAppointments() {
        var keyword = normalizeText(patientSearch.value);
        var count = 0;
        document.querySelectorAll('#appointment-list .appointment-row').forEach(function (row) {
            var matchesKeyword = normalizeText(row.cells[3].textContent).includes(keyword);
            matchesKeyword = matchesKeyword || normalizeText(row.cells[2].textContent).includes(keyword);
            row.hidden = !matchesKeyword;
            if (!row.hidden) { count++; }
        });
        var waitingCount = document.getElementById('waiting-count');
        if (waitingCount) { waitingCount.textContent = count; }
        document.getElementById('appointment-count').textContent = count ? 'Hiển thị ' + count + ' lịch hẹn' : 'Không có lịch hẹn phù hợp.';
    }
    filterAppointments();
    patientSearch.addEventListener('input', filterAppointments);
}

/* Thêm và xóa dòng thuốc trong biểu mẫu. */
var addMedicine = document.getElementById('add-medicine');
if (addMedicine) {
    var medicineIndex = 0;
    addMedicine.addEventListener('click', function () {
        var row = document.createElement('tr');
        var fields = [
            ['medicineName', 'Tên thuốc', 'text'],
            ['dosage', 'Liều dùng', 'text'],
            ['frequency', 'Số lần mỗi ngày', 'number'],
            ['days', 'Số ngày', 'number']
        ];
        fields.forEach(function (field) {
            var cell = document.createElement('td');
            var input = document.createElement('input');
            input.type = field[2];
            input.name = field[0] + '[' + medicineIndex + ']';
            input.setAttribute('aria-label', field[1]);
            input.required = true;
            if (field[2] === 'number') { input.min = '1'; input.value = '1'; }
            cell.appendChild(input);
            row.appendChild(cell);
        });
        var cell = document.createElement('td');
        var remove = document.createElement('button');
        remove.type = 'button';
        remove.textContent = 'Xóa';
        remove.setAttribute('aria-label', 'Xóa dòng thuốc');
        remove.addEventListener('click', function () {
            row.remove();
            document.getElementById('medicine-empty').hidden = document.getElementById('medicine-list').children.length > 0;
            addMedicine.focus();
        });
        cell.appendChild(remove);
        row.appendChild(cell);
        document.getElementById('medicine-list').appendChild(row);
        document.getElementById('medicine-empty').hidden = true;
        row.querySelector('input').focus();
        medicineIndex++;
    });
}
