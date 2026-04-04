// Функции для валидации даты рождения
function validateBirthDate(dateValue) {
    if (!dateValue) return true;

    const today = new Date();
    const minDate = new Date();
    minDate.setFullYear(today.getFullYear() - 4);

    const selectedDate = new Date(dateValue);

    if (selectedDate > minDate) {
        return false;
    }
    return true;
}

function showBirthDateError(show) {
    if (show) {
        $('#birthDateInput').addClass('error-input');
        $('#birthDateError').text('Дата рождения не может быть позже чем 4 года назад').show();
    } else {
        $('#birthDateInput').removeClass('error-input');
        $('#birthDateError').hide();
    }
}

function showModal(modalId) {
    var modal = $('#' + modalId);
    console.log("Показываем модальное окно:", modalId);
    modal.css('display', 'flex');
    modal.show();
}

function hideModal(modalId) {
    $('#' + modalId).fadeOut(200);
}

// Функция для экранирования HTML
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

$(document).ready(function() {
    // Получаем CSRF токен
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    // Настраиваем AJAX для всех запросов
    $.ajaxSetup({
        beforeSend: function(xhr, settings) {
            if (settings.type === 'POST' || settings.type === 'PUT' || settings.type === 'DELETE') {
                if (csrfToken && csrfHeader) {
                    xhr.setRequestHeader(csrfHeader, csrfToken);
                }
            }
        }
    });

    // Получение деталей пользователя
    $(document).on('click', '.js-details-user', function() {
        const userId = $(this).data('id');
        console.log("Нажата кнопка Подробнее, userId:", userId);

        if (!userId) {
            console.error("userId не найден!");
            return;
        }

        $('#userDetailsModal').css('display', 'flex').show();
        console.log("Открываем модальное окно userDetailsModal");
        $('#userDetailsContent').html('<p style="text-align:center"><i class="fas fa-spinner fa-spin"></i> Загрузка...</p>');

        $.ajax({
            url: '/users/get-user-details',
            type: 'GET',
            data: { id: userId },
            success: function(response) {
                console.log("Ответ от сервера:", response);
                if (response.success) {
                    const user = response.user;

                    let html = `
                        <div>
                            <h4 style="margin-bottom: 15px;">${escapeHtml(user.fullName)}</h4>
                            <p><strong>Роль:</strong> ${escapeHtml(user.role)}</p>
                            ${window.showLogin ? `<p><strong>Логин:</strong> ${escapeHtml(user.username)}</p>` : ''}
                            <p><strong>Email:</strong> ${escapeHtml(user.email || 'Не указан')}</p>
                            <p><strong>Телефон:</strong> ${escapeHtml(user.phone || 'Не указан')}</p>
                            <p><strong>Дата рождения:</strong> ${escapeHtml(user.birthDate || 'Не указана')}</p>`;

                    // Монеты показываем только для учеников
                    if (user.role === 'Ученик') {
                        html += `<p><strong>Монеты:</strong> ${user.coins || 0}</p>`;
                    }

                    // Дополнительная информация для ученика
                    if (user.role === 'Ученик') {
                        html += `
                            <p><strong>Класс:</strong> ${escapeHtml(user.className || 'Не указан')}</p>
                            <p><strong>Классный руководитель:</strong> ${escapeHtml(user.classTeacherName || 'Не указан')}</p>
                            <p><strong>Родители:</strong> ${escapeHtml(user.parents || 'Не указаны')}</p>`;
                    }

                    // Дополнительная информация для родителя
                    else if (user.role === 'Родитель') {
                        html += `<p><strong>Дети:</strong><br> ${escapeHtml(user.students || 'Не указаны')}</p>`;
                    }

                    // Дополнительная информация для учителя
                    else if (user.role === 'Учитель') {
                        html += `<p><strong>Классное руководство:</strong> ${escapeHtml(user.supervisedClasses || 'Нет классного руководства')}</p>`;
                    }

                    html += `
                            ${user.info ? `<p><strong>Дополнительная информация:</strong><br>${escapeHtml(user.info)}</p>` : ''}
                        </div>
                    `;
                    $('#userDetailsContent').html(html);
                } else {
                    $('#userDetailsContent').html(`<p style="color: red;">${escapeHtml(response.message)}</p>`);
                }
            },
            error: function(xhr, status, error) {
                console.error("Ошибка AJAX:", status, error);
                console.error("Ответ:", xhr.responseText);
                $('#userDetailsContent').html('<p style="color: red;">Ошибка загрузки данных: ' + error + '</p>');
            }
        });
    });

    // Редактирование пользователя
    $(document).on('click', '.js-edit-user', function() {
        const userId = $(this).data('id');
        console.log("Нажата кнопка Редактировать, userId:", userId);

        if (!userId) {
            console.error("userId не найден!");
            return;
        }

        $('#editUserModal').css('display', 'flex').show();
        $('#editUserContent').html('<p style="text-align:center"><i class="fas fa-spinner fa-spin"></i> Загрузка...</p>');

        $.ajax({
            url: '/users/edit-user-partial',
            type: 'GET',
            data: { id: userId },
            success: function(response) {
                console.log("Ответ от сервера (редактирование):", response);
                if (response.success) {
                    const data = response.viewModel;
                    console.log("Данные для формы:", data);

                    let html = `
                        <form id="editUserForm">
                            <input type="hidden" name="userId" value="${data.userId}" />
                            <div class="form-group">
                                <label>Фамилия *</label>
                                <input type="text" name="lastName" class="form-control" value="${escapeHtml(data.lastName || '')}" required />
                            </div>
                            <div class="form-group">
                                <label>Имя *</label>
                                <input type="text" name="firstName" class="form-control" value="${escapeHtml(data.firstName || '')}" required />
                            </div>
                            <div class="form-group">
                                <label>Отчество</label>
                                <input type="text" name="middleName" class="form-control" value="${escapeHtml(data.middleName || '')}" />
                            </div>
                            <div class="form-group">
                                <label>Email</label>
                                <input type="email" name="email" class="form-control" value="${escapeHtml(data.email || '')}" />
                            </div>
                            <div class="form-group">
                                <label>Телефон</label>
                                <input type="tel" name="phone" class="form-control" value="${escapeHtml(data.phone || '')}" />
                            </div>
                            <div class="form-group">
                                <label>Дата рождения</label>
                                <input type="date" name="birthDate" id="birthDateInput" class="form-control" value="${data.birthDate || ''}" />
                                <span id="birthDateError" class="error-message" style="display: none;"></span>
                            </div>
                            <div class="form-group">
                                <label>Дополнительная информация</label>
                                <textarea name="info" class="form-control" rows="3">${escapeHtml(data.info || '')}</textarea>
                            </div>
                        </form>
                    `;
                    $('#editUserContent').html(html);

                    // Добавляем обработчик валидации даты
                    $('#birthDateInput').off('change').on('change', function() {
                        const isValid = validateBirthDate($(this).val());
                        showBirthDateError(!isValid);
                    });
                } else {
                    $('#editUserContent').html(`<p style="color: red;">Ошибка: ${escapeHtml(response.message)}</p>`);
                }
            },
            error: function(xhr, status, error) {
                console.error("Ошибка AJAX (редактирование):", status, error);
                console.error("Ответ сервера:", xhr.responseText);
                $('#editUserContent').html('<p style="color: red;">Ошибка загрузки формы: ' + error + '</p>');
            }
        });
    });

    // Сохранение редактирования
    $(document).on('click', '#saveEditUser', function() {
        // Проверка даты рождения
        const birthDate = $('#birthDateInput').val();
        if (birthDate && !validateBirthDate(birthDate)) {
            showBirthDateError(true);
            alert('Дата рождения не может быть позже чем 4 года назад');
            return;
        }

        const formData = $('#editUserForm').serialize();
        console.log("Сохранение данных:", formData);

        $.ajax({
            url: '/users/edit-user',
            type: 'POST',
            data: formData,
            success: function(response) {
                console.log("Ответ на сохранение:", response);
                if (response.success) {
                    alert(response.message);
                    hideModal('editUserModal');
                    location.reload();
                } else {
                    alert('Ошибка: ' + response.message);
                }
            },
            error: function(xhr, status, error) {
                console.error("Ошибка сохранения:", status, error);
                alert('Ошибка сети: ' + error);
            }
        });
    });

    // Удаление пользователя
    $(document).on('click', '.js-delete-user', function() {
        const userId = $(this).data('id');
        const userName = $(this).data('name');

        if (confirm(`Вы уверены, что хотите удалить пользователя ${userName}?`)) {
            console.log("Удаление пользователя ID:", userId);
            $.ajax({
                url: '/users/delete',
                type: 'POST',
                data: { id: userId },
                success: function(response) {
                    console.log("Ответ на удаление:", response);
                    if (response.success) {
                        alert(response.message);
                        location.reload();
                    } else {
                        alert('Ошибка: ' + response.message);
                    }
                },
                error: function(xhr, status, error) {
                    console.error("Ошибка удаления:", status, error);
                    alert('Ошибка сети: ' + error);
                }
            });
        }
    });

    // Закрытие модальных окон
    $(document).on('click', '#closeDetailsModal, #closeEditModal', function() {
        const modal = $(this).closest('.modal-overlay');
        modal.fadeOut(200);
    });

    // Закрытие по клику вне окна
    $(document).on('click', '.modal-overlay', function(e) {
        if (e.target === this) {
            $(this).fadeOut(200);
        }
    });

    // Закрытие по клавише ESC
    $(document).on('keydown', function(e) {
        if (e.key === 'Escape') {
            $('.modal-overlay:visible').fadeOut(200);
        }
    });
});