package com.sqljudge.exam.modules.classes;

import com.sqljudge.exam.common.BusinessException;
import com.sqljudge.exam.common.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
