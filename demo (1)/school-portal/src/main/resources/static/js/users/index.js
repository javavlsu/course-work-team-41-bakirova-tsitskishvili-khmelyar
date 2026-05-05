// Функции для валидации даты рождения
function validateBirthDate(dateValue) {
    if (!dateValue) return true;
    const today = new Date();
    const minDate = new Date();
    minDate.setFullYear(today.getFullYear() - 4);
    const selectedDate = new Date(dateValue);
    return selectedDate <= minDate;
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
    modal.css('display', 'flex');
    modal.show();
}

function hideModal(modalId) {
    $('#' + modalId).fadeOut(200);
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

$(document).ready(function() {
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    $.ajaxSetup({
        beforeSend: function(xhr, settings) {
            if (['POST', 'PUT', 'DELETE'].includes(settings.type.toUpperCase())) {
                if (csrfToken && csrfHeader) {
                    xhr.setRequestHeader(csrfHeader, csrfToken);
                }
            }
        }
    });

    // Получение деталей пользователя
    $(document).on('click', '.js-details-user', function() {
        const userId = $(this).data('id');
        if (!userId) return;

        $('#userDetailsModal').css('display', 'flex').show();
        $('#userDetailsContent').html('<p style="text-align:center"><i class="fas fa-spinner fa-spin"></i> Загрузка...</p>');

        $.ajax({
            url: '/users/get-user-details',
            type: 'GET',
            data: { id: userId },
            success: function(response) {
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

                    if (user.role === 'Ученик') {
                        html += `<p><strong>Монеты:</strong> ${user.coins || 0}</p>
                                 <p><strong>Класс:</strong> ${escapeHtml(user.className || 'Не указан')}</p>
                                 <p><strong>Классный руководитель:</strong> ${escapeHtml(user.classTeacherName || 'Не указан')}</p>
                                 <p><strong>Родители:</strong> ${escapeHtml(user.parents || 'Не указаны')}</p>`;
                    } else if (user.role === 'Родитель') {
                        html += `<p><strong>Дети:</strong><br> ${escapeHtml(user.students || 'Не указаны')}</p>`;
                    } else if (user.role === 'Учитель') {
                        html += `<p><strong>Классное руководство:</strong> ${escapeHtml(user.supervisedClasses || 'Нет классного руководства')}</p>`;
                    }

                    html += `
                            ${user.info ? `<p><strong>Дополнительная информация:</strong><br>${escapeHtml(user.info)}</p>` : ''}

                            <!-- БЛОК ОТПРАВКИ СООБЩЕНИЯ (КАК В ПРОФИЛЕ) -->
                            <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                            <div class="message-block-admin">
                                <button type="button" id="toggleAdminMsgBtn" class="btn-secondary-text" style="width: 100%; justify-content: center;">
                                    <i class="fas fa-envelope"></i> &nbsp; Написать пользователю
                                </button>
                                <div id="adminMessagePanel" class="message-input-panel" style="display: none; margin-top: 15px;">
                                    <h4 class="message-form-header">Новое сообщение для ${escapeHtml(user.fullName)}</h4>
                                    <form id="adminToUserForm">
                                        <input type="hidden" name="recipientId" value="${user.userId}" />
                                        <textarea id="adminMsgBody" name="body" rows="4" required
                                                  class="form-input-field"
                                                  placeholder="Введите текст сообщения..."></textarea>
                                        <div id="adminMsgResponse" style="display: none; margin-top: 10px;"></div>
                                        <div class="form-actions" style="margin-top: 10px;">
                                            <button type="submit" class="btn-primary-blue" id="submitAdminMsgBtn">Отправить</button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>`;
                    $('#userDetailsContent').html(html);
                } else {
                    $('#userDetailsContent').html(`<p style="color: red;">${escapeHtml(response.message)}</p>`);
                }
            },
            error: function(xhr, status, error) {
                $('#userDetailsContent').html('<p style="color: red;">Ошибка загрузки данных</p>');
            }
        });
    });

    // Редактирование пользователя
    $(document).on('click', '.js-edit-user', function() {
        const userId = $(this).data('id');
        if (!userId) return;

        $('#editUserModal').css('display', 'flex').show();
        $('#editUserContent').html('<p style="text-align:center"><i class="fas fa-spinner fa-spin"></i> Загрузка...</p>');

        $.ajax({
            url: '/users/edit-user-partial',
            type: 'GET',
            data: { id: userId },
            success: function(response) {
                if (response.success) {
                    const data = response.viewModel;
                    let html = `
                        <form id="editUserForm">
                            <input type="hidden" name="userId" value="${data.userId}" />
                            <div class="form-group"><label>Фамилия *</label><input type="text" name="lastName" class="form-control" value="${escapeHtml(data.lastName || '')}" required /></div>
                            <div class="form-group"><label>Имя *</label><input type="text" name="firstName" class="form-control" value="${escapeHtml(data.firstName || '')}" required /></div>
                            <div class="form-group"><label>Отчество</label><input type="text" name="middleName" class="form-control" value="${escapeHtml(data.middleName || '')}" /></div>
                            <div class="form-group"><label>Email</label><input type="email" name="email" class="form-control" value="${escapeHtml(data.email || '')}" /></div>
                            <div class="form-group"><label>Телефон</label><input type="tel" name="phone" class="form-control" value="${escapeHtml(data.phone || '')}" /></div>
                            <div class="form-group">
                                <label>Дата рождения</label>
                                <input type="date" name="birthDate" id="birthDateInput" class="form-control" value="${data.birthDate || ''}" />
                                <span id="birthDateError" class="error-message" style="display: none;"></span>
                            </div>
                            <div class="form-group"><label>Дополнительная информация</label><textarea name="info" class="form-control" rows="3">${escapeHtml(data.info || '')}</textarea></div>
                        </form>`;
                    $('#editUserContent').html(html);
                    $('#birthDateInput').on('change', function() { showBirthDateError(!validateBirthDate($(this).val())); });
                }
            }
        });
    });

    // Сохранение редактирования
    $(document).on('click', '#saveEditUser', function() {
        const birthDate = $('#birthDateInput').val();
        if (birthDate && !validateBirthDate(birthDate)) {
            showBirthDateError(true);
            return;
        }
        $.ajax({
            url: '/users/edit-user',
            type: 'POST',
            data: $('#editUserForm').serialize(),
            success: function(response) {
                if (response.success) { location.reload(); } else { alert(response.message); }
            }
        });
    });

    // Удаление пользователя
    $(document).on('click', '.js-delete-user', function() {
        const userId = $(this).data('id');
        const userName = $(this).data('name');
        if (confirm(`Вы уверены, что хотите удалить пользователя ${userName}?`)) {
            $.ajax({
                url: '/users/delete',
                type: 'POST',
                data: { id: userId },
                success: function(response) { if (response.success) location.reload(); }
            });
        }
    });

    // --- НОВЫЕ ОБРАБОТЧИКИ ДЛЯ СООБЩЕНИЙ ---
    $(document).on('click', '#toggleAdminMsgBtn', function() {
        $('#adminMessagePanel').slideToggle(300);
    });

    $(document).on('submit', '#adminToUserForm', function(e) {
        e.preventDefault();
        const submitBtn = $('#submitAdminMsgBtn');
        const resp = $('#adminMsgResponse');

        submitBtn.prop('disabled', true).text('Отправка...');
        resp.hide().removeClass('response-success response-error');

        $.ajax({
            url: '/users/send-message', // URL вашего метода в контроллере Java
            type: 'POST',
            data: $(this).serialize(),
            success: function(data) {
                if (data.success) {
                    resp.addClass('response-success').html(data.message).show();
                    $('#adminMsgBody').val('');
                    setTimeout(() => { $('#adminMessagePanel').slideUp(300); }, 2000);
                } else {
                    resp.addClass('response-error').html(data.message).show();
                }
            },
            error: function() {
                resp.addClass('response-error').html('Ошибка связи с сервером').show();
            },
            complete: function() {
                submitBtn.prop('disabled', false).text('Отправить');
            }
        });
    });

    // Закрытие модальных окон
    $(document).on('click', '#closeDetailsModal, #closeEditModal', function() {
        $(this).closest('.modal-overlay').fadeOut(200);
    });

    $(document).on('click', '.modal-overlay', function(e) {
        if (e.target === this) $(this).fadeOut(200);
    });

    $(document).on('keydown', function(e) {
        if (e.key === 'Escape') $('.modal-overlay:visible').fadeOut(200);
    });
});