package com.sqljudge.exam.modules.judge;

import com.sqljudge.exam.common.BusinessException;
import com.sqljudge.exam.common.CurrentUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SubmissionService {
    private final JudgeService judgeService;
    private final SubmissionMapper submissionMapper;
    private final com.sqljudge.exam.modules.exam.ExamService examService;
    private final com.sqljudge.exam.modules.exam.ExamMapper examMapper;
    private final ObjectMapper objectMapper;

    public SubmissionService(JudgeService judgeService, SubmissionMapper submissionMapper,
                             com.sqljudge.exam.modules.exam.ExamService examService,
                             com.sqljudge.exam.modules.exam.ExamMapper examMapper,
                             ObjectMapper objectMapper) {
        this.judgeService = judgeService;
        this.submissionMapper = submissionMapper;
        this.examService = examService;
        this.examMapper = examMapper;
        this.objectMapper = objectMapper;
    }

    public JudgeResult submit(JudgeRequest request) {
        JudgeResult result = judgeService.run(request);
        SubmissionRecord record = new SubmissionRecord();
        record.setUserId(CurrentUser.id());
        record.setQuestionId(request.getQuestionId());
        record.setExamId(request.getExamId());
        record.setSqlCode(request.getSqlCode());
        record.setStatus(result.getStatus());
        record.setScore(result.getScore());
        record.setRuntimeMs(result.getRuntimeMs());
        record.setErrorMessage(result.getErrorMessage());
        try {
            record.setResultPreview(result.getResultPreview() == null ? "{}" : objectMapper.writeValueAsString(result.getResultPreview()));
        } catch (Exception ex) {
            record.setResultPreview("{}");
        }
        submissionMapper.insert(record);
        if (request.getExamId() != null) {
            // Validate exam exists, is published, and student is enrolled
            var exam = examMapper.findById(request.getExamId());
            if (exam == null) {
                throw BusinessException.notFound("考试不存在");
            }
            if (!"PUBLISHED".equals(exam.getStatus())) {
                throw BusinessException.badRequest("考试未发布，不能提交");
            }
            if (examMapper.countExamStudent(request.getExamId(), CurrentUser.id()) == 0) {
                throw BusinessException.forbidden("您未参加该考试");
            }
            examService.recalculateFinalScore(request.getExamId(), CurrentUser.id());
            examService.markSubmitted(request.getExamId(), CurrentUser.id());
        }
        return result;
    }

    public List<Map<String, Object>> mine() {
        return submissionMapper.listMine(CurrentUser.id());
    }
}
