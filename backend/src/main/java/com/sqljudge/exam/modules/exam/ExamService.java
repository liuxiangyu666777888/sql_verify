package com.sqljudge.exam.modules.exam;

import com.sqljudge.exam.common.BusinessException;
import com.sqljudge.exam.common.CurrentUser;
import com.sqljudge.exam.modules.question.QuestionMapper;
import com.sqljudge.exam.modules.question.QuestionRecord;
import com.sqljudge.exam.modules.user.UserMapper;
import com.sqljudge.exam.modules.user.UserRecord;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExamService {
    private final ExamMapper examMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;

    public ExamService(ExamMapper examMapper, QuestionMapper questionMapper, UserMapper userMapper) {
        this.examMapper = examMapper;
        this.questionMapper = questionMapper;
        this.userMapper = userMapper;
    }

    public List<ExamRecord> listMine() {
        String role = CurrentUser.role();
        if ("STUDENT".equals(role)) {
            return examMapper.listByStudent(CurrentUser.id());
        }
        if ("ADMIN".equals(role)) {
            return examMapper.listAll();
        }
        return examMapper.listByCreator(CurrentUser.id());
    }

    public ExamRecord create(ExamRequest request) {
        ExamRecord record = toRecord(request);
        record.setStatus("DRAFT");
        record.setCreatorId(CurrentUser.id());
        examMapper.insert(record);
        return record;
    }

    public ExamRecord update(Long examId, ExamRequest request) {
        ExamRecord existing = detail(examId);
        if (!"ADMIN".equals(CurrentUser.role()) && !CurrentUser.id().equals(existing.getCreatorId())) {
            throw BusinessException.forbidden("无权修改该考试");
        }
        ExamRecord record = toRecord(request);
        record.setExamId(examId);
        record.setCreatorId(existing.getCreatorId());
        record.setStatus(existing.getStatus());
        examMapper.update(record);
        return detail(examId);
    }

    public void delete(Long examId) {
        ExamRecord existing = detail(examId);
        if (!"ADMIN".equals(CurrentUser.role()) && !CurrentUser.id().equals(existing.getCreatorId())) {
            throw BusinessException.forbidden("无权删除该考试");
        }
        examMapper.updateStatus(examId, "ARCHIVED");
    }

    public ExamRecord detail(Long examId) {
        ExamRecord record = examMapper.findById(examId);
        if (record == null) {
            throw BusinessException.notFound("考试不存在");
        }
        ensureCanView(record);
        return record;
    }

    public List<Map<String, Object>> questions(Long examId) {
        detail(examId);
        return examMapper.listQuestions(examId);
    }

    public List<Map<String, Object>> scores(Long examId) {
        detail(examId);
        return examMapper.listScores(examId);
    }

    public List<StudentOptionResponse> studentOptions() {
        return userMapper.listActiveByRole("STUDENT").stream()
                .map(user -> new StudentOptionResponse(user.getUserId(), user.getUsername(), user.getRealName()))
                .collect(Collectors.toList());
    }

    public void publish(Long examId) {
        ExamRecord existing = detail(examId);
        if ("PUBLISHED".equals(existing.getStatus())) {
            throw BusinessException.badRequest("考试已发布");
        }
        if ("ARCHIVED".equals(existing.getStatus())) {
            throw BusinessException.badRequest("已归档考试不能发布");
        }
        examMapper.updateStatus(examId, "PUBLISHED");
    }

    public void addQuestions(Long examId, List<Map<String, Object>> questions) {
        ExamRecord exam = detail(examId);
        if (!"DRAFT".equals(exam.getStatus())) {
            throw BusinessException.badRequest("只能为草稿状态的考试添加题目");
        }
        if (questions == null || questions.isEmpty()) {
            throw BusinessException.badRequest("请至少选择一道题");
        }
        Set<Long> seen = new HashSet<>();
        for (Map<String, Object> item : questions) {
            if (item == null) {
                throw BusinessException.badRequest("题目信息不能为空");
            }
            Number qid = (Number) item.get("questionId");
            Number score = (Number) item.get("score");
            Number order = (Number) item.getOrDefault("questionOrder", 1);
            if (qid == null) {
                throw BusinessException.badRequest("题目ID不能为空");
            }
            if (score == null || score.doubleValue() < 0 || score.doubleValue() > 100) {
                throw BusinessException.badRequest("题目分值必须在 0 到 100 之间");
            }
            if (order == null || order.intValue() < 1) {
                throw BusinessException.badRequest("题目顺序必须大于 0");
            }
            Long questionId = qid.longValue();
            if (!seen.add(questionId)) {
                throw BusinessException.badRequest("请求中包含重复题目");
            }
            QuestionRecord question = questionMapper.findById(questionId);
            if (question == null || question.getVisible() == null || question.getVisible() == 0) {
                throw BusinessException.notFound("题目不存在");
            }
            if (examMapper.countExamQuestion(examId, questionId) > 0) {
                throw BusinessException.badRequest("题目已在考试中");
            }
            examMapper.insertExamQuestion(examId, questionId, score.doubleValue(), order.intValue());
        }
    }

    public void addStudents(Long examId, List<Long> studentIds) {
        ExamRecord exam = detail(examId);
        if (!"DRAFT".equals(exam.getStatus())) {
            throw BusinessException.badRequest("只能为草稿状态的考试添加学生");
        }
        if (studentIds == null || studentIds.isEmpty()) {
            throw BusinessException.badRequest("请至少选择一名学生");
        }
        Set<Long> seen = new HashSet<>();
        for (Long studentId : studentIds) {
            if (studentId == null) {
                throw BusinessException.badRequest("学生ID不能为空");
            }
            if (!seen.add(studentId)) {
                throw BusinessException.badRequest("请求中包含重复学生");
            }
            UserRecord student = userMapper.findById(studentId);
            if (student == null || !"STUDENT".equals(student.getRole())) {
                throw BusinessException.notFound("学生不存在");
            }
            if (!"ACTIVE".equals(student.getStatus())) {
                throw BusinessException.badRequest("学生账号不可用");
            }
            if (examMapper.countExamStudent(examId, studentId) > 0) {
                throw BusinessException.badRequest("学生已在考试中");
            }
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

    private ExamRecord toRecord(ExamRequest request) {
        if (request == null) {
            throw BusinessException.badRequest("考试信息不能为空");
        }
        if (request.getExamName() == null || request.getExamName().trim().isEmpty()) {
            throw BusinessException.badRequest("考试名称不能为空");
        }
        if (request.getStartTime() == null || request.getEndTime() == null || !request.getEndTime().isAfter(request.getStartTime())) {
            throw BusinessException.badRequest("考试时间不合法");
        }
        ExamRecord record = new ExamRecord();
        record.setExamName(request.getExamName().trim());
        record.setStartTime(request.getStartTime());
        record.setEndTime(request.getEndTime());
        record.setDurationMinutes(request.getDurationMinutes());
        record.setInstructions(request.getInstructions());
        record.setLockdownEnabled(Boolean.TRUE.equals(request.getLockdownEnabled()) ? 1 : 0);
        return record;
    }

    private void ensureCanView(ExamRecord record) {
        String role = CurrentUser.role();
        if ("ADMIN".equals(role)) {
            return;
        }
        if (("TEACHER".equals(role) || "ASSISTANT".equals(role)) && CurrentUser.id().equals(record.getCreatorId())) {
            return;
        }
        if ("STUDENT".equals(role) && examMapper.countExamStudent(record.getExamId(), CurrentUser.id()) > 0) {
            return;
        }
        throw BusinessException.forbidden("无权查看该考试");
    }
}
