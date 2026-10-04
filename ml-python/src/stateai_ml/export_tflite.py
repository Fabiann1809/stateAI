"""Exports the state classifier to TensorFlow Lite and verifies it against the original model.

``python -m stateai_ml.export_tflite`` reads ``models/state_classifier.json`` and writes
``models/state_classifier.tflite``. Requires the packages in ``requirements-export.txt``.
"""

import argparse
import json
from pathlib import Path

import numpy as np

from stateai_ml.model import LinearModel

TOLERANCE = 1e-5
VERIFICATION_SAMPLES = 2000


def load_model(path: Path) -> LinearModel:
    data = json.loads(path.read_text(encoding="utf-8"))
    return LinearModel(*(np.array(data[key], dtype=float) for key in ("mean", "scale", "weights", "bias")))


def folded_parameters(model: LinearModel) -> tuple[np.ndarray, np.ndarray]:
    """Folds standardization into the dense layer: ``W' = W / scale``, ``b' = b - W' . mean``."""
    kernel = (model.weights / model.scale).T
    bias = model.bias - model.mean @ kernel
    return kernel.astype(np.float32), bias.astype(np.float32)


def to_tflite(model: LinearModel) -> bytes:
    import tensorflow as tf  # noqa: PLC0415 - heavy optional dependency, only needed to export

    kernel, bias = folded_parameters(model)
    keras_model = tf.keras.Sequential(
        [tf.keras.Input(shape=(kernel.shape[0],)), tf.keras.layers.Dense(kernel.shape[1], activation="softmax")]
    )
    keras_model.layers[0].set_weights([kernel, bias])
    return tf.lite.TFLiteConverter.from_keras_model(keras_model).convert()


def tflite_probabilities(flatbuffer: bytes, inputs: np.ndarray) -> np.ndarray:
    import tensorflow as tf  # noqa: PLC0415

    interpreter = tf.lite.Interpreter(model_content=flatbuffer)
    input_index = interpreter.get_input_details()[0]["index"]
    output_index = interpreter.get_output_details()[0]["index"]
    interpreter.resize_tensor_input(input_index, inputs.shape)
    interpreter.allocate_tensors()
    interpreter.set_tensor(input_index, inputs.astype(np.float32))
    interpreter.invoke()
    return interpreter.get_tensor(output_index)


def verification_inputs(model: LinearModel, seed: int = 0) -> np.ndarray:
    """Inputs spread over the training distribution (mean +/- 3 standard deviations)."""
    rng = np.random.default_rng(seed)
    return model.mean + rng.uniform(-3, 3, (VERIFICATION_SAMPLES, len(model.mean))) * model.scale


def max_difference(model: LinearModel, flatbuffer: bytes) -> float:
    inputs = verification_inputs(model)
    return float(np.abs(tflite_probabilities(flatbuffer, inputs) - model.probabilities(inputs)).max())


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--model", type=Path, default=Path("models/state_classifier.json"))
    parser.add_argument("--out", type=Path, default=Path("models/state_classifier.tflite"))
    arguments = parser.parse_args()
    model = load_model(arguments.model)
    flatbuffer = to_tflite(model)
    difference = max_difference(model, flatbuffer)
    if difference > TOLERANCE:
        raise SystemExit(f"TFLite model differs from the original by {difference:.2e}")
    arguments.out.write_bytes(flatbuffer)
    print(f"wrote {arguments.out} ({len(flatbuffer)} bytes), max probability difference {difference:.2e}")


if __name__ == "__main__":
    main()
