#!/bin/bash

# Compose Stability Check - Local Runner
# This script builds the project and analyzes Compose stability locally

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 Compose Stability Check - Local Runner${NC}"
echo "================================================"
echo ""

# Check if we're in the project root
if [ ! -f "settings.gradle.kts" ]; then
    echo -e "${RED}Error: Must be run from project root directory${NC}"
    exit 1
fi

# Parse arguments
CLEAN_BUILD=false
SKIP_BUILD=false
OUTPUT_FILE="stability_report.md"
OPEN_REPORT=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --clean)
            CLEAN_BUILD=true
            shift
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --output)
            OUTPUT_FILE="$2"
            shift 2
            ;;
        --open)
            OPEN_REPORT=true
            shift
            ;;
        --help)
            echo "Usage: ./scripts/check_stability_local.sh [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --clean         Clean build before generating reports"
            echo "  --skip-build    Skip build step (use existing reports)"
            echo "  --output FILE   Output file path (default: stability_report.md)"
            echo "  --open          Open the report after generation"
            echo "  --help          Show this help message"
            echo ""
            echo "Examples:"
            echo "  ./scripts/check_stability_local.sh"
            echo "  ./scripts/check_stability_local.sh --clean"
            echo "  ./scripts/check_stability_local.sh --skip-build --open"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Step 1: Build the project
if [ "$SKIP_BUILD" = false ]; then
    echo -e "${YELLOW}📦 Building project and generating Compose reports...${NC}"
    echo ""

    if [ "$CLEAN_BUILD" = true ]; then
        echo "Running clean build..."
        ./gradlew clean
    fi

    # Build both modules with Compose reports enabled
    echo "Compiling charty module..."
    ./gradlew :charty:compileDebugKotlinAndroid --rerun-tasks || {
        echo -e "${YELLOW}Warning: charty compilation had issues, continuing...${NC}"
    }

    echo "Compiling composeApp module..."
    ./gradlew :composeApp:compileDebugKotlinAndroid --rerun-tasks || {
        echo -e "${YELLOW}Warning: composeApp compilation had issues, continuing...${NC}"
    }

    echo -e "${GREEN}✓ Build complete${NC}"
    echo ""
else
    echo -e "${YELLOW}⏭️  Skipping build step${NC}"
    echo ""
fi

# Step 2: Check if reports exist
REPORTS_FOUND=false
if [ -d "charty/build/compose_reports" ] || [ -d "composeApp/build/compose_reports" ]; then
    REPORTS_FOUND=true
fi

if [ "$REPORTS_FOUND" = false ]; then
    echo -e "${RED}❌ No Compose reports found!${NC}"
    echo "Please ensure the build completed successfully."
    echo "Reports should be in:"
    echo "  - charty/build/compose_reports/"
    echo "  - composeApp/build/compose_reports/"
    exit 1
fi

# Step 3: Analyze the reports
echo -e "${YELLOW}🔬 Analyzing Compose stability reports...${NC}"
echo ""

python3 scripts/analyze_compose_stability.py . "$OUTPUT_FILE"
ANALYSIS_EXIT_CODE=$?

echo ""

# Step 4: Display results
if [ -f "$OUTPUT_FILE" ]; then
    echo -e "${GREEN}✓ Analysis complete!${NC}"
    echo ""
    echo "Report saved to: ${BLUE}$OUTPUT_FILE${NC}"
    echo ""

    # Show a preview
    echo -e "${BLUE}Preview:${NC}"
    echo "================================================"
    head -n 20 "$OUTPUT_FILE"

    LINE_COUNT=$(wc -l < "$OUTPUT_FILE")
    if [ "$LINE_COUNT" -gt 20 ]; then
        echo ""
        echo "... (see $OUTPUT_FILE for full report)"
    fi
    echo "================================================"
    echo ""

    # Open if requested
    if [ "$OPEN_REPORT" = true ]; then
        if command -v open &> /dev/null; then
            open "$OUTPUT_FILE"
            echo -e "${GREEN}✓ Opened report in default viewer${NC}"
        elif command -v xdg-open &> /dev/null; then
            xdg-open "$OUTPUT_FILE"
            echo -e "${GREEN}✓ Opened report in default viewer${NC}"
        else
            echo -e "${YELLOW}Could not auto-open report (no 'open' or 'xdg-open' command)${NC}"
        fi
    fi

    # Exit with appropriate code
    if [ $ANALYSIS_EXIT_CODE -eq 0 ]; then
        echo -e "${GREEN}🎉 All Compose components are stable!${NC}"
        exit 0
    else
        echo -e "${YELLOW}⚠️  Some stability issues found. Review the report above.${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ Failed to generate stability report${NC}"
    exit 1
fi

