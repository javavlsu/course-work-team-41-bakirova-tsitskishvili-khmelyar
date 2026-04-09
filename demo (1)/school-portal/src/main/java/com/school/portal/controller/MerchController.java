package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/merch")
public class MerchController {

    @Autowired
    private MerchItemRepository merchItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchRequestRepository merchRequestRepository;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    // Отображение витрины магазина
    @GetMapping
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<MerchItem> items;
        if (isAdmin) {
            // Админы видят все товары (включая архивные)
            items = merchItemRepository.findByOrderByPriceAsc();
        } else {
            // Ученики видят только активные
            items = merchItemRepository.findByIsArchivedFalseOrderByPriceAsc();
        }

        model.addAttribute("items", items);
        model.addAttribute("title", "Школьный магазин");
        model.addAttribute("activePage", "merch");
        model.addAttribute("content", "merch/index");

        return "layout";
    }

    // Создание товара
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createItem(@RequestParam String name,
                                          @RequestParam String text,
                                          @RequestParam Integer price,
                                          @RequestParam(required = false) String imageUrl) {
        Map<String, Object> response = new HashMap<>();
        try {
            MerchItem item = new MerchItem();
            item.setName(name);
            item.setText(text);
            item.setPrice(price);
            item.setImageUrl(imageUrl);
            item.setIsArchived(false);

            merchItemRepository.save(item);
            response.put("success", true);
            response.put("message", "Товар успешно добавлен!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }

    // Получение товара для редактирования
    @GetMapping("/get/{id}")
    @ResponseBody
    public Map<String, Object> getItem(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        merchItemRepository.findById(id).ifPresentOrElse(
                item -> {
                    response.put("success", true);
                    response.put("item", item);
                },
                () -> {
                    response.put("success", false);
                    response.put("message", "Товар не найден");
                }
        );
        return response;
    }

    // Сохранение изменений
    @PostMapping("/edit")
    @ResponseBody
    public Map<String, Object> editItem(@RequestParam Integer merchId,
                                        @RequestParam String name,
                                        @RequestParam String text,
                                        @RequestParam Integer price,
                                        @RequestParam(required = false) String imageUrl) {
        Map<String, Object> response = new HashMap<>();
        try {
            MerchItem item = merchItemRepository.findById(merchId)
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));

            item.setName(name);
            item.setText(text);
            item.setPrice(price);
            item.setImageUrl(imageUrl);

            merchItemRepository.save(item);
            response.put("success", true);
            response.put("message", "Товар успешно обновлен!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }

    // Архивирование/разархивирование
    @PostMapping("/toggle-archive")
    @ResponseBody
    public Map<String, Object> toggleArchive(@RequestParam Integer merchId) {
        Map<String, Object> response = new HashMap<>();
        try {
            MerchItem item = merchItemRepository.findById(merchId)
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));

            // Меняем статус на противоположный
            item.setIsArchived(!item.getIsArchived());
            merchItemRepository.save(item);

            response.put("success", true);
            response.put("message", item.getIsArchived() ? "Товар перенесен в архив" : "Товар восстановлен из архива");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }

    // Обработка покупки (AJAX)
    @PostMapping("/buy")
    @ResponseBody
    @Transactional
    public Map<String, Object> buyItem(@RequestParam Integer merchId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Узнаем, кто покупает
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User student = userRepository.findByLogin(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Проверка роли (покупать могут только ученики)
            if (!"STUDENT".equals(student.getRole().getRoleName())) {
                response.put("success", false);
                response.put("message", "Только ученики могут покупать мерч!");
                return response;
            }

            // 2. Ищем товар
            MerchItem item = merchItemRepository.findById(merchId)
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));

            // 3. Проверяем баланс
            if (student.getCoins() < item.getPrice()) {
                response.put("success", false);
                response.put("message", "Недостаточно монет для покупки!");
                return response;
            }

            // 4. СПИСЫВАЕМ МОНЕТЫ
            student.setCoins(student.getCoins() - item.getPrice());
            userRepository.save(student);

            // 5. СОЗДАЕМ ЗАЯВКУ (MerchRequest)
            MerchRequest request = new MerchRequest();
            request.setStudent(student);
            request.setMerchItem(item);
            request.setStatus(0); // 0 - Новая заявка
            request = merchRequestRepository.save(request);

            // 6. ПИШЕМ ИСТОРИЮ (TransactionHistory)
            TransactionHistory transaction = new TransactionHistory();
            transaction.setStudent(student);
            transaction.setAmount(-item.getPrice()); // Минус, так как это трата
            transaction.setDescription("Покупка в магазине: " + item.getName());
            transaction.setMerchRequest(request);
            transactionHistoryRepository.save(transaction);

            response.put("success", true);
            response.put("message", "Покупка успешно оформлена! Ожидайте выдачи.");
            response.put("newBalance", student.getCoins()); // Отправляем новый баланс на фронт

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // Отображение истории
    @GetMapping("/history")
    public String history(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User student = userRepository.findByLogin(auth.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Получаем историю транзакций ученика, отсортированную по дате (новые сверху)
        List<TransactionHistory> transactions = transactionHistoryRepository.findLatestTransactions(student.getUserId());

        model.addAttribute("transactions", transactions);
        model.addAttribute("title", "История операций");
        model.addAttribute("activePage", "merch");
        model.addAttribute("content", "merch/history");

        return "layout";
    }

    // ================== ПАНЕЛЬ ДИРЕКТОРА ==================

    // 1. Страница всех заявок
    @GetMapping("/requests")
    public String viewRequests(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isDirector = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DIRECTOR") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!isDirector) {
            return "redirect:/merch"; // Защита: если зашел ученик, выкидываем обратно
        }

        // Получаем все заявки
        List<MerchRequest> requests = merchRequestRepository.findAll();

        // Сортируем: сначала новые (0), потом всё остальное по дате
        requests.sort((r1, r2) -> {
            if (r1.getStatus() == 0 && r2.getStatus() != 0) return -1;
            if (r1.getStatus() != 0 && r2.getStatus() == 0) return 1;
            return r2.getRequestDate().compareTo(r1.getRequestDate());
        });

        model.addAttribute("requests", requests);
        model.addAttribute("title", "Заявки на мерч");
        model.addAttribute("activePage", "merch-requests");
        model.addAttribute("content", "merch/requests");

        return "layout";
    }

    // 2. Обработка решения (Одобрить / Отклонить)
    @PostMapping("/requests/process")
    @ResponseBody
    @Transactional
    public Map<String, Object> processRequest(@RequestParam Integer requestId, @RequestParam String action) {
        Map<String, Object> response = new HashMap<>();

        try {
            MerchRequest request = merchRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

            // Защита от двойного нажатия
            if (request.getStatus() != 0) {
                response.put("success", false);
                response.put("message", "Эта заявка уже была обработана ранее!");
                return response;
            }

            if ("approve".equals(action)) {
                // ОДОБРЕНИЕ ЗАЯВКИ
                request.setStatus(1); // 1 = Выдано
                request.setFulfilledDate(java.time.LocalDateTime.now());
                merchRequestRepository.save(request);

                response.put("message", "Заявка одобрена. Товар выдан!");

            } else if ("reject".equals(action)) {
                // ОТКЛОНЕНИЕ И ВОЗВРАТ СРЕДСТВ
                request.setStatus(2); // 2 = Отклонено
                request.setFulfilledDate(java.time.LocalDateTime.now());
                merchRequestRepository.save(request);

                User student = request.getStudent();
                MerchItem item = request.getMerchItem();

                // 1. Возвращаем монеты
                student.setCoins(student.getCoins() + item.getPrice());
                userRepository.save(student);

                // 2. Пишем историю возврата
                TransactionHistory refundTx = new TransactionHistory();
                refundTx.setStudent(student);
                refundTx.setAmount(item.getPrice()); // ПЛЮС монеты
                refundTx.setDescription("Возврат монет за отклоненный заказ №" + request.getRequestId() + " (" + item.getName() + ")");

                transactionHistoryRepository.save(refundTx);

                response.put("message", "Заявка отклонена. Монеты (" + item.getPrice() + ") возвращены на баланс ученика.");
            }

            response.put("success", true);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }

        return response;
    }
}