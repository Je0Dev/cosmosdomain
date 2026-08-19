# Constraints

These rules apply to every commit in this repository. Reviews must enforce them.

## Git workflow

- `main` is protected: **never commit directly to it**.
- **Every feature, test, or fix gets its own branch** created from an up-to-date
  `main`, then merged with a merge commit. Example:
  - `feat/<name>` for app features,
  - `test/<name>` for new or updated tests,
  - `fix/<name>`, `docs/<name>`, `chore/<name>` for the rest.
- Commit messages use Conventional Commits (`feat:`, `test:`, `fix:`, `docs:`,
  `chore:`). See [CONTRIBUTING.md](CONTRIBUTING.md) for the full workflow.
- Tests must pass on a branch before it is merged into `main`.

## Merge approval

- **When changes are pushed to GitHub and the user has not explicitly said
  "merge", never merge to `main` automatically.**
- Instead, push the work on its own branch and leave it for the user to review
  and approve the merge on GitHub.
- If the user asks the agent to merge on their behalf, that explicit request
  is approval enough - the agent may then merge and push `main`.

## Code size

- Every source file must stay within **100-120 lines maximum**.
- If a file grows past the limit, split it (e.g. separate `*Controller.java`
  for UI logic and `*Service.java` for business logic, or a dedicated
  `config/` class for properties).

## Functions / methods

- Keep methods small, focused, and single-purpose. **No monolithic methods**
  (a method must not load data, process it, and render a view at once).
- Prefer several small helpers over one large method.
- A method that becomes hard to read should be split into named helpers.

## Comments

- Comment *why*, not what - the code should already show what it does.
- Every public method, class, and field gets a short doc comment.
- Keep comments brief and to the point.
- **No `TODO:` markers or unfinished code may be committed.**

## Simplicity

- Keep things simple and direct. Avoid clever one-liners and unnecessary abstraction.
- No dead code: remove unused imports, fields, and methods before committing.

## Tests

- Tests are required for **important features**: authentication logic, rating
  calculations, persistence, and the TMDB API client. Not every method needs a
  test - small glue helpers and thin wrappers do not.
- Pure logic lives in testable modules (`service/`, `util/`), separate from
  the Thymeleaf controller layer.
- Run `mvn test` and all tests must pass before merging.

## Maintainability & scalability

- Code must be maintainable, scalable, and optimal where it matters:
  - no accidental `O(n^2)` scans of movie lists in hot paths,
  - database filtering pushed into queries instead of post-filtering in Java,
  - deterministic, side-effect-free helper functions where possible.
- Build cleanly with `./mvnw compile` and `./mvnw test` - zero errors.

## Style

- Java 21+ (target `<release>21</release>`).
- Standard Java naming conventions: `lowerCamelCase` for methods/variables,
  `UpperCamelCase` for classes, `UPPER_SNAKE` for constants.
- Spring Boot conventions: `@Component`, `@Service`, `@Repository`, `@Controller`.
- Order helpers top-down: private helpers above the public items that use them.