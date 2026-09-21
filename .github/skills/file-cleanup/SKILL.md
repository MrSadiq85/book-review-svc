---
name: file-cleanup
description: Move files from working/ to archived/<task>/ and stage them in git.
argument-hint: "[task-name]"
---

# File Cleanup Skill

This skill organizes completed work by moving files from the `working/` directory to a task-specific subdirectory under `archived/` and staging them in Git.

## Instructions

1. **Check for Task Name**: Check if the user provided a task name as an argument (`$args` or input).
2. **Prompt if Missing**: If any text is provided by user then use its as TaskName, if user didn't provided any single text then politely ask the user: *"Please provide a task name or description for this cleanup:"* and wait for their input.
3. **Execute PowerShell Script**: Run the companion PowerShell script (`file-cleanup.ps1`) located in the same directory, passing the task name as an argument.

## Execution

```powershell
# Run the PowerShell script with the provided task name
powershell -File "${skill_dir}/file-cleanup.ps1" -TaskName "<task-name>"
```