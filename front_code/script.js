// --- Логика для управления модальными окнами ---

// Находим все элементы для управления окнами
const openModalButtons = document.querySelectorAll("[data-modal-trigger]");
const closeModalButtons = document.querySelectorAll(".modal-close-btn");
const overlays = document.querySelectorAll(".modal-overlay");

// Функция открытия окна
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add("active");
  }
}

// Функция закрытия окна
function closeModal(modal) {
  modal.classList.remove("active");
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

    // Здесь в будущем можно будет добавить код для реальной отправки данных на сервер
    // например, с помощью fetch()
  });
}
