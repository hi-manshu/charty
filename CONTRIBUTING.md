# Contributing to Charty

First off, thank you for considering contributing to Charty! It's people like you that make Charty such a great tool.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Your First Code Contribution](#your-first-code-contribution)
  - [Pull Requests](#pull-requests)
- [Style Guidelines](#style-guidelines)
  - [Git Commit Messages](#git-commit-messages)
  - [Kotlin Style Guide](#kotlin-style-guide)
- [Development Setup](#development-setup)
- [Testing](#testing)

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to [hi-manshu on GitHub](https://github.com/hi-manshu).

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

* **Use a clear and descriptive title**
* **Describe the exact steps to reproduce the problem**
* **Provide specific examples**
* **Describe the behavior you observed and what behavior you expected**
* **Include screenshots if possible**
* **Include your environment details** (Kotlin version, platform, OS, etc.)

#### Template for Bug Reports

```markdown
**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. See error

**Expected behavior**
A clear and concise description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Environment:**
 - Platform: [e.g. Android, iOS, Web, Desktop]
 - Kotlin Version: [e.g. 2.0.0]
 - Charty Version: [e.g. 3.0.0]
 - OS: [e.g. Android 14, iOS 17, macOS 14]

**Additional context**
Add any other context about the problem here.
```

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

* **Use a clear and descriptive title**
* **Provide a detailed description of the suggested enhancement**
* **Provide examples of how the enhancement would be used**
* **Explain why this enhancement would be useful**
* **List any alternatives you've considered**

### Your First Code Contribution

Unsure where to begin? You can start by looking through these issues:

* `good-first-issue` - issues that should only require a few lines of code
* `help-wanted` - issues that are a bit more involved

### Pull Requests

1. Fork the repo and create your branch from `main`
2. If you've added code that should be tested, add tests
3. Ensure the test suite passes
4. Make sure your code follows the style guidelines
5. Write a clear commit message
6. Create the pull request

#### PR Guidelines

* **Fill in the PR template**
* **Include screenshots for UI changes**
* **Link relevant issues**
* **Keep PRs focused** - one feature/fix per PR
* **Update documentation** if you're changing functionality
* **Add tests** for new features

#### PR Template

```markdown
## Description
Brief description of what this PR does.

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## How Has This Been Tested?
Describe the tests you ran and on which platforms.

## Checklist
- [ ] My code follows the style guidelines
- [ ] I have performed a self-review
- [ ] I have commented my code where necessary
- [ ] I have updated the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix/feature works
- [ ] New and existing tests pass locally

## Screenshots (if applicable)
Add screenshots here.
```

## Style Guidelines

### Git Commit Messages

We follow [Conventional Commits](https://www.conventionalcommits.org/):

* `feat:` - A new feature
* `fix:` - A bug fix
* `docs:` - Documentation only changes
* `style:` - Code style changes (formatting, missing semicolons, etc.)
* `refactor:` - Code change that neither fixes a bug nor adds a feature
* `perf:` - Performance improvements
* `test:` - Adding or updating tests
* `chore:` - Changes to build process or auxiliary tools
* `ci:` - Changes to CI configuration files

Examples:
```
feat: add candlestick chart component
fix: resolve animation timing issue on iOS
docs: update README with new chart examples
```

### Kotlin Style Guide

* Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
* Run `./gradlew ktlintFormat` before committing
* Run `./gradlew detekt` to check for code issues
* Use meaningful variable and function names
* Write KDoc comments for public APIs
* Keep functions small and focused
* Prefer immutability

#### Code Example

```kotlin
/**
 * Represents a data point in a line chart.
 *
 * @property x The x-coordinate value
 * @property y The y-coordinate value
 * @property label Optional label for the data point
 */
data class PointData(
    val x: Float,
    val y: Float,
    val label: String? = null,
)

/**
 * Draws a line chart with the given data points.
 *
 * @param dataPoints List of points to display
 * @param modifier Modifier to apply to the chart
 * @param config Configuration for chart appearance
 */
@Composable
fun LineChart(
    dataPoints: List<PointData>,
    modifier: Modifier = Modifier,
    config: LineChartConfig = LineChartConfig(),
) {
    // Implementation
}
```

## Development Setup

### Prerequisites

* JDK 17 or higher
* Android Studio (for Android development)
* Xcode (for iOS development, macOS only)
* IntelliJ IDEA (recommended for multiplatform development)

### Setup Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/hi-manshu/charty.git
   cd charty
   ```

2. **Open in IDE**
   * Open the project in IntelliJ IDEA or Android Studio
   * Wait for Gradle sync to complete

3. **Run format and checks**
   ```bash
   ./gradlew ktlintFormat
   ./gradlew detekt
   ```

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run tests**
   ```bash
   ./gradlew test
   ```

### Project Structure

```
chartyv3/
├── charty/                 # Main library module
│   └── src/
│       ├── commonMain/     # Common Kotlin code
│       ├── androidMain/    # Android-specific code
│       ├── iosMain/        # iOS-specific code
│       ├── jsMain/         # JavaScript-specific code
│       └── wasmJsMain/     # WebAssembly-specific code
├── composeApp/            # Demo application
├── config/                # Configuration files
│   └── detekt/           # Detekt rules
└── .github/              # GitHub workflows
```

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :charty:test

# Run tests for specific platform
./gradlew :charty:testDebugUnitTest  # Android
```

### Writing Tests

* Write unit tests for all new features
* Use descriptive test names
* Follow the Arrange-Act-Assert pattern
* Mock external dependencies

Example:
```kotlin
@Test
fun `test line chart renders with correct number of points`() {
    // Arrange
    val dataPoints = listOf(
        PointData(0f, 10f),
        PointData(1f, 20f),
        PointData(2f, 15f)
    )
    
    // Act
    val result = calculateChartPoints(dataPoints)
    
    // Assert
    assertEquals(3, result.size)
}
```

## Code Review Process

1. At least one maintainer must approve the PR
2. All CI checks must pass
3. Code must follow style guidelines
4. Tests must be included for new features
5. Documentation must be updated if needed

## Recognition

Contributors will be:
* Listed in the CONTRIBUTORS.md file
* Mentioned in release notes
* Given credit in the documentation

## Questions?

Feel free to:
* Open an issue with the `question` label
* Start a discussion in GitHub Discussions
* Reach out to the maintainers

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

---

Thank you for contributing to Charty! 🎉

