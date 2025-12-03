#!/bin/bash

# Charty GitHub Secrets Preparation Script
# This script helps you gather all the information needed for GitHub secrets

set -e

echo "================================================"
echo "Charty GitHub Secrets Preparation"
echo "================================================"
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if secring.gpg exists
if [ ! -f "secring.gpg" ]; then
    echo -e "${RED}Error: secring.gpg file not found in current directory${NC}"
    echo "Please make sure your secring.gpg file is in the current directory"
    exit 1
fi

echo -e "${GREEN}✓ Found secring.gpg file${NC}"
echo ""

# Step 1: Get GPG Key ID
echo "================================================"
echo "Step 1: Getting GPG Key ID"
echo "================================================"
echo ""

echo "Listing GPG keys..."
gpg --keyring secring.gpg --list-secret-keys --keyid-format=short 2>/dev/null || \
    gpg --list-secret-keys --keyid-format=short

echo ""
echo -e "${YELLOW}Please copy the 8-character key ID from above (e.g., ABCD1234)${NC}"
echo -n "Enter your SIGNING_KEY_ID: "
read SIGNING_KEY_ID

if [ -z "$SIGNING_KEY_ID" ]; then
    echo -e "${RED}Error: SIGNING_KEY_ID cannot be empty${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✓ SIGNING_KEY_ID: $SIGNING_KEY_ID${NC}"
echo ""

# Step 2: Encode secring.gpg
echo "================================================"
echo "Step 2: Encoding secring.gpg"
echo "================================================"
echo ""

echo "Encoding secring.gpg to base64..."
GPG_KEY_CONTENTS=$(base64 -i secring.gpg)

echo -e "${GREEN}✓ secring.gpg encoded successfully${NC}"
echo ""

# Step 3: Get GPG Passphrase
echo "================================================"
echo "Step 3: GPG Key Passphrase"
echo "================================================"
echo ""

echo -n "Enter your GPG key passphrase (SIGNING_PASSWORD): "
read -s SIGNING_PASSWORD
echo ""

if [ -z "$SIGNING_PASSWORD" ]; then
    echo -e "${RED}Error: SIGNING_PASSWORD cannot be empty${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Passphrase received${NC}"
echo ""

# Step 4: Get Maven Central Credentials
echo "================================================"
echo "Step 4: Maven Central Credentials"
echo "================================================"
echo ""

echo -n "Enter your Maven Central username: "
read MAVEN_CENTRAL_USERNAME

echo -n "Enter your Maven Central password/token: "
read -s MAVEN_CENTRAL_PASSWORD
echo ""

if [ -z "$MAVEN_CENTRAL_USERNAME" ] || [ -z "$MAVEN_CENTRAL_PASSWORD" ]; then
    echo -e "${RED}Error: Maven Central credentials cannot be empty${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Maven Central credentials received${NC}"
echo ""

# Step 5: Verify GPG key can be used
echo "================================================"
echo "Step 5: Verifying GPG Key"
echo "================================================"
echo ""

echo "Testing GPG key export..."
gpg --batch --pinentry-mode loopback --passphrase "$SIGNING_PASSWORD" \
    --export-secret-keys --armor "$SIGNING_KEY_ID" > /tmp/test-key.asc 2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ GPG key verification successful${NC}"
    rm -f /tmp/test-key.asc
else
    echo -e "${RED}✗ GPG key verification failed${NC}"
    echo "Please check your key ID and passphrase"
    exit 1
fi

echo ""

# Step 6: Save to file
echo "================================================"
echo "Step 6: Saving Secrets"
echo "================================================"
echo ""

OUTPUT_FILE="github-secrets.txt"

cat > "$OUTPUT_FILE" <<EOF
================================================
GitHub Secrets for Charty
Generated: $(date)
================================================

⚠️  IMPORTANT: This file contains sensitive information!
    - Do NOT commit this file to git
    - Delete after adding secrets to GitHub
    - Keep in a secure location if you need to save it

================================================
Secrets to Add to GitHub
================================================

1. GPG_KEY_CONTENTS
-------------------
${GPG_KEY_CONTENTS}

2. SIGNING_KEY_ID
-----------------
${SIGNING_KEY_ID}

3. SIGNING_PASSWORD
-------------------
${SIGNING_PASSWORD}

4. MAVEN_CENTRAL_USERNAME
-------------------------
${MAVEN_CENTRAL_USERNAME}

5. MAVEN_CENTRAL_PASSWORD
-------------------------
${MAVEN_CENTRAL_PASSWORD}

================================================
How to Add These to GitHub
================================================

1. Go to: https://github.com/YOUR_USERNAME/charty/settings/secrets/actions
2. Click "New repository secret" for each secret
3. Copy the name and value from above
4. Click "Add secret"

================================================
EOF

echo -e "${GREEN}✓ Secrets saved to: $OUTPUT_FILE${NC}"
echo ""
echo -e "${YELLOW}⚠️  SECURITY WARNING:${NC}"
echo "   - This file contains your private keys and passwords"
echo "   - Add 'github-secrets.txt' to .gitignore"
echo "   - Delete this file after adding secrets to GitHub"
echo ""
echo "================================================"
echo "Next Steps:"
echo "================================================"
echo "1. Review the secrets in: $OUTPUT_FILE"
echo "2. Go to GitHub → Settings → Secrets → Actions"
echo "3. Add each secret from the file"
echo "4. Delete the file: rm $OUTPUT_FILE"
echo "5. Run the Manual Release workflow"
echo ""
echo -e "${GREEN}✓ Setup complete!${NC}"

