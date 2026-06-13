package com.sqljudge.exam.modules.dashboard;

import com.sqljudge.exam.common.CurrentUser;
import com.sqljudge.exam.modules.classes.ClassMapper;
import com.sqljudge.exam.modules.judge.SubmissionMapper;
import com.sqljudge.exam.modules.exam.ExamMapper;
import com.sqljudge.exam.modules.question.QuestionMapper;
import com.sqljudge.exam.modules.question.QuestionRecord;
import com.sqljudge.exam.modules.question.dto.QuestionSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    private final SubmissionMapper submissionMapper;
    private final QuestionMapper questionMapper;
    private final ExamMapper examMapper;
    private final ClassMapper classMapper;

    public DashboardService(SubmissionMapper submissionMapper, QuestionMapper questionMapper, ExamMapper examMapper, ClassMapper classMapper) {
        this.submissionMapper = submissionMapper;
        this.questionMapper = questionMapper;
        this.examMapper = examMapper;
        this.classMapper = classMapper;
    }

    public Map<String, Object> student() {
        Long userId = CurrentUser.id();
        int total = submissionMapper.totalCount(userId);
        int ac = submissionMapper.acCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("solvedCount", submissionMapper.solvedCount(userId));
        data.put("accuracyRate", total == 0 ? 0 : Math.round(ac * 10000.0 / total) / 100.0);
        data.put("streakDays", streakDays(submissionMapper.recentAcDates(userId)));
        data.put("recommendedQuestions", questionSummaries(questionMapper.listVisible(null)));
        data.put("upcomingExams", examMapper.listByStudent(userId));
        return data;
    }

    public Map<String, Object> teacher() {
        Long teacherId = CurrentUser.id();
        Map<String, Object> data = new HashMap<>();
        data.put("activeClasses", classMapper.countByTeacher(teacherId));
        data.put("totalProblems", questionMapper.listVisible(null).size());
        data.put("pendingReviews", submissionMapper.pendingReviewCount(teacherId));
        data.put("recentExams", examMapper.listByCreator(teacherId));
        return data;
    }

    private int streakDays(List<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return 0;
        }
        LocalDate cursor = LocalDate.now();
        int streak = 0;
        for (LocalDate date : dates) {
            if (date.equals(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            } else if (streak == 0 && date.equals(cursor.minusDays(1))) {
                streak++;
                cursor = cursor.minusDays(2);
            } else if (date.isBefore(cursor)) {
                break;
            }
        }
        return streak;
    }

    private List<QuestionSummary> questionSummaries(List<QuestionRecord> records) {
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
}
