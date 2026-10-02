.PHONY: env run

PYTHON_VERSION := 3.13

env:
	uv venv --clear --python $(PYTHON_VERSION)
	uv pip install --python .venv/bin/python -r requirements.txt

run:
	uv run --python .venv/bin/python app.py
