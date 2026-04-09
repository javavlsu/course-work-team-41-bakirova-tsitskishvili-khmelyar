$(document).ready(function() {
        const panel = $('#messagePanel');
        const btn = $('#toggleMessagePanelBtn');
        const resp = $('#messageResponse');

        // Функция переключения (работает с 1-го раза)
        btn.on('click', function() {
            panel.slideToggle(300, function() {
                if (panel.is(':visible')) {
                    btn.html('<i class="fas fa-times"></i> &nbsp; Скрыть форму');
                    $('#messageBody').focus();
                } else {
                    btn.html('<i class="fas fa-envelope"></i> &nbsp; Написать Директору');
                }
            });
        });

        $('#closeMessagePanelBtn').on('click', function() {
            panel.slideUp(300);
            btn.html('<i class="fas fa-envelope"></i> &nbsp; Написать Директору');
        });

        // Отправка (AJAX)
        $('#messageForm').on('submit', function(e) {
            e.preventDefault();
            const submitBtn = $('#submitMessageBtn');

            submitBtn.prop('disabled', true).text('Отправка...');
            resp.hide().removeClass('response-success response-error');

            $.ajax({
                url: $(this).attr('action'),
                type: 'POST',
                data: $(this).serialize(),
                success: function(data) {
                    if (data.success) {
                        resp.addClass('response-success').html(data.message).show();
                        $('#messageBody').val('');
                        setTimeout(() => { panel.slideUp(300); btn.html('<i class="fas fa-envelope"></i> Написать'); }, 2000);
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
    });