#!/usr/bin/env python3
"""
Analyze Compose Compiler stability reports and generate a markdown table.
Based on: https://nordsecurity.com/blog/improving-nordvpn-android-compose-compiler
"""

import json
import os
import sys
from pathlib import Path
from typing import List, Dict, Any
import re


class StabilityIssue:
    def __init__(self, file_path: str, component_type: str, name: str, full_name: str, stability: str, reason: str = "", source_file: str = ""):
        self.file_path = file_path
        self.component_type = component_type
        self.name = name
        self.full_name = full_name  # Full qualified name like com.example.MyClass
        self.stability = stability
        self.reason = reason
        self.source_file = source_file  # The actual .kt file if we can determine it


def parse_compose_report(report_file: Path) -> List[StabilityIssue]:
    """Parse a Compose compiler report file and extract unstable/skippable components."""
    issues = []

    try:
        with open(report_file, 'r', encoding='utf-8') as f:
            content = f.read()

        lines = content.split('\n')
        report_type = "classes" if "classes" in report_file.name else "composables"
        module_name = report_file.stem.replace("_debug", "").replace("_release", "")

        i = 0
        while i < len(lines):
            line = lines[i]

            if report_type == "classes":
                # Parse class stability reports
                # Format: unstable class com.example.MyClass {
                match = re.match(r'^(stable|unstable|runtime)\s+(class|interface|object)\s+([\w.]+)\s*\{?', line)
                if match:
                    stability_type = match.group(1)
                    component_type = match.group(2)
                    full_class_name = match.group(3)

                    # Only report unstable or runtime (uncertain) classes
                    if stability_type in ['unstable', 'runtime']:
                        # Extract the simple class name
                        simple_name = full_class_name.split('.')[-1]

                        # Look ahead to find unstable properties or the reason
                        unstable_properties = []
                        runtime_reason = ""
                        j = i + 1

                        while j < len(lines) and j < i + 50:
                            prop_line = lines[j].strip()

                            # Stop at next class/function definition
                            if prop_line and not prop_line.startswith(('stable', 'unstable', 'runtime', '<')) and (
                                prop_line.startswith('class ') or
                                prop_line.startswith('fun ') or
                                prop_line.startswith('}')
                            ):
                                break

                            # Check for unstable properties
                            if prop_line.startswith('unstable '):
                                prop_match = re.search(r'unstable\s+val\s+(\w+):', prop_line)
                                if prop_match:
                                    unstable_properties.append(prop_match.group(1))

                            # Check for runtime stability reason
                            if '<runtime stability>' in prop_line:
                                runtime_reason = prop_line
                                break

                            j += 1

                        # Build reason string
                        reason_parts = []
                        if unstable_properties:
                            reason_parts.append(f"Unstable properties: {', '.join(unstable_properties[:3])}")
                            if len(unstable_properties) > 3:
                                reason_parts[-1] += f" (+{len(unstable_properties) - 3} more)"

                        if runtime_reason:
                            reason_parts.append(runtime_reason.strip())

                        reason = " | ".join(reason_parts) if reason_parts else "Check class definition"

                        # Determine source file from package name
                        source_file = full_class_name.replace('.', '/') + ".kt"

                        issues.append(StabilityIssue(
                            file_path=module_name,
                            component_type=component_type.capitalize(),
                            name=simple_name,
                            full_name=full_class_name,
                            stability="Runtime" if stability_type == "runtime" else "Unstable",
                            reason=reason,
                            source_file=source_file
                        ))

            else:  # composables report
                # Parse composable function reports
                # Format: restartable skippable scheme(...) fun com.example.MyComposable(
                if 'fun ' in line and ('restartable' in line or 'scheme' in line):
                    # Extract function full name
                    func_match = re.search(r'fun\s+([\w.]+)\s*\(', line)
                    if func_match:
                        full_func_name = func_match.group(1)
                        simple_func_name = full_func_name.split('.')[-1]

                        is_restartable = 'restartable' in line
                        is_skippable = 'skippable' in line

                        # Look ahead for unstable parameters
                        unstable_params = []
                        j = i + 1

                        while j < len(lines) and j < i + 30:
                            param_line = lines[j].strip()

                            # Stop at closing parenthesis or next function
                            if param_line.startswith(')') or (param_line.startswith('fun ') and 'restartable' in param_line):
                                break

                            # Look for unstable parameters
                            if 'unstable' in param_line:
                                # Format: unstable paramName: Type = ...
                                param_match = re.search(r'unstable\s+(\w+):\s*([^=\n]+)', param_line)
                                if param_match:
                                    param_name = param_match.group(1)
                                    param_type = param_match.group(2).strip()
                                    unstable_params.append(f"{param_name}: {param_type}")

                            j += 1

                        # Only report if there are unstable parameters or not skippable
                        if unstable_params or (is_restartable and not is_skippable):
                            reason_parts = []

                            if not is_skippable:
                                reason_parts.append("Not skippable - will recompose on every parent recomposition")

                            if unstable_params:
                                params_str = ", ".join(unstable_params[:2])
                                if len(unstable_params) > 2:
                                    params_str += f" (+{len(unstable_params) - 2} more)"
                                reason_parts.append(f"Unstable params: {params_str}")

                            reason = " | ".join(reason_parts)

                            # Determine source file
                            source_file = full_func_name.rsplit('.', 1)[0].replace('.', '/') + ".kt"

                            issues.append(StabilityIssue(
                                file_path=module_name,
                                component_type="Composable",
                                name=simple_func_name,
                                full_name=full_func_name,
                                stability="Unstable" if unstable_params else "Not Skippable",
                                reason=reason,
                                source_file=source_file
                            ))

            i += 1

    except Exception as e:
        print(f"Error parsing {report_file}: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()

    return issues


def find_all_compose_reports(build_dir: Path) -> List[Path]:
    """Find all Compose compiler report files in the build directory."""
    report_files = []

    # Look for compose_reports directories
    for root, dirs, files in os.walk(build_dir):
        if 'compose_reports' in root or 'compose_metrics' in root:
            for file in files:
                if file.endswith('.txt'):
                    report_files.append(Path(root) / file)

    return report_files


def analyze_reports(project_root: Path) -> List[StabilityIssue]:
    """Analyze all Compose reports in the project."""
    all_issues = []

    # Find build directories
    build_dirs = [
        project_root / 'charty' / 'build',
        project_root / 'composeApp' / 'build',
    ]

    for build_dir in build_dirs:
        if build_dir.exists():
            report_files = find_all_compose_reports(build_dir)
            for report_file in report_files:
                issues = parse_compose_report(report_file)
                all_issues.extend(issues)

    return all_issues


def generate_markdown_table(issues: List[StabilityIssue]) -> str:
    """Generate a markdown table from stability issues."""
    if not issues:
        return "✅ **All Compose components are stable!** No stability issues found.\n"

    markdown = "## 🔍 Compose Stability Report\n\n"
    markdown += f"Found **{len(issues)}** stability issues that may affect Compose recomposition performance.\n\n"

    # Group issues by type
    classes = [i for i in issues if i.component_type in ['Class', 'Interface', 'Object']]
    composables = [i for i in issues if i.component_type == 'Composable']

    if classes:
        markdown += "### 📦 Unstable Classes\n\n"
        markdown += "| Module | Class Name | Full Qualified Name | Source File | Issue |\n"
        markdown += "|--------|------------|---------------------|-------------|-------|\n"

        for issue in classes:
            # Escape pipe characters in reason
            reason = issue.reason.replace('|', '\\|').strip()
            if len(reason) > 150:
                reason = reason[:150] + "..."

            # Shorten source file path
            source = issue.source_file.replace('com/himanshoe/', '.../')

            markdown += f"| `{issue.file_path}` | **`{issue.name}`** | `{issue.full_name}` | `{source}` | {reason} |\n"

        markdown += "\n"

    if composables:
        markdown += "### 🎨 Unstable/Non-Skippable Composables\n\n"
        markdown += "| Module | Function Name | Full Qualified Name | Source File | Issue |\n"
        markdown += "|--------|---------------|---------------------|-------------|-------|\n"

        for issue in composables:
            # Escape pipe characters in reason
            reason = issue.reason.replace('|', '\\|').strip()
            if len(reason) > 150:
                reason = reason[:150] + "..."

            # Shorten source file path
            source = issue.source_file.replace('com/himanshoe/', '.../')

            markdown += f"| `{issue.file_path}` | **`{issue.name}`** | `{issue.full_name}` | `{source}` | {reason} |\n"

        markdown += "\n"

    markdown += "---\n\n"
    markdown += "### 📚 Learn More\n\n"
    markdown += "- [Improving Compose Compiler Stability](https://nordsecurity.com/blog/improving-nordvpn-android-compose-compiler)\n"
    markdown += "- [Compose Compiler Metrics](https://github.com/androidx/androidx/blob/androidx-main/compose/compiler/design/compiler-metrics.md)\n"
    markdown += "- [Jetpack Compose Performance](https://developer.android.com/jetpack/compose/performance)\n"
    markdown += "\n### 💡 How to Fix\n\n"

    if classes:
        markdown += "#### For Unstable Classes:\n\n"
        markdown += "```kotlin\n"
        markdown += "// ❌ Before - Unstable\n"
        markdown += "data class User(var name: String, val items: MutableList<String>)\n\n"
        markdown += "// ✅ After - Stable\n"
        markdown += "@Immutable\n"
        markdown += "data class User(val name: String, val items: List<String>)\n"
        markdown += "```\n\n"
        markdown += "**Quick fixes:**\n"
        markdown += "- Replace `var` with `val`\n"
        markdown += "- Use immutable collections (`List` instead of `MutableList`)\n"
        markdown += "- Add `@Immutable` or `@Stable` annotation\n"
        markdown += "- Ensure all property types are stable\n\n"

    if composables:
        markdown += "#### For Unstable Composables:\n\n"
        markdown += "```kotlin\n"
        markdown += "// ❌ Before - Unstable parameter\n"
        markdown += "@Composable\n"
        markdown += "fun UserProfile(user: UnstableUser) { ... }\n\n"
        markdown += "// ✅ After - Stable parameter\n"
        markdown += "@Composable\n"
        markdown += "fun UserProfile(user: StableUser) { ... }\n\n"
        markdown += "// Or use primitives\n"
        markdown += "@Composable\n"
        markdown += "fun UserProfile(userName: String, userAge: Int) { ... }\n"
        markdown += "```\n\n"
        markdown += "**Quick fixes:**\n"
        markdown += "- Make parameter types stable (see class fixes above)\n"
        markdown += "- Pass primitives instead of complex objects\n"
        markdown += "- Use `remember { }` for lambda parameters at call site\n"
        markdown += "- Consider state hoisting\n\n"

    return markdown


def main():
    """Main entry point."""
    if len(sys.argv) < 2:
        print("Usage: analyze_compose_stability.py <project_root> [output_file]")
        sys.exit(1)

    project_root = Path(sys.argv[1]).resolve()
    output_file = Path(sys.argv[2]) if len(sys.argv) > 2 else None

    if not project_root.exists():
        print(f"Error: Project root '{project_root}' does not exist")
        sys.exit(1)

    print(f"Analyzing Compose stability reports in {project_root}...")
    issues = analyze_reports(project_root)

    print(f"Found {len(issues)} stability issues")

    markdown = generate_markdown_table(issues)

    if output_file:
        output_file.write_text(markdown, encoding='utf-8')
        print(f"Report written to {output_file}")
    else:
        print("\n" + markdown)

    # Exit with error code if issues found (for CI)
    sys.exit(0 if len(issues) == 0 else 1)


if __name__ == "__main__":
    main()

