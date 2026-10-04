"""Python mirror of the watch's ProfileBlender, for checking the category/activity blending."""

BLEND_K = 5


def own_weight(valid_sessions: int, k: int = BLEND_K) -> float:
    """``n / (n + K)``: how much an activity's own learning counts."""
    return valid_sessions / (valid_sessions + k)


def blend(category_value: float, learned_value: float, valid_sessions: int, k: int = BLEND_K) -> float:
    weight = own_weight(valid_sessions, k)
    return weight * learned_value + (1 - weight) * category_value
