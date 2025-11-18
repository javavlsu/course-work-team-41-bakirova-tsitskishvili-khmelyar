// --- Загрузка боковой панели ---
document.addEventListener("DOMContentLoaded", function () {
    // Убираем класс "загрузки", чтобы показать страницу
    document.body.classList.remove("js-loading");

    // --- НАЧАЛО НОВОГО КОДА ---
    // Загружаем боковую панель
    fetch("left-panel.html")
        .then((response) => response.text()) // Получаем HTML как текст
        .then((data) => {
            // Вставляем полученный HTML в наш контейнер
            document.getElementById("left-panel-placeholder").innerHTML = data;

            // Активируем навигацию после загрузки панели
            activateNavigation();
            highlightCurrentPage();
        })
        .catch((error) => {
            console.error('Error loading left panel:', error);
            createFallbackPanel();
        });
    // --- КОНЕЦ НОВОГО КОДА ---

    // Инициализируем модальные окна
    initializeModals();
});

// Функция создания резервной панели при ошибке загрузки
function createFallbackPanel() {
    const placeholder = document.getElementById("left-panel-placeholder");
    if (placeholder) {
        placeholder.innerHTML = `
            <aside class="left-panel">
                <div class="logo">
                    <div class="logo-background"></div>
                    <div class="logo-text">ШкЭД</div>
                </div>
                <nav class="left-panel-buttons">
                    <a href="home.html" class="nav-button">Главная</a>
                    <a href="journal-teacher.html" class="nav-button">Журнал</a>
                    <a href="#" class="nav-button">Сообщения</a>
                    <a href="#" class="nav-button">Расписание</a>
                </nav>
                <div class="profile-buttons">
                    <a href="#" class="nav-button">Профиль</a>
                    <a href="index.html" class="nav-button">Выйти</a>
                </div>
            </aside>
        `;
        activateNavigation();
        highlightCurrentPage();
    }
}

// Функция активации навигации
function activateNavigation() {
    const navButtons = document.querySelectorAll('.nav-button');

    navButtons.forEach(button => {
        // Убираем стандартное поведение для ссылок с #
        if (button.getAttribute('href') === '#') {
            button.addEventListener('click', function(e) {
                e.preventDefault();

                // Убираем активный класс у всех кнопок
                navButtons.forEach(btn => btn.classList.remove('active'));
                // Добавляем активный класс текущей кнопке
                this.classList.add('active');

                // Показываем сообщение о недоступности
                showAccessDeniedModal();
            });
        } else {
            // Для обычных ссылок просто подсвечиваем текущую страницу
            button.addEventListener('click', function() {
                // Обновляем активное состояние после перехода
                setTimeout(() => {
                    highlightCurrentPage();
                }, 100);
            });
        }
    });
}

// Функция подсветки текущей страницы
function highlightCurrentPage() {
    const currentPage = window.location.pathname.split('/').pop() || 'home.html';
    const navButtons = document.querySelectorAll('.nav-button');

    navButtons.forEach(button => {
        const buttonHref = button.getAttribute('href');
        if (buttonHref && buttonHref !== '#') {
            if (buttonHref === currentPage) {
                button.classList.add('active');
            } else {
                button.classList.remove('active');
            }
        }
    });
}

// Функция инициализации модальных окон
function initializeModals() {
    const openModalButtons = document.querySelectorAll("[data-modal-trigger]");
    const closeModalButtons = document.querySelectorAll(".modal-close-btn");
    const overlays = document.querySelectorAll(".modal-overlay");

    // Функция открытия окна
    function openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add("active");
            document.body.style.overflow = 'hidden'; // Блокируем скролл страницы
        }
    }

    // Функция закрытия окна
    function closeModal(modal) {
        modal.classList.remove("active");
        document.body.style.overflow = ''; // Восстанавливаем скролл
    }

    // Добавляем обработчик клика на все кнопки-триггеры для открытия
    openModalButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const modalId = button.getAttribute("data-modal-trigger");
            openModal(modalId);
        });
    });

    // Добавляем обработчик клика на все кнопки для закрытия
    closeModalButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const modal = button.closest(".modal-overlay");
            closeModal(modal);
        });
    });

    // Закрытие окна по клику на темный фон
    overlays.forEach((overlay) => {
        overlay.addEventListener("click", (event) => {
            if (event.target === overlay) {
                closeModal(overlay);
            }
        });
    });

    // Закрытие по ESC
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            const activeModal = document.querySelector('.modal-overlay.active');
            if (activeModal) {
                closeModal(activeModal);
            }
        }
    });

    // ==========================================================
    // НОВЫЙ КОД: Логика для обработки отправки формы предложения
    // ==========================================================
    const proposalForm = document.getElementById("proposal-form");

    // Проверяем, существует ли форма на странице, чтобы избежать ошибок
    if (proposalForm) {
        proposalForm.addEventListener("submit", function (event) {
            // 1. Предотвращаем стандартную отправку формы и перезагрузку страницы
            event.preventDefault();

            // 2. Находим родительское модальное окно с формой
            const formModal = proposalForm.closest(".modal-overlay");

            // 3. Закрываем окно с формой
            if (formModal) {
                closeModal(formModal);
            }

            // 4. Открываем окно "Спасибо"
            openModal("thank-you-modal");

            // 5. Очищаем форму
            proposalForm.reset();

            // Здесь в будущем можно будет добавить код для реальной отправки данных на сервер
            // например, с помощью fetch()
        });
    }
}

// Функция показа модального окна "Доступ запрещен"
function showAccessDeniedModal() {
    const modal = document.getElementById('access-denied-modal');
    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
}

// Инициализация при полной загрузке страницы
window.addEventListener('load', function() {
    // Добавляем класс для плавных переходов после загрузки
    document.body.classList.add('loaded');
});
