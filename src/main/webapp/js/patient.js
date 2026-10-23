document.querySelectorAll('[data-patient-filter-area]').forEach(function (area) {
    const buttons = area.querySelectorAll('[data-filter-value]');
    const search = area.querySelector('[data-filter-search]');
    const date = area.querySelector('[data-filter-date]');
    const items = area.querySelectorAll('[data-patient-item]');
    const empty = area.querySelector('[data-filter-empty]');
    const count = area.querySelector('[data-filter-count]');
    let status = 'all';

    function normalize(value) {
        return value.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').trim();
    }

    function filter() {
        const keyword = search ? normalize(search.value) : '';
        let visible = 0;
        items.forEach(function (item) {
            const matchesStatus = status === 'all' || status.split(',').includes(item.dataset.status);
            const matchesText = normalize(item.textContent).includes(keyword);
            const matchesDate = !date || !date.value || item.dataset.date === date.value;
            item.hidden = !(matchesStatus && matchesText && matchesDate);
            if (!item.hidden) visible++;
        });
        if (empty) empty.hidden = visible > 0 || items.length === 0;
        if (count) count.textContent = visible;
    }

    buttons.forEach(function (button) {
        button.addEventListener('click', function () {
            status = button.dataset.filterValue;
            buttons.forEach(function (other) {
                const active = other === button;
                other.classList.toggle('active', active);
                other.setAttribute('aria-pressed', String(active));
            });
            filter();
        });
    });
    if (search) search.addEventListener('input', filter);
    if (date) date.addEventListener('change', filter);
    const clear = area.querySelector('[data-filter-clear]');
    if (clear) clear.addEventListener('click', function () {
        if (search) search.value = '';
        if (date) date.value = '';
        const all = area.querySelector('[data-filter-value="all"]');
        if (all) all.click();
        else filter();
    });
    filter();
});
