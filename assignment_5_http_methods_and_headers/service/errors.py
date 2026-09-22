from flask import jsonify, Response

def problem(status: int, title: str, detail: str, type_uri: str = "about:blank") -> Response:
    """Helper function enforcing RFC 7807 Problem Details error format across all endpoints."""
    payload = {
        "type": type_uri,
        "title": title,
        "status": status,
        "detail": detail
    }
    response = jsonify(payload)
    response.status_code = status
    response.headers["Content-Type"] = "application/problem+json"
    return response