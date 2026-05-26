from flask import request, jsonify
from flask_restful import Resource
from ..config import *
from .. import models
from ..permissions import auth_role


class TeacherDashboard(Resource):
    @auth_role(AUTH_TEACHER)
    def get(self):
        teacher_id = int(request.args.get('teacher_id'))
        if not teacher_id:
            return {"message": "缺少教师ID"}, HTTP_BAD_REQUEST

        classes = models.Class.query.filter_by(teacher_id=teacher_id).all()
        class_ids = [c.id for c in classes]
        class_stats = []
        total_students = 0
        for c in classes:
            cnt = models.StudentClass.query.filter_by(class_id=c.id).count()
            total_students += cnt
            class_stats.append({"id": c.id, "name": c.name, "student_count": cnt})

        question_count = models.Question.query.filter_by(teacher_id=teacher_id).count()

        exams = models.Exam.query.filter_by(teacher_id=teacher_id).all()
        exam_stats = []
        for e in exams:
            q_cnt = models.ExamQuestion.query.filter_by(exam_id=e.id).count()
            s_cnt = models.ExamStudent.query.filter_by(exam_id=e.id).count()
            exam_stats.append({
                "id": e.id, "name": e.name,
                "start_time": str(e.start_time), "end_time": str(e.end_time),
                "question_count": q_cnt, "student_count": s_cnt
            })

        all_student_ids = []
        for c in classes:
            sc = models.StudentClass.query.filter_by(class_id=c.id).all()
            all_student_ids.extend([s.student_id for s in sc])

        total_submissions = 0
        accepted_submissions = 0
        if all_student_ids:
            total_submissions = models.Submission.query.filter(
                models.Submission.student_id.in_(all_student_ids)
            ).count()
            accepted_submissions = models.Submission.query.filter(
                models.Submission.student_id.in_(all_student_ids),
                models.Submission.status == JUDGE_ACCEPTED
            ).count()

        overall_pass_rate = round(accepted_submissions / total_submissions * 100, 1) if total_submissions > 0 else 0

        recent = []
        if all_student_ids:
            subs = models.Submission.query.filter(
                models.Submission.student_id.in_(all_student_ids)
            ).order_by(models.Submission.submit_time.desc()).limit(10).all()
            for s in subs:
                recent.append({
                    "id": s.id,
                    "student_name": s.student.username if s.student else None,
                    "question_title": s.question.title if s.question else None,
                    "status": s.status, "pass_rate": s.pass_rate,
                    "submit_time": str(s.submit_time)
                })

        return jsonify({
            "class_count": len(classes),
            "class_stats": class_stats,
            "total_students": total_students,
            "question_count": question_count,
            "exam_count": len(exams),
            "exam_stats": exam_stats,
            "total_submissions": total_submissions,
            "accepted_submissions": accepted_submissions,
            "overall_pass_rate": overall_pass_rate,
            "recent_submissions": recent
        })
