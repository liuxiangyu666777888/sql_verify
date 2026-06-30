package com.sqljudge.exam.modules.question;

import com.sqljudge.exam.common.BusinessException;
import com.sqljudge.exam.common.CurrentUser;
import com.sqljudge.exam.common.PageResponse;
import com.sqljudge.exam.modules.question.dto.QuestionSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {
    private final QuestionMapper questionMapper;
    private final TestCaseMapper testCaseMapper;

    public QuestionService(QuestionMapper questionMapper, TestCaseMapper testCaseMapper) {
        this.questionMapper = questionMapper;
        this.testCaseMapper = testCaseMapper;
    }

    public List<QuestionSummary> list(String keyword) {
        return toSummaries(questionMapper.listVisible(keyword));
    }

    public PageResponse<QuestionSummary> page(String keyword, int page, int size) {
        int normalizedPage = Math.max(page, 1);
        int normalizedSize = Math.min(Math.max(size, 1), 100);
        int offset = (normalizedPage - 1) * normalizedSize;
        return new PageResponse<>(
                toSummaries(questionMapper.listVisiblePage(keyword, offset, normalizedSize)),
                questionMapper.countVisible(keyword),
                normalizedPage,
                normalizedSize
        );
    }

    private List<QuestionSummary> toSummaries(List<QuestionRecord> records) {
        List<QuestionSummary> summaries = new ArrayList<>();
        for (QuestionRecord record : records) {
            QuestionSummary summary = new QuestionSummary();
            summary.setQuestionId(record.getQuestionId());
            summary.setTitle(record.getTitle());
            summary.setDifficulty(record.getDifficulty());
            summary.setTags(record.getTags());
            summaries.add(summary);
        }
        return summaries;
    }

    public QuestionRecord detail(Long id) {
        QuestionRecord record = questionMapper.findById(id);
        if (record == null || record.getVisible() == null || record.getVisible() == 0) {
            throw BusinessException.notFound("题目不存在");
        }
        return record;
    }

    public QuestionDetailResponse detailResponse(Long id) {
        QuestionRecord record = detail(id);
        QuestionDetailResponse response = new QuestionDetailResponse();
        response.setQuestionId(record.getQuestionId());
        response.setTitle(record.getTitle());
        response.setDescription(record.getDescription());
        response.setDifficulty(record.getDifficulty());
        response.setSourceSchemaSql(record.getSourceSchemaSql());
        response.setTags(record.getTags());
        if (canManageQuestions()) {
            response.setAnswerSql(record.getAnswerSql());
            response.setTestCases(testCaseMapper.listByQuestionId(id));
        }
        return response;
    }

    public QuestionRecord create(QuestionRequest request) {
        QuestionRecord record = toRecord(request);
        record.setCreatorId(CurrentUser.id());
        questionMapper.insert(record);
        return record;
    }

    public QuestionRecord update(Long id, QuestionRequest request) {
        QuestionRecord existing = detail(id);
        QuestionRecord record = toRecord(request);
        record.setQuestionId(id);
        record.setCreatorId(existing.getCreatorId());
        questionMapper.update(record);
        return questionMapper.findById(id);
    }

    public void delete(Long id) {
        detail(id);
        questionMapper.hide(id);
    }

    private QuestionRecord toRecord(QuestionRequest request) {
        if (request == null) {
            throw BusinessException.badRequest("题目信息不能为空");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw BusinessException.badRequest("题目标题不能为空");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw BusinessException.badRequest("题目描述不能为空");
        }
        if (request.getAnswerSql() == null || request.getAnswerSql().trim().isEmpty()) {
            throw BusinessException.badRequest("参考答案不能为空");
        }
        QuestionRecord record = new QuestionRecord();
        record.setTitle(request.getTitle().trim());
        record.setDescription(request.getDescription().trim());
        record.setDifficulty(normalizeDifficulty(request.getDifficulty()));
        record.setAnswerSql(request.getAnswerSql().trim());
        record.setSourceSchemaSql(request.getSourceSchemaSql());
        record.setTags(normalizeTags(request.getTags()));
        record.setVisible(request.getVisible() == null ? 1 : request.getVisible());
        return record;
    }

    private String normalizeDifficulty(String difficulty) {
        if (difficulty == null || difficulty.trim().isEmpty()) {
            return "MEDIUM";
        }
        String value = difficulty.trim().toUpperCase();
        if (!"EASY".equals(value) && !"MEDIUM".equals(value) && !"HARD".equals(value)) {
            throw BusinessException.badRequest("题目难度不合法");
        }
        return value;
    }

    private String normalizeTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return "[]";
        }
        return tags.trim();
    }

    private boolean canManageQuestions() {
        String role = CurrentUser.role();
        return "TEACHER".equals(role) || "ASSISTANT".equals(role) || "ADMIN".equals(role);
    }
}
