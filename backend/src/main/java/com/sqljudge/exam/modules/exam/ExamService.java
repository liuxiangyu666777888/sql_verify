package com.sqljudge.exam.modules.exam;

import com.sqljudge.exam.common.BusinessException;
import com.sqljudge.exam.common.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ExamService {
    private final ExamMapper examMapper;

    public ExamService(ExamMapper examMapper) {
        this.examMapper = examMapper;
    }

    public List<ExamRecord> listMine() {
        String role = CurrentUser.role();
        if ("STUDENT".equals(role)) {
            return examMapper.listByStudent(CurrentUser.id());
        }
        return examMapper.listByCreator(CurrentUser.id());
    }

    public ExamRecord create(ExamRequest request) {
        if (request.getExamName() == null || request.getExamName().trim().isEmpty()) {
            throw BusinessException.badRequest("考试名称不能为空");
        }
        if (request.getStartTime() == null || request.getEndTime() == null || !request.getEndTime().isAfter(request.getStartTime())) {
            throw BusinessException.badRequest("考试时间不合法");
        }
        ExamRecord record = new ExamRecord();
        record.setExamName(request.getExamName());
        record.setStartTime(request.getStartTime());
        record.setEndTime(request.getEndTime());
        record.setDurationMinutes(request.getDurationMinutes());
        record.setInstructions(request.getInstructions());
        record.setLockdownEnabled(Boolean.TRUE.equals(request.getLockdownEnabled()) ? 1 : 0);
        record.setStatus("DRAFT");
        record.setCreatorId(CurrentUser.id());
        examMapper.insert(record);
        return record;
    }

    public ExamRecord detail(Long examId) {
        ExamRecord record = examMapper.findById(examId);
        if (record == null) {
            throw BusinessException.notFound("考试不存在");
        }
        return record;
    }

    public List<Map<String, Object>> questions(Long examId) {
        return examMapper.listQuestions(examId);
    }

    public List<Map<String, Object>> scores(Long examId) {
        return examMapper.listScores(examId);
    }

    public void publish(Long examId) {
        detail(examId);
        examMapper.updateStatus(examId, "PUBLISHED");
    }

    public void addQuestions(Long examId, List<Map<String, Object>> questions) {
        detail(examId);
        for (Map<String, Object> item : questions) {
            Number qid = (Number) item.get("questionId");
            Number score = (Number) item.get("score");
            Number order = (Number) item.getOrDefault("questionOrder", 1);
            examMapper.insertExamQuestion(examId, qid.longValue(), score.doubleValue(), order.intValue());
        }
    }

    public void addStudents(Long examId, List<Long> studentIds) {
        detail(examId);
        for (Long studentId : studentIds) {
            examMapper.insertExamStudent(examId, studentId);
        }
    }

    public void recalculateFinalScore(Long examId, Long studentId) {
        if (examId != null) {
            examMapper.recalculateFinalScore(examId, studentId);
        }
    }

    public void markSubmitted(Long examId, Long studentId) {
        if (examId != null) {
            examMapper.updateStudentStatus(examId, studentId, "SUBMITTED");
        }
    }
}
