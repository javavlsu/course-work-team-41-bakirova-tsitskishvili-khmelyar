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
        List<MerchItem> items = merchItemRepository.findByOrderByPriceAsc();

        model.addAttribute("items", items);
        model.addAttribute("title", "Школьный магазин");
        model.addAttribute("activePage", "merch");
        model.addAttribute("content", "merch/index");

        return "layout";
    }

    // Обработка покупки (AJAX)
    @PostMapping("/buy")
    @ResponseBody
    @Transactional // ОЧЕНЬ ВАЖНО: либо всё сохранится вместе, либо ничего!
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
}