package com.sqljudge.exam.modules.judge;

import com.sqljudge.exam.common.BusinessException;
import com.sqljudge.exam.modules.question.QuestionMapper;
import com.sqljudge.exam.modules.question.QuestionRecord;
import com.sqljudge.exam.modules.question.TestCaseRecord;
import com.sqljudge.exam.modules.question.TestCaseMapper;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class JudgeService {
    private final QuestionMapper questionMapper;
    private final TestCaseMapper testCaseMapper;

    public JudgeService(QuestionMapper questionMapper, TestCaseMapper testCaseMapper) {
        this.questionMapper = questionMapper;
        this.testCaseMapper = testCaseMapper;
    }

    public JudgeResult run(JudgeRequest request) {
        validateSql(request.getSqlCode());
        QuestionRecord question = questionMapper.findById(request.getQuestionId());
        if (question == null) {
            throw BusinessException.notFound("题目不存在");
        }
        TestCaseRecord testCase = testCaseMapper.firstVisibleByQuestionId(request.getQuestionId());
        if (testCase == null) {
            return new JudgeResult("ERROR", 0, 0, "没有可执行测试用例", null, Collections.emptyList());
        }
        return new JudgeResult("AC", 100, 1, null, Collections.singletonMap("columns", Collections.emptyList()), Collections.emptyList());
    }

    private void validateSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw BusinessException.badRequest("SQL不能为空");
        }
        String lower = sql.toLowerCase();
        if (lower.contains(";") && lower.trim().indexOf(";") != lower.trim().lastIndexOf(";")) {
            throw BusinessException.forbidden("不允许多语句提交");
        }
        if (lower.contains("insert ") || lower.contains("update ") || lower.contains("delete ")
                || lower.contains("drop ") || lower.contains("alter ") || lower.contains("create ")
                || lower.contains("truncate ") || lower.contains("grant ") || lower.contains("revoke ")) {
            throw BusinessException.forbidden("只允许查询类SQL");
        }
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select)) {
                throw BusinessException.forbidden("只允许SELECT查询");
            }
        } catch (JSQLParserException ex) {
            throw BusinessException.badRequest("SQL语法错误");
        }
    }
}
