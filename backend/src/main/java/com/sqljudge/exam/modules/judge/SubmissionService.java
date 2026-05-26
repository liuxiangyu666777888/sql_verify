package com.sqljudge.exam.modules.judge;

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
    private final ObjectMapper objectMapper;

    public SubmissionService(JudgeService judgeService, SubmissionMapper submissionMapper, com.sqljudge.exam.modules.exam.ExamService examService, ObjectMapper objectMapper) {
        this.judgeService = judgeService;
        this.submissionMapper = submissionMapper;
        this.examService = examService;
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
            examService.recalculateFinalScore(request.getExamId(), CurrentUser.id());
            examService.markSubmitted(request.getExamId(), CurrentUser.id());
        }
        return result;
    }

    public List<Map<String, Object>> mine() {
        return submissionMapper.listMine(CurrentUser.id());
    }
}
