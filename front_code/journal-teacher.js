// Функционал для выпадающих списков в журнале учителя
document.addEventListener('DOMContentLoaded', function() {
    const filterSelects = document.querySelectorAll('.filter-select');

    // Закрытие всех выпадающих списков
    function closeAllDropdowns() {
        filterSelects.forEach(select => {
            select.classList.remove('active');
        });
    }

    // Обработчик для каждого селекта
    filterSelects.forEach(select => {
        const dropdown = select.querySelector('.dropdown-content');
        const selectedSpan = select.querySelector('.select-display span');
        const selectDisplay = select.querySelector('.select-display');

        // Клик по отображаемой области
        selectDisplay.addEventListener('click', function(e) {
            e.stopPropagation();
            const isActive = select.classList.contains('active');
            closeAllDropdowns();
            if (!isActive) {
                select.classList.add('active');
            }
        });

        // Клик по варианту в выпадающем списке
        dropdown.querySelectorAll('.dropdown-option').forEach(option => {
            option.addEventListener('click', function(e) {
                e.stopPropagation();
                selectedSpan.textContent = this.textContent;
                closeAllDropdowns();
                updateJournalTable();
            });
        });
    });

    // Закрытие выпадающих списков при клике вне их
    document.addEventListener('click', function() {
        closeAllDropdowns();
    });

    // Функция обновления таблицы журнала
    function updateJournalTable() {
        console.log('Обновление таблицы журнала...');
        // Здесь будет логика обновления таблицы на основе выбранных фильтров
    }
});