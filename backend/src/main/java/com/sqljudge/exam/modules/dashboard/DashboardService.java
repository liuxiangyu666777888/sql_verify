package com.sqljudge.exam.modules.dashboard;

import com.sqljudge.exam.common.CurrentUser;
import com.sqljudge.exam.modules.judge.SubmissionMapper;
import com.sqljudge.exam.modules.exam.ExamMapper;
import com.sqljudge.exam.modules.question.QuestionMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {
    private final SubmissionMapper submissionMapper;
    private final QuestionMapper questionMapper;
    private final ExamMapper examMapper;

    public DashboardService(SubmissionMapper submissionMapper, QuestionMapper questionMapper, ExamMapper examMapper) {
        this.submissionMapper = submissionMapper;
        this.questionMapper = questionMapper;
        this.examMapper = examMapper;
    }

    public Map<String, Object> student() {
        Long userId = CurrentUser.id();
        int total = submissionMapper.totalCount(userId);
        int ac = submissionMapper.acCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("solvedCount", submissionMapper.solvedCount(userId));
        data.put("accuracyRate", total == 0 ? 0 : Math.round(ac * 10000.0 / total) / 100.0);
        data.put("streakDays", 0);
        data.put("recommendedQuestions", questionMapper.listVisible(null));
        data.put("upcomingExams", examMapper.listByStudent(userId));
        return data;
    }

    public Map<String, Object> teacher() {
        Map<String, Object> data = new HashMap<>();
        data.put("activeClasses", 1);
        data.put("totalProblems", questionMapper.listVisible(null).size());
        data.put("pendingReviews", 0);
        data.put("recentExams", examMapper.listByCreator(CurrentUser.id()));
        return data;
    }
}
