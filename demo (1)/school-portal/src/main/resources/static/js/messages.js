$(document).ready(function() {
    // 1. Константы и селекторы
    const modal = $('#newMessageModal');
    const messageContainer = $('.message-list-container'); // Общий контейнер для делегирования
    const roleSelect = $('#recipientRole');
    const searchInput = $('#recipientSearch');
    const validationMsg = $('#recipientValidationMessage');
    const sendButton = $('#sendMessageButton');
    const hiddenToUserId = $('#hiddenToUserId');
    const messageTextarea = $('#messageText');
    const messageForm = $('#messageForm');

    // 2. Инициализация
    initAutocomplete();

    // 3. Функции модального окна
    function prepareModal(isReply = false, userId = '', userName = '', messageId = '') {
        messageForm[0].reset();
        hiddenToUserId.val('');
        $('#parentMessageId').val('');
        validationMsg.text('').removeClass('text-green-500 text-red-500 text-gray-500');
        messageTextarea.val('');

        if (isReply) {
            $('#parentMessageId').val(messageId);
            hiddenToUserId.val(userId);
            searchInput.val(userName).prop('readonly', true).prop('disabled', false);
            roleSelect.prop('disabled', true);
            validationMsg.text('Ответ пользователю: ' + userName).addClass('text-green-500');
        } else {
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
        if (searchInput.data("ui-autocomplete")) return;
        searchInput.autocomplete({
            source: function(request, response) {
                const role = roleSelect.val() || "";
                $.ajax({
                    url: '/messages/search-user',
                    dataType: "json",
                    data: { fullName: request.term, role: role },
                    success: function(data) {
                        if (data && data.length > 0) {
                            response($.map(data, function(item) {
                                return { label: item.fullName, value: item.fullName, userId: item.userId };
                            }));
                        } else {
                            response([]);
                        }
                    }
                });
            },
            minLength: 1,
            select: function(event, ui) {
                hiddenToUserId.val(ui.item.userId);
                searchInput.val(ui.item.value);
                validationMsg.text('Получатель выбран').addClass('text-green-500');
                checkFormValidity();
                return false;
            }
        });
    }

    // 4. ОБРАБОТЧИКИ СОБЫТИЙ

    // Используем делегирование для кнопок "Ответить" (чтобы работали на подгруженных сообщениях)
    $(document).on('click', '.btn-reply', function(e) {
        e.preventDefault();
        const btn = $(this);
        const messageRow = btn.closest('.message-row');
        // Находим id сообщения через hidden input или data-атрибут
        const messageId = messageRow.find('input[name="id"]').val();

        prepareModal(true, btn.data('user-id'), btn.data('user-name'), messageId);
    });

    // Кнопка "Загрузить еще"
    $(document).on('click', '#loadMoreBtn', function() {
        const btn = $(this);
        const page = btn.data('page');
        const filter = btn.data('filter');

        btn.prop('disabled', true).text('Загрузка...');

        $.get(`/messages/index?filter=${filter}&page=${page}`, function(data) {
            const htmlData = $(data);
            const newMessages = htmlData.find('.message-row');
            const nextPagination = htmlData.find('#loadMoreBtn');

            if (newMessages.length > 0) {
                $('#loadMoreWrapper').before(newMessages);
                if (nextPagination.length > 0) {
                    btn.data('page', page + 1).prop('disabled', false).text('Загрузить еще');
                } else {
                    $('#loadMoreWrapper').remove();
                }
            }
        });
    });

    // Остальные базовые события
    $('#openNewMessageModal').on('click', (e) => { e.preventDefault(); prepareModal(false); });
    $('#closeNewMessageModal').on('click', () => modal.hide());
    roleSelect.on('change', () => { hiddenToUserId.val(''); searchInput.val('').focus(); checkFormValidity(); });
    searchInput.on('input', function() { if (!$(this).prop('readonly')) { hiddenToUserId.val(''); checkFormValidity(); } });
    messageTextarea.on('input', checkFormValidity);

    $(window).on('click', (e) => { if ($(e.target).is(modal)) modal.hide(); });
    $(document).on('keydown', (e) => { if (e.key === 'Escape' && modal.is(':visible')) modal.hide(); });

    messageForm.on('submit', function(e) {
        if (hiddenToUserId.val() === '') {
            e.preventDefault();
            validationMsg.text('Пожалуйста, выберите получателя').addClass('text-red-500');
        }
    });
});