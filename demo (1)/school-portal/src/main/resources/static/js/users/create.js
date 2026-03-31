// Функция для валидации даты рождения
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

// Функция для показа ошибки даты
function showBirthDateError(show) {
    if (show) {
        $('#birthDate').addClass('error-input');
        $('#birthDateError').text('Дата рождения не может быть позже чем 4 года назад').show();
    } else {
        $('#birthDate').removeClass('error-input');
        $('#birthDateError').hide();
    }
}

function handleRoleChange() {
    const roleSelect = $('#roleId');
    const selectedRoleText = roleSelect.find('option:selected').text();
    const dynamicFields = $('#dynamicFields');

    dynamicFields.empty();

    if (selectedRoleText === 'STUDENT' || selectedRoleText === 'Ученик') {
        let options = '<option value="">-- Выберите класс --</option>';
        if (typeof schoolClasses !== 'undefined' && schoolClasses.length > 0) {
            schoolClasses.forEach(function(item) {
                options += `<option value="${item.key}">${item.value}</option>`;
            });
        }
        dynamicFields.html(`
            <div class="col-span-2">
                <label for="classId" class="form-label">Класс ученика *</label>
                <select id="classId" name="classId" class="form-select-field" required>
                    ${options}
                </select>
            </div>
        `);
    } else if (selectedRoleText === 'PARENT' || selectedRoleText === 'Родитель') {
        let options = '<option value="">-- Выберите ученика --</option>';
        if (typeof allStudents !== 'undefined' && allStudents.length > 0) {
            allStudents.forEach(function(item) {
                options += `<option value="${item.key}">${item.value}</option>`;
            });
        }
        dynamicFields.html(`
            <div class="col-span-2">
                <label for="studentIdForParent" class="form-label">Привязать к ученику *</label>
                <select id="studentIdForParent" name="studentIdForParent" class="form-select-field" required>
                    ${options}
                </select>
            </div>
        `);
    }
}

$(document).ready(function() {
    // Переключение панели управления классами
    $('#toggleClassPanelBtn').click(function() {
        const panel = $('#classPanel');
        if (panel.hasClass('show')) {
            panel.removeClass('show');
        } else {
            // Закрываем панель предметов, если открыта
            $('#subjectPanel').removeClass('show');
            panel.addClass('show');
            // Загружаем данные, если панель пустая
            if ($('#classManagementContent').html().trim() === '<p>Загрузка данных...</p>') {
                loadClassesData();
            }
        }
    });

    // Закрытие панели управления классами
    $('#closeClassPanelBtn').click(function() {
        $('#classPanel').removeClass('show');
    });

    // Переключение панели управления предметами
    $('#toggleSubjectPanelBtn').click(function() {
        const panel = $('#subjectPanel');
        if (panel.hasClass('show')) {
            panel.removeClass('show');
        } else {
            // Закрываем панель классов, если открыта
            $('#classPanel').removeClass('show');
            panel.addClass('show');
            // Загружаем данные, если панель пустая
            if ($('#subjectManagementContent').html().trim() === '<p>Загрузка данных...</p>') {
                loadSubjectsData();
            }
        }
    });

    // Закрытие панели управления предметами
    $('#closeSubjectPanelBtn').click(function() {
        $('#subjectPanel').removeClass('show');
    });

    // Функция загрузки данных классов
    function loadClassesData() {
        $.ajax({
            url: '/users/manage-classes-partial',
            method: 'GET',
            success: function(data) {
                renderClassesList(data);
            },
            error: function() {
                $('#classManagementContent').html('<p class="text-danger">Ошибка загрузки данных</p>');
            }
        });
    }

    // Функция загрузки данных предметов
    function loadSubjectsData() {
        $.ajax({
            url: '/users/manage-subjects-partial',
            method: 'GET',
            success: function(data) {
                renderSubjectsList(data);
            },
            error: function() {
                $('#subjectManagementContent').html('<p class="text-danger">Ошибка загрузки данных</p>');
            }
        });
    }

    // Функция отрисовки списка классов - с выделением классов без классного руководителя
    function renderClassesList(response) {
        let html = '<div class="classes-list">';

        // Форма добавления нового класса
        html += `
            <div class="add-class-form">
                <h5><i class="fas fa-plus-circle"></i> Добавить новый класс</h5>
                <form id="addClassForm">
                    <div class="form-group-small">
                        <label>Номер класса *</label>
                        <input type="number" id="classNumber" name="classNumber" placeholder="Например: 5" required min="1" max="11">
                    </div>
                    <div class="form-group-small">
                        <label>Буква класса *</label>
                        <input type="text" id="classLetter" name="classLetter" placeholder="Например: А" required maxlength="1">
                    </div>
                    <div class="form-group-small">
                        <label>Классный руководитель</label>
                        <select id="classTeacherId" name="teacherId">
                            <option value="">-- Не выбран --</option>
                        </select>
                    </div>
                    <div class="form-actions-small">
                        <button type="button" id="cancelAddClass" class="btn-sm btn-cancel-red">Отмена</button>
                        <button type="submit" class="btn-sm btn-primary-blue">Создать класс</button>
                    </div>
                </form>
            </div>
        `;

        // Отображаем список классов
        if (response.schoolClasses && response.schoolClasses.length > 0) {
            response.schoolClasses.forEach(function(cls) {
                // Проверяем, есть ли классный руководитель
                const hasTeacher = cls.classTeacherName && cls.classTeacherName !== 'Не назначен';
                // Добавляем специальный класс для выделения, если нет классного руководителя
                const warningClass = !hasTeacher ? 'class-list-item-warning' : '';

                // Формируем текст классного руководителя с возможным выделением
                const teacherText = cls.classTeacherName || 'Не назначен';
                const teacherHtml = !hasTeacher ?
                    `<span class="teacher-urgent">${teacherText}</span>` :
                    teacherText;

                html += `
                    <div class="class-list-item ${warningClass}" data-class-id="${cls.classId}">
                        <div class="class-name">${cls.className}</div>
                        <div class="class-teacher">
                            <i class="fas fa-chalkboard-user"></i>
                            Классный руководитель: ${teacherHtml}
                        </div>
                        <div class="class-student-count">
                            <i class="fas fa-users"></i>
                            Учеников: ${cls.studentCount || 0}
                        </div>
                        <div class="class-actions">
                            <button class="btn-icon btn-edit-class" onclick="editClass(${cls.classId})">
                                <i class="fas fa-edit"></i> Редактировать
                            </button>
                            <button class="btn-icon btn-delete-class" onclick="deleteClass(${cls.classId})" ${!cls.canDelete ? 'disabled style="opacity:0.5;"' : ''}>
                                <i class="fas fa-trash"></i> Удалить
                            </button>
                        </div>
                    </div>
                `;
            });
        } else {
            html += '<p class="text-muted text-center">Нет созданных классов</p>';
        }

        html += '</div>';
        $('#classManagementContent').html(html);

        // Загрузка списка учителей для выбора
        if (response.availableTeachers && response.availableTeachers.length > 0) {
            let options = '<option value="">-- Не выбран --</option>';
            response.availableTeachers.forEach(function(teacher) {
                options += `<option value="${teacher.value}">${teacher.text}</option>`;
            });
            $('#classTeacherId').html(options);
        } else {
            $('#classTeacherId').html('<option value="">-- Нет доступных учителей --</option>');
        }

        // Обработчик формы добавления класса
        $('#addClassForm').off('submit').on('submit', function(e) {
            e.preventDefault();
            addNewClass();
        });

        $('#cancelAddClass').click(function() {
            $('#addClassForm')[0].reset();
        });
    }

    // Функция загрузки учителей для выбора
    function loadTeachersForSelect() {
        $.ajax({
            url: '/api/teachers/list',
            method: 'GET',
            success: function(teachers) {
                let options = '<option value="">-- Не выбран --</option>';
                teachers.forEach(function(teacher) {
                    options += `<option value="${teacher.id}">${teacher.fullName}</option>`;
                });
                $('#classTeacherId').html(options);
            }
        });
    }

    // Функция добавления нового класса
    function addNewClass() {
        const classData = {
            classNumber: $('#classNumber').val(),
            classLetter: $('#classLetter').val(),
            classTeacherId: $('#classTeacherId').val() || null
        };

        $.ajax({
            url: '/users/add-class',
            method: 'POST',
            data: classData,
            headers: {
                'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')
            },
            success: function(response) {
                if (response.success) {
                    if (response.warning) {
                        showMessage(response.message, 'warning');
                    } else {
                        showMessage(response.message, 'success');
                    }
                    loadClassesData(); // Перезагружаем список
                    $('#addClassForm')[0].reset();
                } else {
                    showMessage(response.message, 'error');
                }
            },
            error: function() {
                showMessage('Ошибка создания класса', 'error');
            }
        });
    }


    // Функция удаления класса с подтверждением
    window.deleteClass = function(classId) {
        $.ajax({
            url: '/users/delete-class',
            method: 'POST',
            data: { classId: classId },
            headers: {
                'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')
            },
            success: function(response) {
                if (response.success) {
                    showMessage(response.message, 'success');
                    loadClassesData();
                } else if (response.warning) {
                    // Показываем предупреждение с подтверждением
                    if (confirm(response.message + '\n\nВы уверены, что хотите удалить класс?')) {
                        // Повторный запрос с подтверждением
                        $.ajax({
                            url: '/users/delete-class-force',
                            method: 'POST',
                            data: { classId: classId, force: true },
                            headers: {
                                'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')
                            },
                            success: function(forceResponse) {
                                if (forceResponse.success) {
                                    showMessage(forceResponse.message, 'success');
                                    loadClassesData();
                                } else {
                                    showMessage(forceResponse.message, 'error');
                                }
                            },
                            error: function() {
                                showMessage('Ошибка при удалении класса', 'error');
                            }
                        });
                    }
                } else {
                    showMessage(response.message, 'error');
                }
            },
            error: function() {
                showMessage('Ошибка при удалении класса', 'error');
            }
        });
    };

    // Функция редактирования класса
    window.editClass = function(classId) {
        // Получаем данные класса из текущего списка, который уже загружен
        $.ajax({
            url: '/users/get-class-details',
            method: 'GET',
            data: { classId: classId },
            success: function(response) {
                if (response.success) {
                    showEditClassForm(response.classData);
                } else {
                    showMessage('Ошибка загрузки данных класса: ' + response.message, 'error');
                }
            },
            error: function() {
                showMessage('Ошибка загрузки данных класса', 'error');
            }
        });
    };

    // Показ формы редактирования класса
    function showEditClassForm(classData) {
        // Загружаем список учителей
        $.ajax({
            url: '/users/manage-classes-partial',
            method: 'GET',
            success: function(response) {
                if (response.success) {
                    // Удаляем предыдущее модальное окно, если есть
                    if ($('#editClassModal').length) {
                        $('#editClassModal').remove();
                    }

                    // Создаем модальную форму редактирования
                    const editFormHtml = `
                        <div id="editClassModal" class="modal-overlay" style="display: flex; z-index: 10000;">
                            <div class="modal-content-form" style="max-width: 500px; position: relative; z-index: 10001;">
                                <div class="modal-form-header">
                                    <i class="fas fa-edit"></i>
                                    Редактирование класса: ${classData.className}
                                </div>
                                <div class="modal-form-body">
                                    <form id="editClassForm">
                                        <input type="hidden" id="editClassId" value="${classData.classId}">

                                        <div class="form-group">
                                            <label>Номер класса *</label>
                                            <input type="number" id="editClassNumber" class="form-control"
                                                   value="${classData.classNumber}" required min="1" max="11">
                                        </div>

                                        <div class="form-group">
                                            <label>Буква класса *</label>
                                            <input type="text" id="editClassLetter" class="form-control"
                                                   value="${classData.classLetter}" required maxlength="1"
                                                   style="text-transform: uppercase;">
                                        </div>

                                        <div class="form-group">
                                            <label>Классный руководитель</label>
                                            <select id="editClassTeacherId" class="form-control">
                                                <option value="">-- Не выбран --</option>
                                                ${generateTeacherOptions(response.availableTeachers, classData.classTeacherId)}
                                            </select>
                                        </div>
                                    </form>
                                </div>
                                <div class="modal-form-actions">
                                    <button type="button" class="btn-secondary-text" id="closeEditModalBtn">Отмена</button>
                                    <button type="button" class="btn-primary-blue" id="saveEditModalBtn">Сохранить</button>
                                </div>
                            </div>
                        </div>
                    `;

                    $('body').append(editFormHtml);

                    // Привязываем обработчики после добавления в DOM
                    $('#closeEditModalBtn').off('click').on('click', function() {
                        closeEditClassModal();
                    });

                    $('#saveEditModalBtn').off('click').on('click', function() {
                        submitEditClass();
                    });

                    // Закрытие по клику на оверлей
                    $('#editClassModal').off('click').on('click', function(e) {
                        if (e.target === this) {
                            closeEditClassModal();
                        }
                    });

                } else {
                    showMessage('Ошибка загрузки списка учителей', 'error');
                }
            },
            error: function() {
                showMessage('Ошибка загрузки списка учителей', 'error');
            }
        });
    }

    // Генерация опций для выбора учителя
    function generateTeacherOptions(teachers, selectedTeacherId) {
        let options = '';
        teachers.forEach(function(teacher) {
            const selected = (teacher.value == selectedTeacherId) ? 'selected' : '';
            options += `<option value="${teacher.value}" ${selected}>${teacher.text}</option>`;
        });
        return options;
    }

    // Отправка данных редактирования класса
    function submitEditClass() {
        // Проверяем, что форма существует
        if (!$('#editClassId').length) {
            showMessage('Форма редактирования не найдена', 'error');
            return;
        }

        const classData = {
            classId: $('#editClassId').val(),
            classNumber: $('#editClassNumber').val(),
            classLetter: $('#editClassLetter').val(),
            classTeacherId: $('#editClassTeacherId').val() || null
        };

        // Валидация
        if (!classData.classNumber || !classData.classLetter) {
            showMessage('Пожалуйста, заполните номер и букву класса', 'error');
            return;
        }

        // Показываем индикатор загрузки
        const saveBtn = $('#saveEditModalBtn');
        const originalText = saveBtn.html();
        saveBtn.html('<i class="fas fa-spinner fa-spin"></i> Сохранение...').prop('disabled', true);

        $.ajax({
            url: '/users/edit-class',
            method: 'POST',
            data: classData,
            headers: {
                'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')
            },
            success: function(response) {
                if (response.success) {
                    showMessage(response.message, 'success');
                    closeEditClassModal();
                    loadClassesData(); // Перезагружаем список классов
                } else {
                    showMessage(response.message, 'error');
                    saveBtn.html(originalText).prop('disabled', false);
                }
            },
            error: function(xhr) {
                let errorMsg = 'Ошибка при сохранении изменений';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMsg = xhr.responseJSON.message;
                }
                showMessage(errorMsg, 'error');
                saveBtn.html(originalText).prop('disabled', false);
            }
        });
    }
    // Функция редактирования предмета
    window.editSubject = function(subjectId) {
        // Получаем данные предмета
        $.ajax({
            url: '/users/get-subject-details',
            method: 'GET',
            data: { subjectId: subjectId },
            success: function(response) {
                if (response.success) {
                    showEditSubjectForm(response.subjectData);
                } else {
                    showMessage('Ошибка загрузки данных предмета: ' + response.message, 'error');
                }
            },
            error: function() {
                showMessage('Ошибка загрузки данных предмета', 'error');
            }
        });
    };

    // Показ формы редактирования предмета
    function showEditSubjectForm(subjectData) {
        // Удаляем предыдущее модальное окно, если есть
        if ($('#editSubjectModal').length) {
            $('#editSubjectModal').remove();
        }

        // Создаем модальную форму редактирования
        const editFormHtml = `
            <div id="editSubjectModal" class="modal-overlay" style="display: flex; z-index: 10000;">
                <div class="modal-content-form" style="max-width: 500px; position: relative; z-index: 10001;">
                    <div class="modal-form-header">
                        <i class="fas fa-edit"></i>
                        Редактирование предмета
                    </div>
                    <div class="modal-form-body">
                        <form id="editSubjectForm">
                            <input type="hidden" id="editSubjectId" value="${subjectData.subjectId}">

                            <div class="form-group">
                                <label>Название предмета *</label>
                                <input type="text" id="editSubjectName" class="form-control"
                                       value="${escapeHtml(subjectData.subjectName)}"
                                       required placeholder="Например: Математика">
                            </div>
                        </form>
                    </div>
                    <div class="modal-form-actions">
                        <button type="button" class="btn-secondary-text" id="closeEditSubjectModalBtn">Отмена</button>
                        <button type="button" class="btn-primary-blue" id="saveEditSubjectModalBtn">Сохранить</button>
                    </div>
                </div>
            </div>
        `;

        $('body').append(editFormHtml);

        // Привязываем обработчики после добавления в DOM
        $('#closeEditSubjectModalBtn').off('click').on('click', function() {
            closeEditSubjectModal();
        });

        $('#saveEditSubjectModalBtn').off('click').on('click', function() {
            submitEditSubject();
        });

        // Закрытие по клику на оверлей
        $('#editSubjectModal').off('click').on('click', function(e) {
            if (e.target === this) {
                closeEditSubjectModal();
            }
        });

        // Отправка по нажатию Enter
        $('#editSubjectName').off('keypress').on('keypress', function(e) {
            if (e.which === 13) {
                e.preventDefault();
                submitEditSubject();
            }
        });

        // Автофокус на поле ввода
        $('#editSubjectName').focus();
    }

    // Отправка данных редактирования предмета
    function submitEditSubject() {
        // Проверяем, что форма существует
        if (!$('#editSubjectId').length) {
            showMessage('Форма редактирования не найдена', 'error');
            return;
        }

        const subjectData = {
            subjectId: $('#editSubjectId').val(),
            subjectName: $('#editSubjectName').val().trim()
        };

        // Валидация
        if (!subjectData.subjectName) {
            showMessage('Пожалуйста, введите название предмета', 'error');
            $('#editSubjectName').focus();
            return;
        }

        if (subjectData.subjectName.length < 2) {
            showMessage('Название предмета должно содержать минимум 2 символа', 'error');
            $('#editSubjectName').focus();
            return;
        }

        // Показываем индикатор загрузки
        const saveBtn = $('#saveEditSubjectModalBtn');
        const originalText = saveBtn.html();
        saveBtn.html('<i class="fas fa-spinner fa-spin"></i> Сохранение...').prop('disabled', true);

        $.ajax({
            url: '/users/edit-subject',
            method: 'POST',
            data: subjectData,
            headers: {
                'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')
            },
            success: function(response) {
                if (response.success) {
                    showMessage(response.message, 'success');
                    closeEditSubjectModal();
                    loadSubjectsData(); // Перезагружаем список предметов
                } else {
                    showMessage(response.message, 'error');
                    saveBtn.html(originalText).prop('disabled', false);
                }
            },
            error: function(xhr) {
                let errorMsg = 'Ошибка при сохранении изменений';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMsg = xhr.responseJSON.message;
                }
                showMessage(errorMsg, 'error');
                saveBtn.html(originalText).prop('disabled', false);
            }
        });
    }

    // Закрытие модального окна редактирования предмета
    function closeEditSubjectModal() {
        const modal = $('#editSubjectModal');
        if (modal.length) {
            modal.fadeOut(200, function() {
                $(this).remove();
            });
        }
    }

    // Вспомогательная функция для экранирования HTML
    function escapeHtml(text) {
        if (!text) return '';
        return text
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    // Закрытие модального окна редактирования
    function closeEditClassModal() {
        const modal = $('#editClassModal');
        if (modal.length) {
            modal.fadeOut(200, function() {
                $(this).remove();
            });
        }
    }

    // Функция добавления нового предмета
    function addNewSubject() {
        const subjectData = {
            subjectName: $('#subjectName').val()
        };

        $.ajax({
            url: '/users/add-subject',
            method: 'POST',
            data: subjectData,
            headers: {
                'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')
            },
            success: function(response) {
                if (response.success) {
                    showMessage(response.message, 'success');
                    loadSubjectsData(); // Перезагружаем список
                    $('#addSubjectForm')[0].reset();
                } else {
                    showMessage(response.message, 'error');
                }
            },
            error: function() {
                showMessage('Ошибка создания предмета', 'error');
            }
        });
    }

    // Функция удаления предмета с подтверждением
    window.deleteSubject = function(subjectId) {
        $.ajax({
            url: '/users/delete-subject',
            method: 'POST',
            data: { subjectId: subjectId },
            headers: {
                'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')
            },
            success: function(response) {
                if (response.success) {
                    showMessage(response.message, 'success');
                    loadSubjectsData();
                } else if (response.warning) {
                    // Показываем предупреждение с подтверждением
                    if (confirm(response.message + '\n\nВы уверены, что хотите удалить предмет?')) {
                        // Повторный запрос с подтверждением
                        $.ajax({
                            url: '/users/delete-subject-force',
                            method: 'POST',
                            data: { subjectId: subjectId, force: true },
                            headers: {
                                'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')
                            },
                            success: function(forceResponse) {
                                if (forceResponse.success) {
                                    showMessage(forceResponse.message, 'success');
                                    loadSubjectsData();
                                } else {
                                    showMessage(forceResponse.message, 'error');
                                }
                            },
                            error: function() {
                                showMessage('Ошибка при удалении предмета', 'error');
                            }
                        });
                    }
                } else {
                    showMessage(response.message, 'error');
                }
            },
            error: function() {
                showMessage('Ошибка при удалении предмета', 'error');
            }
        });
    };

    // Функция отрисовки списка предметов - обновленная с вызовом editSubject
    function renderSubjectsList(response) {
        let html = '<div class="subjects-list">';

        // Форма добавления нового предмета
        html += `
            <div class="add-class-form">
                <h5><i class="fas fa-plus-circle"></i> Добавить новый предмет</h5>
                <form id="addSubjectForm">
                    <div class="form-group-small">
                        <label>Название предмета *</label>
                        <input type="text" id="subjectName" name="name" placeholder="Например: Математика" required>
                    </div>
                    <div class="form-actions-small">
                        <button type="button" id="cancelAddSubject" class="btn-sm btn-cancel-red">Отмена</button>
                        <button type="submit" class="btn-sm btn-primary-blue">Создать предмет</button>
                    </div>
                </form>
            </div>
        `;

        // Отображаем список предметов
        if (response.subjects && response.subjects.length > 0) {
            response.subjects.forEach(function(subject) {
                html += `
                    <div class="class-list-item" data-subject-id="${subject.subjectId}">
                        <div class="class-name">${escapeHtml(subject.subjectName)}</div>
                        <div class="class-actions">
                            <button class="btn-icon btn-edit-class" onclick="editSubject(${subject.subjectId})">
                                <i class="fas fa-edit"></i> Редактировать
                            </button>
                            <button class="btn-icon btn-delete-class" onclick="deleteSubject(${subject.subjectId})">
                                <i class="fas fa-trash"></i> Удалить
                            </button>
                        </div>
                    </div>
                `;
            });
        } else {
            html += '<p class="text-muted text-center">Нет созданных предметов</p>';
        }

        html += '</div>';
        $('#subjectManagementContent').html(html);

        // Обработчик формы добавления предмета
        $('#addSubjectForm').off('submit').on('submit', function(e) {
            e.preventDefault();
            addNewSubject();
        });

        $('#cancelAddSubject').click(function() {
            $('#addSubjectForm')[0].reset();
        });
    }



    // Функция показа сообщений - добавьте поддержку warning
    function showMessage(message, type) {
        let alertClass = 'alert-info';
        let icon = 'fa-info-circle';

        if (type === 'success') {
            alertClass = 'alert-success';
            icon = 'fa-check-circle';
        } else if (type === 'error') {
            alertClass = 'alert-danger';
            icon = 'fa-exclamation-circle';
        } else if (type === 'warning') {
            alertClass = 'alert-warning';
            icon = 'fa-exclamation-triangle';
        }

        const alertHtml = `
            <div class="alert ${alertClass}" style="position: fixed; top: 20px; right: 20px; z-index: 9999; min-width: 300px;">
                <i class="fas ${icon}"></i> ${message}
            </div>
        `;

        $('body').append(alertHtml);
        setTimeout(function() {
            $('.alert').fadeOut('slow', function() {
                $(this).remove();
            });
        }, 3000);
    }
});