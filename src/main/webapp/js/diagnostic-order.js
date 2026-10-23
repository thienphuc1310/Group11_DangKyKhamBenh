/* Chỉ hỗ trợ chọn nhiều dịch vụ và tính tạm. DAO luôn kiểm tra lại nhân sự và giá khi xác nhận. */
(function () {
    var form = document.getElementById('diagnostic-order-form');
    if (!form) return;
    var choices = Array.from(form.querySelectorAll('input[name="serviceIds"]:not(:disabled)'));
    var selectAll = document.getElementById('select-all-services');
    var clear = document.getElementById('clear-services');
    var list = document.getElementById('selected-service-list');
    var count = document.getElementById('selected-service-count');
    var total = document.getElementById('selected-service-total');
    var hint = document.getElementById('diagnostic-selection-hint');
    var confirm = document.getElementById('confirm-diagnostic-order');
    var formatter = new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 2 });

    function updateSelection()
    {
        var selected = choices.filter(function (choice) { return choice.checked; });
        var amount = 0;
        list.textContent = '';
        choices.forEach(function (choice) {
            choice.closest('tr').classList.toggle('diagnostic-selected', choice.checked);
        });
        selected.forEach(function (choice) {
            var price = Number(choice.dataset.price);
            amount += price;
            var item = document.createElement('li');
            item.textContent = choice.closest('tr').querySelector('.diagnostic-service-name').textContent + ' — ' + formatter.format(price) + ' đ';
            list.appendChild(item);
        });
        count.textContent = selected.length;
        total.textContent = formatter.format(amount);
        hint.textContent = selected.length > 50 ? 'Tối đa 50 dịch vụ mỗi phiếu.' : '';
        hint.hidden = selected.length <= 50;
        selectAll.disabled = choices.length === 0 || selected.length === Math.min(choices.length, 50);
        clear.disabled = selected.length === 0;
        confirm.disabled = selected.length === 0 || selected.length > 50;
    }

    selectAll.hidden = false;
    if (choices.length > 50) selectAll.textContent = 'Chọn 50 dịch vụ đầu';
    clear.hidden = false;
    document.getElementById('diagnostic-selection').hidden = false;
    choices.forEach(function (choice) { choice.addEventListener('change', updateSelection); });
    selectAll.addEventListener('click', function () {
        choices.forEach(function (choice, index) { choice.checked = index < 50; });
        updateSelection();
    });
    clear.addEventListener('click', function () {
        choices.forEach(function (choice) { choice.checked = false; });
        updateSelection();
    });
    window.addEventListener('pageshow', updateSelection);
    updateSelection();
}

());

(function () {
    var search = document.getElementById('service-search');
    var searchButton = document.getElementById('search-services');
    var resetButton = document.getElementById('reset-service-search');
    var rows = document.querySelectorAll('.diagnostic-service-table tbody tr');
    var emptyMessage = document.getElementById('service-search-empty');

    if (!search || !searchButton || !resetButton) return;

    function normalize(value) {
        return value.toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/đ/g, 'd')
            .trim();
    }

    function filterServices() {
        var keyword = normalize(search.value);
        var matching = 0;
        var serviceCount = 0;

        rows.forEach(function (row) {
            var name = row.querySelector('.diagnostic-service-name');
            if (!name) return;
            serviceCount++;
            row.hidden = !normalize(name.textContent).includes(keyword);
            if (!row.hidden) matching++;
        });
        if (emptyMessage) emptyMessage.hidden = serviceCount === 0 || matching > 0;
    }

    search.addEventListener('input', filterServices);
    searchButton.addEventListener('click', filterServices);

    resetButton.addEventListener('click', function () {
        search.value = '';
        filterServices();
        search.focus();
    });

    search.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            filterServices();
        }
    });
    window.addEventListener('pageshow', filterServices);
    filterServices();
}());

