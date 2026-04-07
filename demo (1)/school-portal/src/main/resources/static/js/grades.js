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
        const tooltip = document.getElementById('globalTooltip');
        if (!tooltip) return;

        let activeTrigger = null;

        function buildGradesContent(element) {
            const student = element.getAttribute('data-student') || '';
            const subject = element.getAttribute('data-subject') || '';
            const average = element.getAttribute('data-average') || '0';
            const grade5 = element.getAttribute('data-grade5') || '0';
            const grade4 = element.getAttribute('data-grade4') || '0';
            const grade3 = element.getAttribute('data-grade3') || '0';
            const grade2 = element.getAttribute('data-grade2') || '0';
            const total = parseInt(grade5) + parseInt(grade4) + parseInt(grade3) + parseInt(grade2);

            return `
                <div class="tooltip-content">
                    <div class="tooltip-title">📊 Детализация оценок</div>
                    <div class="fw-bold mb-1">${escapeHtml(student || subject)}</div>
                    <div class="grade-stat"><span>Отлично (5):</span> <span class="grade-5 fw-bold">${grade5}</span></div>
                    <div class="grade-stat"><span>Хорошо (4):</span> <span class="grade-4 fw-bold">${grade4}</span></div>
                    <div class="grade-stat"><span>Удовлетворительно (3):</span> <span class="grade-3 fw-bold">${grade3}</span></div>
                    <div class="grade-stat"><span>Неудовлетворительно (2):</span> <span class="grade-2 fw-bold">${grade2}</span></div>
                    <div class="grade-distribution"><span>📝 Всего оценок:</span> <strong>${total}</strong></div>
                    <div class="tooltip-average"><span>📈 Средний балл:</span> <strong class="text-info">${average}</strong></div>
                </div>
            `;
        }

        function buildAbsencesContent(element) {
            const student = element.getAttribute('data-student') || '';
            const subject = element.getAttribute('data-subject') || '';
            const total = element.getAttribute('data-total') || '0';
            const absentH = element.getAttribute('data-h') || '0';
            const absentU = element.getAttribute('data-u') || '0';
            const absentB = element.getAttribute('data-b') || '0';

            return `
                <div class="tooltip-content">
                    <div class="tooltip-title">📋 Детализация пропусков</div>
                    <div class="fw-bold mb-1">${escapeHtml(student || subject)}</div>
                    <div class="absence-type"><span class="text-danger">❌ Неуважительные (Н):</span> <span class="fw-bold">${absentH}</span></div>
                    <div class="absence-type"><span class="text-warning">⚠️ Уважительные (У):</span> <span class="fw-bold">${absentU}</span></div>
                    <div class="absence-type"><span class="text-success">🏥 Болезнь (Б):</span> <span class="fw-bold">${absentB}</span></div>
                    <div class="absence-total"><span>📊 Всего пропусков:</span> <strong class="text-danger">${total}</strong></div>
                </div>
            `;
        }

        function positionTooltip(event) {
            const mouseX = event.clientX;
            const mouseY = event.clientY;
            const rect = tooltip.getBoundingClientRect();
            const viewportWidth = window.innerWidth;
            const viewportHeight = window.innerHeight;

            // Позиционируем тултип чуть выше курсора
            let left = mouseX + 15;
            let top = mouseY - rect.height - 10;

            // Проверяем выход за правый край
            if (left + rect.width > viewportWidth - 10) {
                left = mouseX - rect.width - 15;
            }
            if (left < 10) left = 10;

            // Проверяем выход за верхний край
            if (top < 10) {
                top = mouseY + 20;
            }

            // Проверяем выход за нижний край
            if (top + rect.height > viewportHeight - 10) {
                top = mouseY - rect.height - 10;
            }

            tooltip.style.left = left + 'px';
            tooltip.style.top = top + 'px';
            tooltip.style.right = 'auto';
            tooltip.style.bottom = 'auto';
        }

        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        const triggers = document.querySelectorAll('.tooltip-trigger');

        triggers.forEach(trigger => {
            trigger.addEventListener('mouseenter', function(e) {
                activeTrigger = this;
                const type = this.getAttribute('data-type');
                let content = '';

                if (type === 'grades') {
                    content = buildGradesContent(this);
                } else if (type === 'absences') {
                    content = buildAbsencesContent(this);
                }

                if (content) {
                    tooltip.innerHTML = content;
                    tooltip.classList.add('show');
                    positionTooltip(e);
                }
            });

            trigger.addEventListener('mousemove', function(e) {
                if (activeTrigger === this && tooltip.classList.contains('show')) {
                    positionTooltip(e);
                }
            });

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

    // Автоматическая отправка формы при изменении фильтров
    function initAutoSubmit() {
        const selects = document.querySelectorAll('#classId, #subjectId, #quarter');

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