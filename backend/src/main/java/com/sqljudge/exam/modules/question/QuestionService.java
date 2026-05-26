package com.sqljudge.exam.modules.question;

import com.sqljudge.exam.common.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {
    private final QuestionMapper questionMapper;

    public QuestionService(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    public List<QuestionRecord> list(String keyword) {
        return questionMapper.listVisible(keyword);
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
        return response;
    }
}
