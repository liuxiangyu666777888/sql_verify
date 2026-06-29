package com.sqljudge.exam.modules.judge;

import com.sqljudge.exam.common.BusinessException;
import com.sqljudge.exam.modules.question.QuestionMapper;
import com.sqljudge.exam.modules.question.QuestionRecord;
import com.sqljudge.exam.modules.question.TestCaseRecord;
import com.sqljudge.exam.modules.question.TestCaseMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class JudgeService {
    private static final Logger log = LoggerFactory.getLogger(JudgeService.class);

    private static final Pattern DANGEROUS_KEYWORDS =
            Pattern.compile("\\b(insert|update|delete|drop|alter|create|truncate|grant|revoke)\\b",
                    Pattern.CASE_INSENSITIVE);

    private final QuestionMapper questionMapper;
    private final TestCaseMapper testCaseMapper;
    private final ObjectMapper objectMapper;

    @Value("${app.judge.host}")
    private String host;

    @Value("${app.judge.port}")
    private int port;

    @Value("${app.judge.username}")
    private String username;

    @Value("${app.judge.password}")
    private String password;

    @Value("${app.judge.timeout-seconds}")
    private int timeoutSeconds;

    @Value("${app.judge.max-rows}")
    private int maxRows;

    public JudgeService(QuestionMapper questionMapper, TestCaseMapper testCaseMapper, ObjectMapper objectMapper) {
        this.questionMapper = questionMapper;
        this.testCaseMapper = testCaseMapper;
        this.objectMapper = objectMapper;
    }

    public JudgeResult run(JudgeRequest request) {
        validateSql(request.getSqlCode());
        QuestionRecord question = questionMapper.findById(request.getQuestionId());
        if (question == null) {
            throw BusinessException.notFound("题目不存在");
        }
        List<TestCaseRecord> testCases = testCaseMapper.listVisibleByQuestionId(request.getQuestionId());
        if (testCases.isEmpty()) {
            return new JudgeResult("ERROR", 0, 0, "没有可执行测试用例", null, Collections.emptyList());
        }
        String schema = "judge_" + request.getQuestionId() + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        List<Map<String, Object>> details = new ArrayList<>();
        long start = System.currentTimeMillis();
        int passed = 0;
        String status = "AC";
        String errorMessage = null;
        Map<String, Object> preview = null;
        try {
            for (TestCaseRecord testCase : testCases) {
                JudgeCaseResult caseResult = executeCase(schema, testCase, request.getSqlCode());
                if (preview == null) {
                    preview = caseResult.preview;
                }
                Map<String, Object> detail = new HashMap<>();
                detail.put("caseId", testCase.getCaseId());
                detail.put("passed", caseResult.passed);
                detail.put("message", caseResult.message);
                details.add(detail);
                if (caseResult.passed) {
                    passed++;
                } else if ("WA".equals(caseResult.status)) {
                    status = "WA";
                } else {
                    status = caseResult.status;
                    errorMessage = caseResult.message;
                    break;
                }
            }
        } finally {
            dropSchema(schema);
        }
        double score = Math.round((passed * 10000.0 / testCases.size())) / 100.0;
        if (passed == testCases.size()) {
            status = "AC";
        }
        return new JudgeResult(status, score, (int) (System.currentTimeMillis() - start), errorMessage, preview, details);
    }

    private JudgeCaseResult executeCase(String schema, TestCaseRecord testCase, String studentSql) {
        String adminUrl = "jdbc:mysql://" + host + ":" + port + "/?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowMultiQueries=true&allowPublicKeyRetrieval=true";
        String schemaUrl = "jdbc:mysql://" + host + ":" + port + "/" + schema + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowMultiQueries=false&allowPublicKeyRetrieval=true";
        try (Connection admin = DriverManager.getConnection(adminUrl, username, password)) {
            try (java.sql.Statement stmt = admin.createStatement()) {
                stmt.execute("CREATE DATABASE IF NOT EXISTS " + schema);
            }
            try (Connection conn = DriverManager.getConnection(schemaUrl, username, password)) {
                try (java.sql.Statement init = conn.createStatement()) {
                    init.execute(testCase.getInputSql());
                }
                try (java.sql.Statement run = conn.createStatement()) {
                    run.setQueryTimeout(timeoutSeconds);
                    try (ResultSet rs = run.executeQuery(studentSql)) {
                        Map<String, Object> actual = readResult(rs);
                        Map<String, Object> expected = readJson(testCase.getExpectedOutput());
                        boolean passed = compare(actual, expected);
                        return new JudgeCaseResult(passed ? "AC" : "WA", passed, passed ? "通过" : "结果不匹配", actual);
                    }
                }
            }
        } catch (java.sql.SQLTimeoutException ex) {
            return new JudgeCaseResult("TLE", false, "执行超时", null);
        } catch (Exception ex) {
            String msg = ex.getMessage() == null ? "执行失败" : ex.getMessage();
            return new JudgeCaseResult("ERROR", false, msg, null);
        }
    }

    @PostConstruct
    private void cleanOrphanedSchemas() {
        String adminUrl = "jdbc:mysql://" + host + ":" + port + "/?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowMultiQueries=true&allowPublicKeyRetrieval=true";
        try (Connection admin = DriverManager.getConnection(adminUrl, username, password);
             java.sql.Statement stmt = admin.createStatement()) {
            ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE 'judge_%'");
            while (rs.next()) {
                String db = rs.getString(1);
                try {
                    stmt.execute("DROP DATABASE IF EXISTS " + db);
                    log.info("清理孤儿 schema: {}", db);
                } catch (Exception e) {
                    log.warn("清理孤儿 schema 失败: {}", db, e);
                }
            }
        } catch (Exception e) {
            log.warn("启动时扫描孤儿 schema 失败", e);
        }
    }

    private void dropSchema(String schema) {
        String adminUrl = "jdbc:mysql://" + host + ":" + port + "/?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowMultiQueries=true&allowPublicKeyRetrieval=true";
        try (Connection admin = DriverManager.getConnection(adminUrl, username, password);
             java.sql.Statement stmt = admin.createStatement()) {
            stmt.execute("DROP DATABASE IF EXISTS " + schema);
        } catch (Exception e) {
            log.warn("删除临时 schema 失败: {}", schema, e);
        }
    }

    private Map<String, Object> readResult(ResultSet rs) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        List<String> columns = new ArrayList<>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            columns.add(meta.getColumnLabel(i));
        }
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next() && rows.size() < maxRows) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                Object value = rs.getObject(i);
                if (value instanceof BigDecimal) {
                    BigDecimal bd = ((BigDecimal) value).stripTrailingZeros();
                    value = bd.scale() <= 0 ? bd.longValue() : bd.doubleValue();
                }
                row.add(value);
            }
            rows.add(row);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("columns", columns);
        result.put("rows", rows);
        return result;
    }

    private Map<String, Object> readJson(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        Map<String, Object> result = new HashMap<>();
        result.put("columns", objectMapper.convertValue(node.get("columns"), List.class));
        result.put("rows", objectMapper.convertValue(node.get("rows"), List.class));
        result.put("orderSensitive", node.has("orderSensitive") && node.get("orderSensitive").asBoolean());
        return result;
    }

    private boolean compare(Map<String, Object> actual, Map<String, Object> expected) throws Exception {
        List<?> actualColumns = (List<?>) actual.get("columns");
        List<?> expectedColumns = (List<?>) expected.get("columns");
        if (!actualColumns.equals(expectedColumns)) {
            return false;
        }
        List<List<Object>> actualRows = (List<List<Object>>) actual.get("rows");
        List<List<Object>> expectedRows = (List<List<Object>>) expected.get("rows");
        boolean orderSensitive = Boolean.TRUE.equals(expected.get("orderSensitive"));
        if (!orderSensitive) {
            Comparator<List<Object>> comparator = Comparator.comparing(this::rowKey);
            actualRows = new ArrayList<>(actualRows);
            expectedRows = new ArrayList<>(expectedRows);
            actualRows.sort(comparator);
            expectedRows.sort(comparator);
        }
        if (actualRows.size() != expectedRows.size()) {
            return false;
        }
        for (int i = 0; i < actualRows.size(); i++) {
            if (!normalizeRow(actualRows.get(i)).equals(normalizeRow(expectedRows.get(i)))) {
                return false;
            }
        }
        return true;
    }

    private String rowKey(List<Object> row) {
        return normalizeRow(row).toString();
    }

    private List<Object> normalizeRow(List<Object> row) {
        List<Object> normalized = new ArrayList<>();
        for (Object item : row) {
            if (item == null) {
                normalized.add(null);
            } else if (item instanceof Number) {
                double v = ((Number) item).doubleValue();
                if (Math.floor(v) == v) {
                    normalized.add((long) v);
                } else {
                    normalized.add(v);
                }
            } else {
                normalized.add(String.valueOf(item));
            }
        }
        return normalized;
    }

    private static class JudgeCaseResult {
        private final String status;
        private final boolean passed;
        private final String message;
        private final Map<String, Object> preview;

        private JudgeCaseResult(String status, boolean passed, String message, Map<String, Object> preview) {
            this.status = status;
            this.passed = passed;
            this.message = message;
            this.preview = preview;
        }
    }

    private void validateSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw BusinessException.badRequest("SQL不能为空");
        }
        if (sql.trim().indexOf(';') != sql.trim().lastIndexOf(';')) {
            throw BusinessException.forbidden("不允许多语句提交");
        }
        if (DANGEROUS_KEYWORDS.matcher(sql).find()) {
            throw BusinessException.forbidden("只允许查询类SQL");
        }
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select)) {
                throw BusinessException.forbidden("只允许SELECT查询");
            }
            Select select = (Select) statement;
            if (!(select.getSelectBody() instanceof PlainSelect)) {
                throw BusinessException.forbidden("只允许单个SELECT查询");
            }
        } catch (JSQLParserException ex) {
            throw BusinessException.badRequest("SQL语法错误");
        }
    }
}
