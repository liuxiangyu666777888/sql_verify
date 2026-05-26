package com.sqljudge.exam.modules.assistant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssistantStudentService {
    private final AssistantStudentMapper mapper;

    public AssistantStudentService(AssistantStudentMapper mapper) {
        this.mapper = mapper;
    }

    public List<AssistantStudentRecord> listByAssistant(Long assistantId) {
        return mapper.listByAssistant(assistantId);
    }

    @Transactional
    public void assign(List<Long> studentIds, Long assistantId) {
        for (Long sid : studentIds) {
            mapper.deleteByStudent(sid);
            mapper.insert(assistantId, sid);
        }
    }

    public void remove(Long assistantId, Long studentId) {
        mapper.delete(assistantId, studentId);
    }
}
