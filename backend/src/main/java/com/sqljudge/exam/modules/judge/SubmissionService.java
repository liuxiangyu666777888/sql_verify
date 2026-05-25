package com.sqljudge.exam.modules.judge;

import com.sqljudge.exam.common.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SubmissionService {
    private final JudgeService judgeService;
    private final SubmissionMapper submissionMapper;

    public SubmissionService(JudgeService judgeService, SubmissionMapper submissionMapper) {
        this.judgeService = judgeService;
        this.submissionMapper = submissionMapper;
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
        record.setResultPreview("{}");
        submissionMapper.insert(record);
        return result;
    }

    public List<Map<String, Object>> mine() {
        return submissionMapper.listMine(CurrentUser.id());
    }
}
