"""Python port of the watch's RuleBasedClassifier, used as the comparison baseline.

Thresholds and per-category values mirror ClassifierThresholds and DefaultCategoryProfiles in
core-domain. Only the activation level is ported; restlessness is not part of the model comparison.
"""

from stateai_ml.features import WindowFeatures

LOW_MAX_HEART_RATE_DELTA = 5.0
HIGH_MIN_HEART_RATE_DELTA = 12.0
SENSITIVITY_FACTOR = {"DEEP_WORK": 1.25, "STUDY": 1.0, "READING": 0.8, "COLLAB": 1.0, "OTHER": 1.0}
MOVEMENT_LIMIT = {"DEEP_WORK": 0.25, "STUDY": 0.25, "READING": 0.15, "COLLAB": 0.8, "OTHER": 0.45}
LEVELS = ("LOW", "MEDIUM", "HIGH")


def classify(features: WindowFeatures, resting_heart_rate: float, category: str) -> str | None:
    """Activation level of a clean window, or None without heart rate."""
    if features.mean_heart_rate is None:
        return None
    delta = features.mean_heart_rate - resting_heart_rate
    factor = SENSITIVITY_FACTOR[category]
    if delta >= HIGH_MIN_HEART_RATE_DELTA * factor:
        return "HIGH"
    if delta <= LOW_MAX_HEART_RATE_DELTA * factor and features.mean_movement <= MOVEMENT_LIMIT[category]:
        return "LOW"
    return "MEDIUM"
