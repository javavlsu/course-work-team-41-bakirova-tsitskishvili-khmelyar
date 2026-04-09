package com.school.portal.service;

import com.school.portal.model.Homework;
import com.school.portal.repository.HomeworkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HomeworkService {

    @Autowired
    private HomeworkRepository homeworkRepository;

    public List<Homework> getHomeworkForTeacher(Integer teacherId) {
        return homeworkRepository.findHomeworkForTeacher(teacherId);
    }

    public List<Homework> getHomeworkForStudent(Integer studentId) {
        return homeworkRepository.findByStudentUserId(studentId);
    }

    public List<Homework> getHomeworkForClassAndStatus(Integer classId, Integer status) {
        return homeworkRepository.findHomeworkByClassAndStatus(classId, status);
    }

    @Transactional
    public Homework submitHomework(Homework homework) {
        homework.setDate(LocalDateTime.now());
        homework.setStatus(1); // Сдано
        return homeworkRepository.save(homework);
    }

    @Transactional
    public Homework reviewHomework(Integer homeworkId, Integer gradeValue, String comment) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new RuntimeException("Homework not found"));

        homework.setStatus(2); // Проверено
        homework.setTeacherComment(comment);

        return homeworkRepository.save(homework);
    }

    public List<Homework> getPendingHomework() {
        return homeworkRepository.findByStatus(1); // Сдано, но не проверено
    }
}