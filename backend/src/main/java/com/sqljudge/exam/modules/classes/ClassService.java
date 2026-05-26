package com.sqljudge.exam.modules.classes;

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
}
