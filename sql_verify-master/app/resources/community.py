from flask import request, jsonify
from flask_restful import Resource
from sqlalchemy import func
from ..config import *
from .. import models
from ..permissions import auth_role
from . import model_to_dict, parse_iso_datetime


class Community(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        article_id = request.args.get('article_id')
        if not article_id:
            return {"message": "缺少article_id参数"}, HTTP_BAD_REQUEST
        article_id = int(article_id)
        ret = models.Article.query.filter_by(id=article_id).first()
        if ret:
            return model_to_dict(ret), HTTP_OK
        else:
            return {"message": "文章不存在"}, HTTP_NOT_FOUND

    @auth_role(AUTH_ALL)
    def post(self):
        data = request.get_json()
        if not data:
            return {"message": "无效的请求数据"}, HTTP_BAD_REQUEST
        try:
            user_id = int(data.get('user_id'))
        except ValueError:
            return {"message": "无效的用户ID"}, HTTP_BAD_REQUEST

        max_id = db.session.query(func.max(models.Article.id)).scalar()
        max_id = max_id + 1 if max_id else 1
        article = models.Article(
            id=max_id, title=data.get('title'), content=data.get('content'),
            user_id=user_id,
            question_id=int(data.get('question_id')) if data.get('question_id') else None,
            is_notice=data.get('is_notice', False),
            publish_time=parse_iso_datetime(data.get('publish_time')),
            last_modify_time=parse_iso_datetime(data.get('last_modify_time'))
        )
        if article.title and article.content:
            db.session.add(article)
            db.session.commit()
            return {}, HTTP_CREATED
        else:
            return {"message": "文章缺少标题或内容！"}, HTTP_BAD_REQUEST

    @auth_role(AUTH_ALL)
    def put(self):
        data = request.get_json()
        article_id = data.get('article_id')
        if not article_id:
            return {"message": "缺少article_id参数"}, HTTP_BAD_REQUEST
        article_id = int(article_id)
        article = models.Article.query.filter_by(id=article_id).first()
        if not article:
            return {"message": "未找到文章！"}, HTTP_NOT_FOUND
        role = data.get('role', AUTH_STUDENT)
        user_id = data.get('user_id')
        if role == AUTH_STUDENT and article.user_id != user_id:
            return {"message": "没有编辑权限！"}, HTTP_FORBIDDEN
        new_content = data.get('new_content')
        if new_content:
            article.content = new_content
            db.session.commit()
            return {}, HTTP_OK
        else:
            return {"message": "缺少新的内容"}, HTTP_BAD_REQUEST

    @auth_role(AUTH_ADMIN)
    def delete(self):
        article_id = request.args.get('article_id')
        if not article_id:
            return {"message": "缺少article_id参数"}, HTTP_BAD_REQUEST
        article_id = int(article_id)
        article = models.Article.query.filter_by(id=article_id).first()
        if article:
            db.session.delete(article)
            db.session.commit()
            return {}, HTTP_OK
        else:
            return {"message": "未找到文章！"}, HTTP_NOT_FOUND


class CommunityList(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        user_id = int(request.args.get('user_id')) if request.args.get('user_id') else None
        if user_id:
            articles = models.Article.query.filter_by(user_id=user_id)
        else:
            articles = models.Article.query.all()
        data = [model_to_dict(article) for article in articles]
        return jsonify(data)
