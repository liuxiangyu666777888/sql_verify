from flask import request
from functools import wraps
from flask_restful import abort
import models
from config import *
# permissions
def get_session():
    return request.headers.get('session')

def auth_role(role):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            session = get_session()
            if session is None:
                abort(HTTP_BAD_REQUEST)
            if role == AUTH_ALL:
                user = models.User.query.filter_by(session=session).first()
            else:
                user = models.User.query.filter_by(session=session, role=role).first()
            if user is None:
                abort(HTTP_UNAUTHORIZED)
            return func(*args, **kwargs)
        return wrapper
    return decorator
