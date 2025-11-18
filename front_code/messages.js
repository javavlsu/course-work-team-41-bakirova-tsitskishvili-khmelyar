document.addEventListener('DOMContentLoaded', function() {

    // Левая панель
    const panelPlaceholder = document.getElementById('left-panel-placeholder');
    if (panelPlaceholder) {
        fetch('left-panel.html')
            .then(res => res.ok ? res.text() : Promise.reject(`Ошибка ${res.status}`))
            .then(html => panelPlaceholder.innerHTML = html)
            .catch(err => panelPlaceholder.innerHTML = `<div style="color:red;">Ошибка загрузки меню: ${err}</div>`);
    }

    // Кнопка "Новое сообщение"
    const newMessageBtn = document.getElementById('new-message-btn');
    if (newMessageBtn) {
        newMessageBtn.addEventListener('click', () => {
            alert('Открывается форма для создания нового сообщения.');
        });
    }

    // Контейнер сообщений
    const messagesList = document.querySelector('.messages-list');

    // Делегирование событий на кнопки внутри сообщений
    messagesList.addEventListener('click', function(e) {
        const replyBtn = e.target.closest('.reply-btn');
        const deleteBtn = e.target.closest('.delete-btn');

        if (replyBtn) {
            const messageItem = replyBtn.closest('.message-item');
            alert(`Ответ на сообщение ID: ${messageItem.dataset.messageId}`);
        }

        if (deleteBtn) {
            const messageItem = deleteBtn.closest('.message-item');
            if (confirm('Вы уверены, что хотите удалить это сообщение?')) {
                messageItem.remove();
            }
        }
    });

    // Вкладки
    const inboxTab = document.getElementById('inbox-tab');
    const sentTab = document.getElementById('sent-tab');

    // Сохраняем исходные входящие
    const inboxMessages = messagesList.innerHTML;

    // Заглушка для отправленных
    const sentMessages = `
        <div class="message-item" data-message-id="3">
            <div class="message-content">
                <div class="sender-name">Петрова С.А. (Учитель)</div>
                <p class="message-body">Просьба проверить домашнее задание до конца недели.</p>
            </div>
            <div class="message-actions">
                <button class="action-button delete-btn">
                    <span class="icon">🗑️</span>
                    <span class="text_label">Удалить</span>
                </button>
            </div>
        </div>
    `;

    inboxTab.addEventListener('click', () => {
        inboxTab.classList.add('active');
        sentTab.classList.remove('active');
        messagesList.innerHTML = inboxMessages;
    });

    sentTab.addEventListener('click', () => {
        sentTab.classList.add('active');
        inboxTab.classList.remove('active');
        messagesList.innerHTML = sentMessages;
    });

});
