async function saveReview(button) {
    const homeworkId = button.getAttribute('data-id');
    const gradeSelect = document.getElementById(`grade-${homeworkId}`);
    const commentTextarea = document.getElementById(`comment-${homeworkId}`);
    const gradeIdInput = document.getElementById(`gradeid-${homeworkId}`);

    const gradeValue = gradeSelect ? (gradeSelect.value === '' ? null : parseInt(gradeSelect.value)) : null;
    const comment = commentTextarea ? commentTextarea.value : '';
    const existingGradeId = gradeIdInput ? (gradeIdInput.value === '' ? null : parseInt(gradeIdInput.value)) : null;

    const originalText = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Сохранение...';
    button.disabled = true;

    try {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        const response = await fetch('/homework/save-review', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
            },
            body: JSON.stringify({
                homeworkId: parseInt(homeworkId),
                gradeValue: gradeValue,
                comment: comment,
                existingGradeId: existingGradeId
            })
        });

        if (!response.ok) {
            if (response.status === 403) throw new Error('Ошибка безопасности. Обновите страницу (F5).');
            throw new Error(`HTTP ${response.status}`);
        }

        const result = await response.json();

        if (result.success) {
            if (result.gradeId && gradeIdInput) gradeIdInput.value = result.gradeId;
            button.style.backgroundColor = '#10b981';
            button.textContent = '✓ Сохранено';
            setTimeout(() => location.reload(), 1500);
        } else {
            alert('Ошибка: ' + result.message);
            button.innerHTML = originalText;
            button.disabled = false;
        }
    } catch (error) {
        alert('Ошибка: ' + error.message);
        button.innerHTML = originalText;
        button.disabled = false;
    }
}

function viewStudentAnswer(button) {
    const answer = button.getAttribute('data-answer');
    document.getElementById('modalAnswerText').textContent = answer || 'Ответ не указан';
    document.getElementById('answerModal').style.display = 'block';
}

function closeModal() {
    document.getElementById('answerModal').style.display = 'none';
}

document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('answerModal');
    if (modal) {
        window.addEventListener('click', (e) => { if (e.target === modal) closeModal(); });
    }
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeModal(); });
});