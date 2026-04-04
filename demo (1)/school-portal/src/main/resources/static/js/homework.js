/**
 * Homework Review Page Scripts
 * Handles filtering, grade saving, answer viewing
 */

// CSRF Token setup
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

// Modal elements
const modal = document.getElementById('answerModal');

/**
 * Show notification message
 * @param {string} message - Message text
 * @param {string} type - 'success' or 'error'
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
 * @param {HTMLElement} button - The save button element
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
        const response = await fetch('/homework/save-review', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({
                homeworkId: parseInt(homeworkId),
                gradeValue: gradeValue,
                comment: comment,
                existingGradeId: existingGradeId
            })
        });

        const result = await response.json();

        if (result.success) {
            // Update grade ID if new grade was created
            if (result.gradeId) {
                gradeIdInput.value = result.gradeId;
            }

            // Update row status
            const row = document.getElementById(`row-${homeworkId}`);
            if (row) {
                row.classList.remove('pending');
                row.classList.add('reviewed');
            }

            // Visual feedback
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
        showNotification('Ошибка сети. Попробуйте позже.', 'error');
    } finally {
        button.innerHTML = originalText;
        button.disabled = false;
    }
}

/**
 * View student's answer in modal
 * @param {HTMLElement} button - The view button element
 */
function viewStudentAnswer(button) {
    const answer = button.getAttribute('data-answer');
    const studentName = button.getAttribute('data-student-name');

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
 * Initialize grade selects with current values
 */
function initGradeSelects() {
    const gradeSelects = document.querySelectorAll('.grade-select');
    gradeSelects.forEach(select => {
        const selectedValue = select.value;
        if (selectedValue) {
            select.style.borderColor = '#10b981';
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
        // Escape key closes modal
        if (e.key === 'Escape' && modal && modal.style.display === 'block') {
            closeModal();
        }
    });
}

/**
 * Handle filter form submit - preserve scroll position
 */
function initFilterHandlers() {
    const filterForm = document.getElementById('filterForm');
    if (filterForm) {
        filterForm.addEventListener('submit', (e) => {
            // Allow normal form submission
            return true;
        });
    }
}

/**
 * Load saved data from localStorage (optional)
 */
function loadSavedDrafts() {
    const commentTextareas = document.querySelectorAll('.comment-textarea');
    commentTextareas.forEach(textarea => {
        const id = textarea.id;
        const savedValue = localStorage.getItem(`homework_comment_${id}`);
        if (savedValue && !textarea.value) {
            textarea.value = savedValue;
        }

        textarea.addEventListener('input', () => {
            localStorage.setItem(`homework_comment_${id}`, textarea.value);
        });
    });
}

/**
 * Clear drafts after successful save
 */
function clearDraft(homeworkId) {
    localStorage.removeItem(`homework_comment_comment-${homeworkId}`);
}

// Add slideOut animation to styles
const style = document.createElement('style');
style.textContent = `
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    loadOriginalComments();
    initGradeSelects();
    initTextareaAutoResize();
    initKeyboardShortcuts();
    initFilterHandlers();

    // Close modal when clicking outside
    if (modal) {
        window.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeModal();
            }
        });
    }
});