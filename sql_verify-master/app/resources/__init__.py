from datetime import datetime


def parse_iso_datetime(iso_str):
    return datetime.fromisoformat(iso_str.replace('Z', '+00:00'))


def model_to_dict(obj):
    def convert(value):
        if isinstance(value, datetime):
            return value.strftime("%Y-%m-%d %H:%M:%S")
        return value
    return {c.name: convert(getattr(obj, c.name)) for c in obj.__table__.columns}
