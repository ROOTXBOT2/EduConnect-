# Claude Code Review Principles

For the code in $ARGUMENTS, perform a comprehensive code review applying the following criteria, and provide feedback.
Please create a review.md file in the starting location with your review.

## Accuracy
- Does the code accurately fulfill the specified requirements?
- Are behaviors and exception handling clearly defined and implemented?

## Reliability
- Does the code behave consistently in failure or unexpected scenarios?
- Are edge cases, such as null values and invalid inputs, handled gracefully?

## Readability & Maintainability
- Are names (variables, functions, classes) clear and meaningful?
- Is logic broken down into appropriately sized functions and modules?
- Is the code consistent and free from unnecessary complexity or duplication?
- Are comments and documentation provided where needed?

## Testing
- Are appropriate unit and integration tests implemented?
- Are external dependencies isolated using mocks or stubs as necessary?
- Do tests cover both typical and edge cases?

## Security
- Are authentication, authorization, error handling, and logging implemented securely?
- Is sensitive information protected throughout the codebase?

---

- For each area that needs improvement/refactoring, clearly point it out and provide a code example.
- Always explain the reason (basis) and show an example.
- If there are no deficiencies, state “No issues.”
- If something is uncertain or unverifiable, state “Uncertain.”
