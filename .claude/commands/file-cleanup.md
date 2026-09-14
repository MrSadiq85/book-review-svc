# file-cleanup

Move files from `working/` directory to `archive/` directory and commit with task-specific message.

## Usage

```
/file-cleanup <task>
```

## Arguments

- `task` (required): Description of the work completed (e.g., "API implementation", "bug fixes")

## Example

```
/file-cleanup "API endpoint implementation"
```

## Behavior

1. Creates `archive/` directory if it doesn't exist
2. Creates task-specific subdirectory under `archive/<task>/`
3. Moves all files from `working/` to `archive/<task>/`
4. Stages changes with `git add -A`
5. Commits with message: `Work done by Sadique and Claude-code\n\nTask: <task>`
6. Removes empty `working/` directory

---

```bash
#!/bin/bash

set -e

if [ -z "$1" ]; then
    echo "Error: task argument is required"
    echo "Usage: /file-cleanup <task>"
    exit 1
fi

TASK="$1"
ARCHIVE_DIR="archive"
TASK_DIR="$ARCHIVE_DIR/$TASK"

mkdir -p "$TASK_DIR"

if [ ! -d "working" ] || [ -z "$(ls -A working 2>/dev/null)" ]; then
    echo "Warning: working/ directory is empty or does not exist"
    exit 0
fi

echo "Moving files from working/ to archive/$TASK/..."
mv working/* "$TASK_DIR/" 2>/dev/null || true

rmdir working 2>/dev/null || true

git add -A

COMMIT_MESSAGE="Work done by Sadique and Claude-code

Task: $TASK"

git commit -m "$COMMIT_MESSAGE"

echo "✓ Files moved to archive/$TASK/ and committed successfully"
echo "  Task: $TASK"
```
