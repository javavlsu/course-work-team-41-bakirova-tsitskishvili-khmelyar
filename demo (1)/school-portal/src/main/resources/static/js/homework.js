/**
 * Homework Review Page Scripts
 */

// Получаем CSRF токен из мета-тегов
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

/**
 * Show notification message
 */
function showNotification(message, type) {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}"></i>
        <span>${message}</span>
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

/**
 * Save review (grade and comment)
 */
async function saveReview(button) {
    const homeworkId = button.getAttribute('data-id');
    const gradeSelect = document.getElementById(`grade-${homeworkId}`);
    const commentTextarea = document.getElementById(`comment-${homeworkId}`);
    const gradeIdInput = document.getElementById(`gradeid-${homeworkId}`);

    const gradeValue = gradeSelect.value === '' ? null : parseInt(gradeSelect.value);
    const comment = commentTextarea.value;
    const existingGradeId = gradeIdInput.value === '' ? null : parseInt(gradeIdInput.value);

    // Disable button and show loading state
    const originalText = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Сохранение...';
    button.disabled = true;

    try {
        const headers = {
            'Content-Type': 'application/json'
        };

        // Добавляем CSRF заголовок, если он есть
        if (csrfHeader && csrfToken) {
            headers[csrfHeader] = csrfToken;
        }

        const response = await fetch('/homework/save-review', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                homeworkId: parseInt(homeworkId),
                gradeValue: gradeValue,
                comment: comment,
                existingGradeId: existingGradeId
            })
        });

        if (!response.ok) {
            if (response.status === 403) {
                throw new Error('Ошибка безопасности. Пожалуйста, обновите страницу.');
            }
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const result = await response.json();

        if (result.success) {
            if (result.gradeId) {
                gradeIdInput.value = result.gradeId;
                updateTabCounts();
            }
        function updateTabCounts() {
            // Перезагружаем страницу для обновления счетчиков
            // Или можно сделать AJAX запрос для получения новых чисел
            setTimeout(() => {
                location.reload();
            }, 1000);
        }

            const row = document.getElementById(`row-${homeworkId}`);
            if (row) {
                row.classList.remove('pending');
                row.classList.add('reviewed');
            }

            button.style.backgroundColor = '#10b981';
            setTimeout(() => {
                button.style.backgroundColor = '';
            }, 500);

            showNotification('Оценка и комментарий сохранены!', 'success');
        } else {
            showNotification('Ошибка: ' + result.message, 'error');
        }
    } catch (error) {
        console.error('Save error:', error);
        showNotification('Ошибка: ' + error.message, 'error');
    } finally {
        button.innerHTML = originalText;
        button.disabled = false;
    }
}

/**
 * View student's answer in modal
 */
function viewStudentAnswer(button) {
    const answer = button.getAttribute('data-answer');
    const studentName = button.getAttribute('data-student-name');

    const modal = document.getElementById('answerModal');
    if (!modal) return;

    document.getElementById('modalStudentName').textContent = studentName || 'Ученик';
    document.getElementById('modalAnswerText').textContent = answer || 'Ответ не указан';

    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
}

/**
 * Close modal
 */
function closeModal() {
    const modal = document.getElementById('answerModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }
}

/**
 * Load comments from original value on page load
 */
function loadOriginalComments() {
    const commentTextareas = document.querySelectorAll('.comment-textarea');
    commentTextareas.forEach(textarea => {
        const id = textarea.id.replace('comment-', '');
        const originalInput = document.getElementById(`originalcomment-${id}`);
        if (originalInput && originalInput.value) {
            textarea.value = originalInput.value;
        }
    });
}

/**
 * Auto-resize textareas
 */
function initTextareaAutoResize() {
    const textareas = document.querySelectorAll('.comment-textarea');
    textareas.forEach(textarea => {
        textarea.addEventListener('input', function() {
            this.style.height = 'auto';
            this.style.height = Math.min(this.scrollHeight, 100) + 'px';
        });
    });
}

/**
 * Keyboard shortcuts
 */
function initKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeModal();
        }
    });
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    loadOriginalComments();
    initTextareaAutoResize();
    initKeyboardShortcuts();

    // Close modal when clicking outside
    const modal = document.getElementById('answerModal');
    if (modal) {
        window.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeModal();
            }
        });
    }
    /**
     * View student's answer in modal
     */
    function viewStudentAnswer(answer, studentName) {
        const modal = document.getElementById('answerModal');
        if (!modal) return;

        document.getElementById('modalStudentName').textContent = studentName || 'Ученик';
        document.getElementById('modalAnswerText').textContent = answer || 'Ответ не указан';

        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
});