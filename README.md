# SDLC Agentic Workflow

This document outlines the Software Development Lifecycle (SDLC) agentic workflow for the book-review-svc project. This workflow leverages specialized AI agents to streamline feature development from requirements gathering through pull request creation.

## Prerequisites

Before starting the SDLC workflow, ensure the following MCP servers are configured:
- **Jira MCP Server** — for issue tracking and requirement management
- **GitHub MCP Server** — for repository management and pull request creation

## Workflow Steps

### Step 1: Requirement Gathering
**Command:** `start requirement gather for issue KAN-3` (replace with your issue number)

**Purpose:** Extract and document functional and non-functional requirements from the Jira story.

**Agent Used:** `requirements-gathering`

**Outputs:**
- `working/requirements.md` — Structured requirements document

**Actions:**
- The agent reads the story from Jira
- Asks clarification questions one-by-one to refine understanding
- Captures final requirements in a structured format

---

### Step 2: Architecture Design
**Purpose:** Design high-level system architecture based on approved requirements.

**Agent Used:** `architecture-designer`

**Inputs:**
- `working/requirements.md` (from Step 1)

**Outputs:**
- `working/architecture.md` — Component diagrams and technology choices

**Actions:**
- Analyzes requirements and proposes architecture
- Iteratively refines based on your feedback
- Documents final architecture with component diagrams and design rationale

---

### Step 3: Architecture Review
**Purpose:** Conduct a structured design review to identify risks, gaps, and anti-patterns.

**Agent Used:** `design-reviewer`

**Inputs:**
- `working/architecture.md` (from Step 2)

**Outputs:**
- `working/design-review.md` — Review findings and agreed design decisions

**Actions:**
- Acts as a senior reviewer
- Identifies architectural risks and gaps
- Recommends clarifications and updates to architecture.md
- Documents review findings rather than making direct modifications

---

### Step 4: Implementation Planning
**Purpose:** Break down approved architecture into prioritized, dependency-ordered task lists.

**Agent Used:** `implementation-planner`

**Inputs:**
- `working/architecture.md` (from Step 2)

**Outputs:**
- `working/impl-plan.md` — Prioritized task breakdown ordered by dependencies

**Actions:**
- Generates task list ordered by dependencies
- Identifies blocked tasks and critical path
- Documents prioritization rationale

---

### Step 5: Feature Implementation
**Purpose:** Implement actual code based on the implementation plan.

**Agent Used:** `feature-implementer`

**Inputs:**
- `working/requirements.md` (from Step 1)
- `working/architecture.md` (from Step 2)
- `working/impl-plan.md` (from Step 4)

**Outputs:**
- Source code in `src/main/` and `src/test/`
- Commits with clear messages

**Actions:**
- Reads requirements, architecture, and implementation plan
- Asks clarification questions from you one at a time
- Implements features based on approved specifications and design
- Creates organized, well-structured code following project conventions

---

### Step 6: Code Review
**Purpose:** Conduct a structured peer code review of generated changes.

**Agent Used:** `code-reviewer`

**Inputs:**
- Current branch code changes vs. `claude-code-version` branch
- Uncommitted code in working stage

**Outputs:**
- Code review findings and recommendations

**Actions:**
- Evaluates correctness, security, error handling, code clarity, and DRY principles
- Identifies potential issues and improvements
- Presents findings in summarized format

---

### Step 7: Test Suite Generation
**Purpose:** Generate and run comprehensive unit and integration tests.

**Agent Used:** `test-suite-generator`

**Outputs:**
- Test files in `src/test/`
- `working/test-results.md` — Test execution report

**Actions:**
- Analyzes code scope and differentiates unit vs. integration tests
- Generates non-duplicate tests following JUnit 5 conventions
- Prompts you to run tests
- Creates test result report with coverage and pass/fail status

**Post-Step Actions:**
- Commit all changes **except** files in the `working/` directory

---

### Step 8: Working Directory Cleanup
**Command:** `file-cleanup KAN-3` (replace with your story number)

**Purpose:** Clean up working directory and archive documentation.

**Skill Used:** `file-cleanup`

**Actions:**
- Removes temporary files from `working/` directory
- Moves created documentation to archive folder
- Prepares repository for final submission

---

### Step 9: Pull Request Creation
**Purpose:** Push latest code and create a pull request with comprehensive summary and checklist.

**Agent Used:** `pr-creator`

**Outputs:**
- Pull request on GitHub with:
    - Descriptive title and body
    - Test evidence and limitations
    - Reviewer checklist
    - Co-authored commit attribution

**Actions:**
- Commits all changes (except working directory .md files)
- Creates PR to `claude-code-version` branch with:
    - Summary of changes
    - Test plan and evidence
    - Known limitations
    - Reviewer checklist

---

## Quick Reference

| Step | Command/Agent | Input | Output |
|------|---------------|-------|--------|
| 1 | `requirements-gathering` | Jira Issue | `working/requirements.md` |
| 2 | `architecture-designer` | `working/requirements.md` | `working/architecture.md` |
| 3 | `design-reviewer` | `working/architecture.md` | `working/design-review.md` |
| 4 | `implementation-planner` | `working/architecture.md` | `working/impl-plan.md` |
| 5 | `feature-implementer` | All previous files | Source code + commits |
| 6 | `code-reviewer` | Git diff | Review findings |
| 7 | `test-suite-generator` | Source code | Tests + `working/test-results.md` |
| 8 | `file-cleanup` | Story name | Cleaned working dir |
| 9 | `pr-creator` | Current branch | GitHub PR |

## Best Practices

- **Review Each Step:** Don't skip reviews; they catch issues early
- **Provide Feedback:** The agents iterate on feedback—be specific about concerns
- **Commit Frequently:** Each step's outputs are ready for review before proceeding
- **Test Locally:** Run the application (`./gradlew bootRun`) and test via Swagger UI before final PR
- **Follow Naming:** Use consistent issue keys (e.g., KAN-3) throughout the workflow
- **Document Decisions:** Note any significant decisions or blockers for future reference

## Notes

- Working directory (`working/`) files are temporary and removed after Step 8
- All commits should follow the project's commit message convention: "Work done by Sadique and Claude-code"
- The workflow assumes Jira and GitHub MCP servers are pre-configured and accessible
- Each step builds on previous outputs—maintain the sequence for best results

---

**Last Updated:** 2026-09-14