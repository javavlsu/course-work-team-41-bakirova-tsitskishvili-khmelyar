// grades.js - клиентская логика для страницы успеваемости

(function() {
    'use strict';

    // Инициализация при загрузке страницы
    document.addEventListener('DOMContentLoaded', function() {
        initTooltips();
        initAutoSubmit();
    });

    // Инициализация тултипов
    function initTooltips() {
        const triggers = document.querySelectorAll('.grade-trigger');
        const tooltip = document.getElementById('globalTooltip');

        if (!tooltip) return;

        let activeTrigger = null;

        triggers.forEach(trigger => {
            // Показ тултипа
            trigger.addEventListener('mouseenter', function(e) {
                activeTrigger = this;
                const type = this.getAttribute('data-type');
                let content = '';

                if (type === 'grades') {
                    content = buildGradesTooltipContent(this);
                } else if (type === 'absences') {
                    content = buildAbsencesTooltipContent(this);
                }

                tooltip.innerHTML = content;
                tooltip.classList.add('show');
                positionTooltip(e, tooltip);
            });

            // Движение мыши
            trigger.addEventListener('mousemove', function(e) {
                if (activeTrigger === this) {
                    positionTooltip(e, tooltip);
                }
            });

            // Скрытие тултипа
            trigger.addEventListener('mouseleave', function() {
                if (activeTrigger === this) {
                    tooltip.classList.remove('show');
                    activeTrigger = null;
                }
            });
        });

        // Скрытие при скролле и ресайзе
        document.addEventListener('scroll', function() {
            if (activeTrigger) {
                tooltip.classList.remove('show');
                activeTrigger = null;
            }
        });

        window.addEventListener('resize', function() {
            if (activeTrigger) {
                tooltip.classList.remove('show');
                activeTrigger = null;
            }
        });
    }

    // Построение контента для тултипа с оценками
    function buildGradesTooltipContent(element) {
        const student = element.getAttribute('data-student') || '';
        const subject = element.getAttribute('data-subject') || '';
        const average = parseFloat(element.getAttribute('data-average') || 0).toFixed(2);
        const gradesStr = element.getAttribute('data-grades') || '';
        const grade5 = parseInt(element.getAttribute('data-grade5') || 0);
        const grade4 = parseInt(element.getAttribute('data-grade4') || 0);
        const grade3 = parseInt(element.getAttribute('data-grade3') || 0);
        const grade2 = parseInt(element.getAttribute('data-grade2') || 0);
        const total = grade5 + grade4 + grade3 + grade2;

        const title = student ? `Детализация оценок: ${student}` : `Детализация оценок: ${subject}`;

        return `
            <div class="tooltip-content">
                <div class="tooltip-title">${title}</div>
                <div class="tooltip-grades-list">${gradesStr ? `Оценки: [${gradesStr}]` : 'Нет оценок'}</div>
                <div class="tooltip-stats">
                    <div>Всего оценок: ${total}</div>
                    <div class="tooltip-grade-distribution">
                        <span class="grade-5">5: ${grade5}</span>
                        <span class="grade-4">4: ${grade4}</span>
                        <span class="grade-3">3: ${grade3}</span>
                        <span class="grade-2">2: ${grade2}</span>
                    </div>
                    <div class="tooltip-average">Средний балл: <strong>${average}</strong></div>
                </div>
            </div>
            <div class="tooltip-arrow"></div>
        `;
    }

    // Построение контента для тултипа с пропусками
    function buildAbsencesTooltipContent(element) {
        const student = element.getAttribute('data-student') || '';
        const subject = element.getAttribute('data-subject') || '';
        const total = parseInt(element.getAttribute('data-total') || 0);
        const absentH = parseInt(element.getAttribute('data-h') || 0);
        const absentU = parseInt(element.getAttribute('data-u') || 0);
        const absentB = parseInt(element.getAttribute('data-b') || 0);

        const title = student ? `Пропуски: ${student}` : `Пропуски: ${subject}`;

        return `
            <div class="tooltip-content">
                <div class="tooltip-title">${title}</div>
                <div class="tooltip-stats">
                    <div class="absence-type"><span class="absence-h">Н (неуваж.):</span> ${absentH}</div>
                    <div class="absence-type"><span class="absence-u">У (уваж.):</span> ${absentU}</div>
                    <div class="absence-type"><span class="absence-b">Б (болезнь):</span> ${absentB}</div>
                    <div class="absence-total">Всего пропусков: <strong>${total}</strong></div>
                </div>
            </div>
            <div class="tooltip-arrow"></div>
        `;
    }

    // Позиционирование тултипа
    function positionTooltip(event, tooltip) {
        const mouseX = event.clientX;
        const mouseY = event.clientY;
        const rect = tooltip.getBoundingClientRect();
        const viewportWidth = window.innerWidth;
        const viewportHeight = window.innerHeight;

        let left = mouseX + 15;
        let top = mouseY + 15;

        if (left + rect.width > viewportWidth - 10) {
            left = mouseX - rect.width - 15;
        }
        if (left < 10) left = 10;

        if (top + rect.height > viewportHeight - 10) {
            top = mouseY - rect.height - 15;
        }
        if (top < 10) top = 10;

        tooltip.style.left = left + 'px';
        tooltip.style.top = top + 'px';
        tooltip.style.right = 'auto';
        tooltip.style.bottom = 'auto';
    }

    // Автоматическая отправка формы при изменении фильтров
    function initAutoSubmit() {
        const selects = document.querySelectorAll('#quarterForm select, #teacherFilterForm select');

        selects.forEach(select => {
            select.addEventListener('change', function() {
                const form = this.closest('form');
                if (form) {
                    form.submit();
                }
            });
        });
    }
})();