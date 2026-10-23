/* Gợi ý tên thuốc và thêm/xóa dòng; DAO vẫn kiểm tra thuốc, tồn kho và đơn giá. */
(function () {
    var container = document.getElementById('prescription-lines');
    var add = document.getElementById('add-prescription-line');
    var catalog = document.getElementById('prescription-medicines');
    if (!container || !add || !catalog) return;
    var medicines = Array.from(catalog.options);
    var rowNumber = 0;
    var template = container.firstElementChild.cloneNode(true);
    template.querySelectorAll('input').forEach(function (field) {
        var value = field.name === 'quantity' || field.name === 'durationDays' ? '1' : (field.name === 'route' ? 'Uống' : '');
        field.value = value;
        field.setAttribute('value', value);
    });

    function prepareLine(line) {
        var search = line.querySelector('.medicine-search');
        var medicineId = line.querySelector('input[name="medicineId"]');
        var summary = line.querySelector('.medicine-summary');
        var fieldId = 'medicine-search-' + rowNumber++;
        search.id = fieldId;
        line.querySelector('.prescription-medicine-field label').htmlFor = fieldId;
        summary.id = fieldId + '-summary';
        search.setAttribute('aria-describedby', summary.id);

        function updateMedicine() {
            var name = search.value.trim().toLocaleLowerCase('vi');
            var medicine = medicines.find(function (option) {
                return option.value.toLocaleLowerCase('vi') === name;
            });
            medicineId.value = medicine ? medicine.dataset.medicineId : '';
            summary.textContent = medicine ? medicine.label : '';
            summary.hidden = !medicine;
            search.setCustomValidity(name && !medicine ? 'Chọn thuốc từ danh sách gợi ý.' : '');
        }

        search.addEventListener('input', updateMedicine);
        search.addEventListener('change', updateMedicine);
        window.addEventListener('pageshow', updateMedicine);
        updateMedicine();
    }

    function updateButtons() {
        add.disabled = container.children.length >= 50;
        container.querySelectorAll('.remove-line').forEach(function (button) {
            button.disabled = container.children.length === 1;
        });
    }

    Array.from(container.children).forEach(prepareLine);
    add.addEventListener('click', function () {
        if (container.children.length >= 50) return;
        var line = template.cloneNode(true);
        container.appendChild(line);
        prepareLine(line);
        updateButtons();
        line.querySelector('.medicine-search').focus();
    });
    container.addEventListener('click', function (event) {
        if (event.target.classList.contains('remove-line') && container.children.length > 1) {
            event.target.closest('.prescription-line').remove();
            updateButtons();
        }
    });
    updateButtons();
}());
