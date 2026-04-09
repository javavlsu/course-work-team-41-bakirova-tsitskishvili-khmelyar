/**
 * Журнал - клиентская логика
 */

// Глобальные переменные
let currentGradeModal = null;
let currentLessonModal = null;
let currentCell = null;

// CSRF токен
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');

// Функция для экранирования HTML
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Функция показа уведомлений
function showNotification(msg, type) {
    let container = document.querySelector('.notification-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'notification-container';
        document.body.appendChild(container);
    }

    const notification = document.createElement('div');
    notification.className = `notification notification-${type === 'success' ? 'success' : 'error'}`;
    notification.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'} me-2"></i>${escapeHtml(msg)}`;

    container.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) notification.remove();
        }, 300);
    }, 3000);
}

// Инициализация модальных окон
function initModals() {
    const gradeModalEl = document.getElementById('gradeModal');
    const lessonModalEl = document.getElementById('lessonModal');

    if (gradeModalEl) {
        currentGradeModal = new bootstrap.Modal(gradeModalEl);
        gradeModalEl.addEventListener('hidden.bs.modal', function() {
            resetGradeForm();
        });
    }

    if (lessonModalEl) {
        currentLessonModal = new bootstrap.Modal(lessonModalEl);
    }
}

// Сброс формы оценки
function resetGradeForm() {
    currentCell = null;
    const form = document.getElementById('gradeForm');
    if (form) {
        form.reset();
    }
}

// Обновление ячейки в таблице
function updateGradeCell(cell, gradeValue, attendanceStatus, comment, remark) {
    if (!cell) return;

    let html = '';

    if (attendanceStatus && attendanceStatus !== '') {
        let cls = attendanceStatus === 'Н' ? 'attendance-absent' :
                 (attendanceStatus === 'У' ? 'attendance-excused' : 'attendance-sick');
        html = `<span class="attendance-badge ${cls}">${attendanceStatus}</span>`;
    } else if (gradeValue && gradeValue !== '') {
        let cls = gradeValue === '5' ? 'grade-5' :
                 (gradeValue === '4' ? 'grade-4' :
                  (gradeValue === '3' ? 'grade-3' : 'grade-2'));
        html = `<span class="grade-value ${cls}">${gradeValue}</span>`;
    }

    if (comment && comment !== '' && (!attendanceStatus || attendanceStatus === '')) {
        html += `<i class="fas fa-comment-dots comment-icon" data-comment="${escapeHtml(comment)}" title="Комментарий"></i>`;
    }
    if (remark && remark !== '' && (!attendanceStatus || attendanceStatus === '')) {
        html += `<i class="fas fa-exclamation-triangle comment-icon" style="color:#f59e0b;" data-remark="${escapeHtml(remark)}" title="Замечание: ${escapeHtml(remark)}"></i>`;
    }

    cell.innerHTML = html || '';

    cell.setAttribute('data-grade', gradeValue || '');
    cell.setAttribute('data-attendance', attendanceStatus || '');
    cell.setAttribute('data-comment', comment || '');
    cell.setAttribute('data-remark', remark || '');
}

// Обработка отправки формы оценки
async function handleGradeSubmit(event) {
    event.preventDefault();

    const form = event.target;
    const submitBtn = form.querySelector('#submitGradeBtn');
    if (!submitBtn) return;

    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Сохранение...';
    submitBtn.disabled = true;

    const formData = new FormData(form);
    const attendanceStatus = formData.get('attendanceStatus');

    if (attendanceStatus && attendanceStatus !== '') {
        formData.set('gradeValue', '');
        formData.set('comment', '');
        formData.set('remark', '');
    }

    try {
        const response = await fetch(form.action, {
            method: 'POST',
            headers: { 'X-CSRF-TOKEN': csrfToken },
            body: formData
        });

        const result = await response.json();

        if (result.success && currentCell) {
            updateGradeCell(
                currentCell,
                formData.get('gradeValue'),
                formData.get('attendanceStatus'),
                formData.get('comment'),
                formData.get('remark')
            );
            if (currentGradeModal) currentGradeModal.hide();
            showNotification('Сохранено!', 'success');
        } else if (!result.success) {
            showNotification(result.message || 'Ошибка сохранения', 'error');
        }
    } catch(e) {
        console.error('Ошибка:', e);
        showNotification('Ошибка сети', 'error');
    } finally {
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
}

// Открытие модального окна урока
window.openLessonModal = async function(lessonId) {
    const modalContent = document.getElementById('lessonModalContent');
    if (!modalContent) return;

    modalContent.innerHTML = '<div class="lesson-modal-body text-center py-5"><i class="fas fa-spinner fa-spin fa-3x"></i><p>Загрузка...</p></div>';

    if (currentLessonModal) {
        currentLessonModal.show();
    }

    try {
        const response = await fetch(`/journal/lesson-form?lessonId=${lessonId}`);
        const html = await response.text();
        modalContent.innerHTML = html;

        const lessonForm = document.getElementById('lessonForm');
        if (lessonForm) {
            lessonForm.addEventListener('submit', async function(e) {
                e.preventDefault();
                const formData = new FormData(this);
                const resp = await fetch(this.action, {
                    method: 'POST',
                    headers: { 'X-CSRF-TOKEN': csrfToken },
                    body: formData
                });
                const result = await resp.json();
                if (result.success) {
                    showNotification('Урок сохранен!', 'success');
                    if (currentLessonModal) currentLessonModal.hide();
                    setTimeout(() => location.reload(), 500);
                } else {
                    showNotification(result.message, 'error');
                }
            });
        }
    } catch(e) {
        console.error('Ошибка загрузки:', e);
        showNotification('Ошибка загрузки', 'error');
        modalContent.innerHTML = `
            <div class="lesson-modal-header" style="background: linear-gradient(135deg, #dc2626, #b91c1c);">
                <h5 class="modal-title">Ошибка</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="lesson-modal-body text-center py-5">
                <i class="fas fa-exclamation-triangle text-danger fa-3x mb-3"></i>
                <p>Не удалось загрузить данные урока</p>
                <button class="btn btn-secondary" data-bs-dismiss="modal">Закрыть</button>
            </div>
        `;
    }
};

window.closeLessonModal = function() {
    if (currentLessonModal) {
        currentLessonModal.hide();
    }
};

// Инициализация при загрузке документа
document.addEventListener('DOMContentLoaded', function() {
    initModals();

    // Обработчики для ячеек
    const gradeCells = document.querySelectorAll('.grade-cell');

    gradeCells.forEach(cell => {
        cell.addEventListener('click', function(e) {
            if (e.target.classList.contains('comment-icon')) return;

            currentCell = this;

            const studentName = this.dataset.studentName || '';
            const lessonDate = this.dataset.lessonDate || '';
            const studentId = this.dataset.studentId || '';
            const lessonId = this.dataset.lessonId || '';
            const currentGrade = this.dataset.grade || '';
            const currentAttendance = this.dataset.attendance || '';
            const currentComment = this.dataset.comment || '';
            const currentRemark = this.dataset.remark || '';

            document.getElementById('modalStudentName').textContent = studentName;
            document.getElementById('modalLessonDate').textContent = lessonDate;
            document.getElementById('modalStudentId').value = studentId;
            document.getElementById('modalLessonId').value = lessonId;
            document.getElementById('gradeValue').value = currentGrade;
            document.getElementById('attendanceStatus').value = currentAttendance;
            document.getElementById('comment').value = currentComment;
            document.getElementById('remark').value = currentRemark;

            if (currentGradeModal) {
                currentGradeModal.show();
            }
        });
    });

    // Обработка формы оценки
    const gradeForm = document.getElementById('gradeForm');
    if (gradeForm) {
        gradeForm.addEventListener('submit', handleGradeSubmit);
    }
});

// Тултипы для комментариев
document.addEventListener('mouseover', function(e) {
    const icon = e.target.closest('.comment-icon');
    if (icon) {
        const comment = icon.getAttribute('data-comment');
        const remark = icon.getAttribute('data-remark');
        let title = '';
        if (comment) title += `Комментарий: ${comment}`;
        if (remark) title += (title ? '\n' : '') + `Замечание: ${remark}`;
        if (title) {
            const tooltip = document.createElement('div');
            tooltip.className = 'position-fixed bg-dark text-white p-2 rounded small';
            tooltip.style.zIndex = '9999';
            tooltip.style.maxWidth = '300px';
            tooltip.style.whiteSpace = 'pre-wrap';
            tooltip.textContent = title;
            tooltip.style.left = (e.pageX + 10) + 'px';
            tooltip.style.top = (e.pageY - 30) + 'px';
            document.body.appendChild(tooltip);
            icon.addEventListener('mouseleave', function() {
                if (tooltip.parentNode) tooltip.remove();
            }, { once: true });
        }
    }
});