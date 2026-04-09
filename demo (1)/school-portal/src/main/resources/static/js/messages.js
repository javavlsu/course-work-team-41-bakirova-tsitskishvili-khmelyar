$(document).ready(function() {
    const modal = $('#newMessageModal');
    const openNewMsgBtn = $('#openNewMessageModal');
    const closeBtn = $('#closeNewMessageModal');
    const replyButtons = $('.btn-reply');

    const roleSelect = $('#recipientRole');
    const searchInput = $('#recipientSearch');
    const validationMsg = $('#recipientValidationMessage');
    const sendButton = $('#sendMessageButton');
    const hiddenToUserId = $('#hiddenToUserId');
    const messageTextarea = $('#messageText');
    const messageForm = $('#messageForm');

    // Инициализируем autocomplete один раз при загрузке страницы
    initAutocomplete();

    function prepareModal(isReply = false, userId = '', userName = '', messageId = '') {
        // Сброс формы вручную, чтобы не затереть hidden поля сразу
        messageForm[0].reset();
        hiddenToUserId.val('');
        $('#parentMessageId').val('');

        validationMsg.text('');
        validationMsg.removeClass('text-green-500 text-red-500 text-gray-500');
        messageTextarea.val('');

        if (isReply) {
            $('#parentMessageId').val(messageId);
            hiddenToUserId.val(userId);
            searchInput.val(userName);

            searchInput.prop('readonly', true).prop('disabled', false);
            roleSelect.prop('disabled', true);

            validationMsg.text('Ответ пользователю: ' + userName).addClass('text-green-500');
        } else {
            // Режим нового сообщения
            roleSelect.prop('disabled', false).val('');
            searchInput.prop('readonly', false).prop('disabled', false).val('');
            validationMsg.text('Начните вводить ФИО...').addClass('text-gray-500');
        }

        checkFormValidity();
        modal.css('display', 'flex');
    }

    function checkFormValidity() {
        const hasRecipient = hiddenToUserId.val() !== '';
        const hasText = messageTextarea.val().trim().length > 0;
        sendButton.prop('disabled', !(hasRecipient && hasText));
    }

    function initAutocomplete() {
        // Если autocomplete уже был инициализирован, не нужно его уничтожать,
        // он сам подхватит изменения roleSelect.val() при запросе
        if (searchInput.data("ui-autocomplete")) return;

        searchInput.autocomplete({
            source: function(request, response) {
                const role = roleSelect.val() || ""; // Если null, отправляем пустую строку

                validationMsg.text('Поиск...').removeClass('text-red-500 text-green-500').addClass('text-gray-500');

                $.ajax({
                    url: '/messages/search-user',
                    dataType: "json",
                    data: {
                        fullName: request.term,
                        role: role
                    },
                    success: function(data) {
                        if (data && data.length > 0) {
                            validationMsg.text('Выберите из списка').addClass('text-gray-500');
                            response($.map(data, function(item) {
                                return {
                                    label: item.fullName,
                                    value: item.fullName,
                                    userId: item.userId
                                };
                            }));
                        } else {
                            validationMsg.text('Пользователи не найдены').addClass('text-red-500');
                            response([]);
                        }
                    },
                    error: function() {
                        validationMsg.text('Ошибка сервера').addClass('text-red-500');
                    }
                });
            },
            minLength: 1,
            select: function(event, ui) {
                hiddenToUserId.val(ui.item.userId);
                searchInput.val(ui.item.value);
                validationMsg.text('Получатель выбран').removeClass('text-gray-500').addClass('text-green-500');
                checkFormValidity();
                return false;
            }
        });
    }

    // Обработчики событий
    openNewMsgBtn.on('click', function(e) {
        e.preventDefault();
        prepareModal(false);
    });

    closeBtn.on('click', function(e) {
        e.preventDefault();
        modal.hide();
    });

    roleSelect.on('change', function() {
        hiddenToUserId.val('');
        searchInput.val('').focus();
        checkFormValidity();
    });

    searchInput.on('input', function() {
        if (!$(this).prop('readonly')) {
            hiddenToUserId.val('');
            checkFormValidity();
        }
    });

    messageTextarea.on('input', checkFormValidity);

    replyButtons.on('click', function(e) {
        e.preventDefault();
        const btn = $(this);
        const messageRow = btn.closest('.message-row');
        const messageId = messageRow.find('input[name="id"]').val();

        prepareModal(
            true,
            btn.data('user-id'),
            btn.data('user-name'),
            messageId
        );
    });

    // Закрытие модального окна по клику вне его
    $(window).on('click', function(e) {
        if ($(e.target).is(modal)) {
            modal.hide();
        }
    });

    // Закрытие по клавише Escape
    $(document).on('keydown', function(e) {
        if (e.key === 'Escape' && modal.is(':visible')) {
            modal.hide();
        }
    });

    // Дополнительная валидация перед отправкой формы
    messageForm.on('submit', function(e) {
        const hasRecipient = hiddenToUserId.val() !== '';
        const hasText = messageTextarea.val().trim().length > 0;

        if (!hasRecipient) {
            e.preventDefault();
            validationMsg.text('Пожалуйста, выберите получателя').addClass('text-red-500');
            return false;
        }

        if (!hasText) {
            e.preventDefault();
            alert('Пожалуйста, введите текст сообщения');
            return false;
        }

        return true;
    });
});