package com.sqljudge.exam.modules.classes;

import com.sqljudge.exam.common.BusinessException;
import com.sqljudge.exam.common.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ClassService {
    private final ClassMapper classMapper;

    public ClassService(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    public List<ClassRecord> listMine() {
        if ("STUDENT".equals(CurrentUser.role())) {
            return classMapper.listByStudent(CurrentUser.id());
        }
        return classMapper.listByTeacher(CurrentUser.id());
    }

    public ClassRecord create(ClassRequest request) {
        if (!"TEACHER".equals(CurrentUser.role()) && !"ADMIN".equals(CurrentUser.role())) {
            throw BusinessException.forbidden("只有教师或管理员可以创建班级");
        }
        if (request == null || request.getClassName() == null || request.getClassName().trim().isEmpty()) {
            throw BusinessException.badRequest("班级名称不能为空");
        }
        ClassRecord record = new ClassRecord();
        record.setClassName(request.getClassName().trim());
        record.setSemester(request.getSemester() == null ? null : request.getSemester().trim());
        record.setTeacherId(CurrentUser.id());
        record.setInviteCode(generateInviteCode());
        classMapper.insert(record);
        return record;
    }

    public void join(String inviteCode) {
        if (!"STUDENT".equals(CurrentUser.role())) {
            throw BusinessException.forbidden("只有学生可以加入班级");
        }
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            throw BusinessException.badRequest("邀请码不能为空");
        }
        ClassRecord record = classMapper.findByInviteCode(inviteCode.trim());
        if (record == null) {
            throw BusinessException.notFound("班级邀请码不存在");
        }
        classMapper.joinClass(CurrentUser.id(), record.getClassId());
    }

    private String generateInviteCode() {
        for (int i = 0; i < 5; i++) {
            String code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
            if (classMapper.findByInviteCode(code) == null) {
                return code;
            }
        }
        throw new BusinessException(50000, "邀请码生成失败");
    }
}
