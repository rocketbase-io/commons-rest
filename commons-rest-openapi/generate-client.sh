#!/bin/bash

# TypeScript Client Generator Script
# Generates TypeScript client from running Spring Boot application

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
SERVER_URL="${1:-http://localhost:8080}"
OUTPUT_DIR="${2:-target/typescript-client}"
REACT_QUERY_VERSION="${3:-v5}"
BASE_URL="${4:-/api}"
GROUP_NAME="${5:-Api}"

echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   TypeScript Client Generator${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${YELLOW}Configuration:${NC}"
echo "  Server URL:         $SERVER_URL"
echo "  Output directory:   $OUTPUT_DIR"
echo "  React Query:        $REACT_QUERY_VERSION"
echo "  Base URL:           $BASE_URL"
echo "  Group name:         $GROUP_NAME"
echo ""

# Step 1: Download OpenAPI spec
echo -e "${YELLOW}[1/3] Downloading OpenAPI specification...${NC}"
OPENAPI_FILE="target/openapi.json"
mkdir -p target

if curl -sf "$SERVER_URL/v3/api-docs" > "$OPENAPI_FILE"; then
    echo -e "${GREEN}✓ OpenAPI spec downloaded successfully${NC}"
    FILE_SIZE=$(du -h "$OPENAPI_FILE" | cut -f1)
    echo "  File size: $FILE_SIZE"
else
    echo -e "${RED}✗ Failed to download OpenAPI spec from $SERVER_URL/v3/api-docs${NC}"
    echo ""
    echo -e "${YELLOW}Please ensure:${NC}"
    echo "  1. Your Spring Boot application is running"
    echo "  2. The server is accessible at $SERVER_URL"
    echo "  3. springdoc-openapi is configured"
    echo ""
    echo -e "${YELLOW}Usage:${NC}"
    echo "  $0 [server-url] [output-dir] [react-query-version] [base-url] [group-name]"
    echo ""
    echo -e "${YELLOW}Examples:${NC}"
    echo "  $0"
    echo "  $0 http://localhost:8080"
    echo "  $0 http://localhost:8080 client-output v5 /api MyApi"
    exit 1
fi
echo ""

# Step 2: Compile project (if needed)
echo -e "${YELLOW}[2/3] Compiling project...${NC}"
if mvn compile -q; then
    echo -e "${GREEN}✓ Project compiled successfully${NC}"
else
    echo -e "${RED}✗ Compilation failed${NC}"
    exit 1
fi
echo ""

# Step 3: Generate TypeScript client
echo -e "${YELLOW}[3/3] Generating TypeScript client...${NC}"
if mvn exec:java \
    -Dexec.mainClass="io.rocketbase.commons.openapi.StandaloneClientGenerator" \
    -Dexec.classpathScope=test \
    -Dexec.args="$OPENAPI_FILE $OUTPUT_DIR $REACT_QUERY_VERSION $BASE_URL $GROUP_NAME" \
    -q; then

    echo ""
    echo -e "${GREEN}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}   ✓ SUCCESS!${NC}"
    echo -e "${GREEN}════════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "${GREEN}TypeScript client generated successfully!${NC}"
    echo ""
    echo -e "${YELLOW}Generated files:${NC}"
    find "$OUTPUT_DIR" -type f | head -20 | sed 's/^/  /'
    if [ $(find "$OUTPUT_DIR" -type f | wc -l) -gt 20 ]; then
        echo "  ... and more"
    fi
    echo ""
    echo -e "${YELLOW}Next steps:${NC}"
    echo "  cd $OUTPUT_DIR"
    echo "  npm install"
    echo "  npm run build"
    echo ""
else
    echo -e "${RED}✗ Client generation failed${NC}"
    exit 1
fi
