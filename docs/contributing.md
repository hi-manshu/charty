# Contributing to Charty

Thank you for your interest in contributing to Charty! This document provides guidelines for contributing to the project.

---

## Code of Conduct

Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md).

---

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue on GitHub with:

1. A clear, descriptive title
2. Steps to reproduce the issue
3. Expected vs actual behavior
4. Screenshots (if applicable)
5. Your environment (OS, Kotlin version, etc.)

### Suggesting Features

We welcome feature suggestions! Please create an issue with:

1. A clear description of the feature
2. Use case and benefits
3. Proposed API (if applicable)
4. Examples or mockups

### Pull Requests

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Add tests for new functionality
5. Run tests and linting
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

---

## Development Setup

### Prerequisites

- JDK 11 or higher
- Android Studio or IntelliJ IDEA
- Gradle 8.x

### Building the Project

```bash
git clone https://github.com/hi-manshu/charty.git
cd charty
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Code Style

We follow Kotlin coding conventions. Run ktlint before committing:

```bash
./gradlew ktlintCheck
./gradlew ktlintFormat
```

---

## Project Structure

```
charty/
├── charty/                 # Main library module
│   └── src/
│       ├── commonMain/    # Multiplatform code
│       ├── androidMain/   # Android-specific
│       ├── iosMain/       # iOS-specific
│       ├── jsMain/        # JavaScript-specific
│       └── wasmJsMain/    # WebAssembly-specific
├── composeApp/            # Sample app
└── docs/                  # Documentation
```

---

## Adding a New Chart Type

1. Create chart composable in appropriate package
2. Create data model class
3. Create configuration class
4. Add tests
5. Update documentation
6. Add example to sample app

---

## Questions?

Feel free to ask questions by creating a GitHub Discussion or opening an issue.

---

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

