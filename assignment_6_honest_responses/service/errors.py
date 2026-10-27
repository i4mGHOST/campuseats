from flask import make_response, jsonify

def problem(status, title, detail, error_type=None, errors=None):
    """
    Helper function to generate a standardized RFC 7807 Problem Details response.
    """
    body = {
        "type": error_type or f"https://api.campuseats.com/errors/{title.lower().replace(' ', '-')}",
        "title": title,
        "status": status,
        "detail": detail
    }
    
    # If field-level validation errors exist, include the errors array (Part B3)
    if errors:
        body["errors"] = errors

    response = make_response(jsonify(body), status)
    response.headers["Content-Type"] = "application/problem+json; charset=utf-8"
    return response