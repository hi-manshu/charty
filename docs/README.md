# Charty Documentation

This directory contains the MkDocs-based documentation for Charty.

## Setup

### Install MkDocs and dependencies

```bash
pip install -r requirements.txt
```

Or install individually:

```bash
pip install mkdocs mkdocs-material pymdown-extensions
```

## Building Documentation

### Serve locally

Preview the documentation with live reload:

```bash
mkdocs serve
```

Then visit http://127.0.0.1:8000 in your browser.

### Build static site

Generate the static HTML documentation:

```bash
mkdocs build
```

The built site will be in the `site/` directory.

## Deploying to GitHub Pages

Deploy to GitHub Pages:

```bash
mkdocs gh-deploy
```

This will build the documentation and push it to the `gh-pages` branch.

## Documentation Structure

```
docs/
├── index.md                    # Home page
├── getting-started/
│   ├── installation.md         # Installation guide
│   ├── quick-start.md          # Quick start guide
│   └── configuration.md        # Configuration guide
├── charts/
│   ├── overview.md             # Chart overview
│   ├── bar/                    # Bar chart variants
│   ├── line/                   # Line chart variants
│   ├── point/                  # Point/scatter charts
│   ├── pie/                    # Pie charts
│   ├── radar/                  # Radar charts
│   ├── candlestick/            # Candlestick charts
│   ├── combo/                  # Combo charts
│   ├── block/                  # Block charts
│   └── circular/               # Circular progress
├── examples/
│   ├── basic-usage.md          # Basic examples
│   ├── customization.md        # Customization examples
│   └── advanced.md             # Advanced examples
├── api-reference.md            # API reference
├── contributing.md             # Contributing guide
└── license.md                  # License information
```

## Writing Documentation

### Markdown Tips

- Use `#` for headers (# = h1, ## = h2, etc.)
- Use triple backticks for code blocks with language specification
- Use `!!! note`, `!!! tip`, `!!! warning` for admonitions
- Link to other pages with `[text](relative/path.md)`

### Code Examples

Always specify the language for syntax highlighting:

````markdown
```kotlin
fun example() {
    println("Hello")
}
```
````

### Adding New Pages

1. Create the `.md` file in the appropriate directory
2. Add it to the `nav` section in `mkdocs.yml`
3. Update any relevant index or overview pages

## Customization

### Theme

The theme is configured in `mkdocs.yml`. Current settings:

- Material theme with indigo color scheme
- Dark/light mode toggle
- Code copy buttons
- Search functionality
- Navigation tabs and sections

### Extensions

Enabled markdown extensions:

- `admonition` - Call-out boxes
- `pymdownx.details` - Collapsible sections
- `pymdownx.superfences` - Advanced code fencing
- `pymdownx.highlight` - Syntax highlighting
- `pymdownx.tabbed` - Tabbed content
- `tables` - Markdown tables
- `pymdownx.emoji` - Emoji support

## Troubleshooting

### MkDocs not found

Make sure Python and pip are installed:

```bash
python3 --version
pip3 --version
```

Then install MkDocs:

```bash
pip3 install mkdocs
```

### Material theme not found

Install the Material theme:

```bash
pip3 install mkdocs-material
```

### Port already in use

Use a different port:

```bash
mkdocs serve -a localhost:8001
```

## Contributing to Documentation

1. Follow the existing structure and style
2. Test locally with `mkdocs serve`
3. Check for broken links
4. Ensure code examples are correct and tested
5. Update the navigation in `mkdocs.yml` if adding new pages

## Resources

- [MkDocs Documentation](https://www.mkdocs.org/)
- [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)
- [PyMdown Extensions](https://facelessuser.github.io/pymdown-extensions/)

