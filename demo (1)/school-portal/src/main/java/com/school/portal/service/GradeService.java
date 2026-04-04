package com.school.portal.service;

import com.school.portal.model.Grade;
import com.school.portal.model.TransactionHistory;
import com.school.portal.model.User;
import com.school.portal.repository.GradeRepository;
import com.school.portal.repository.TransactionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TransactionHistoryRepository transactionRepository;

    public List<Grade> getGradesForStudent(Integer studentId) {
        return gradeRepository.findByStudentUserId(studentId);
    }

    public Optional<Grade> getGradeForLesson(Integer studentId, Integer lessonId) {
        return gradeRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId);
    }

    @Transactional
    public Grade saveGrade(Grade grade) {
        boolean isNew = grade.getGradeId() == null;

        grade = gradeRepository.save(grade);

        // Если оценка новая и есть значение, начисляем монеты
        if (isNew && grade.getGradeValue() != null) {
            int coins = calculateCoinsForGrade(grade.getGradeValue());
            if (coins > 0) {
                createCoinTransaction(grade.getStudent(), coins,
                        "Получена оценка " + grade.getGradeValue(), grade);
            }
        }

        return grade;
    }

    @Transactional
    public void deleteGrade(Integer gradeId) {
        gradeRepository.deleteById(gradeId);
    }

    private int calculateCoinsForGrade(int gradeValue) {
        switch (gradeValue) {
            case 5: return 10;
            case 4: return 5;
            default: return 0;
        }
    }

    private void createCoinTransaction(User student, int coins, String description, Grade grade) {
        TransactionHistory transaction = new TransactionHistory();
        transaction.setStudent(student);
        transaction.setAmount(coins);
        transaction.setDescription(description);
        transaction.setDate(LocalDateTime.now());
        transaction.setGrade(grade);

        transactionRepository.save(transaction);

        // Обновляем баланс ученика
        student.setCoins(student.getCoins() + coins);
    }

    public Double getStudentAverageGrade(Integer studentId) {
        return gradeRepository.getAverageGradeForStudent(studentId);
    }

    public List<Grade> getGradesForClassAndPeriod(Integer classId, Integer subjectId,
                                                  LocalDateTime startDate, LocalDateTime endDate) {
        return gradeRepository.findGradesForClassAndSubjectAndPeriod(classId, subjectId, startDate, endDate);
    }
}