# GitHub Secrets Configuration for Publishing

This document provides step-by-step instructions for setting up GitHub Secrets required for automated publishing to Maven Central.

## Required Secrets

You need to configure 5 secrets in your GitHub repository:

### 1. MAVEN_CENTRAL_USERNAME

**What it is:** Your Maven Central Portal username (from user token)

**How to get it:**
1. Go to https://central.sonatype.com/
2. Sign in or create an account
3. Click on your profile (top right)
4. Go to **Account** → **Generate User Token**
5. Copy the **username** value

**Add to GitHub:**
- Go to your repo → **Settings** → **Secrets and variables** → **Actions**
- Click **New repository secret**
- Name: `MAVEN_CENTRAL_USERNAME`
- Value: Paste the username from step 5

---

### 2. MAVEN_CENTRAL_PASSWORD

**What it is:** Your Maven Central Portal password (from user token)

**How to get it:**
1. Same steps as MAVEN_CENTRAL_USERNAME
2. Copy the **password** value from the user token

**Add to GitHub:**
- Name: `MAVEN_CENTRAL_PASSWORD`
- Value: Paste the password from step 2

---

### 3. SIGNING_KEY_ID

**What it is:** The last 8 characters of your GPG key ID

**How to get it:**

```bash
# List your GPG keys
gpg --list-secret-keys --keyid-format=short
```

Output example:
```
/Users/username/.gnupg/pubring.kbx
----------------------------------
sec   rsa3072/ABCD1234 2025-01-01 [SC]
      FEDCBA9876543210FEDCBA9876543210ABCD1234
uid                   Your Name <your.email@example.com>
ssb   rsa3072/5678WXYZ 2025-01-01 [E]
```

**The key ID is: `ABCD1234`** (the part after `rsa3072/`)

**If you don't have a GPG key yet:**

```bash
# Generate a new GPG key
gpg --full-generate-key

# Choose:
# - Key type: RSA and RSA
# - Key size: 3072 or 4096
# - Expiration: 0 (never expires) or set as desired
# - Enter your name and email
# - Set a strong passphrase
```

**Add to GitHub:**
- Name: `SIGNING_KEY_ID`
- Value: Just the 8-character key ID (e.g., `ABCD1234`)

---

### 4. SIGNING_PASSWORD

**What it is:** The passphrase you set when creating your GPG key

**How to get it:**
- This is the password you entered when generating the GPG key
- If you forgot it, you'll need to generate a new key

**Add to GitHub:**
- Name: `SIGNING_PASSWORD`
- Value: Your GPG key passphrase

---

### 5. SIGNING_KEY

**What it is:** Your GPG private key in ASCII armor format

**How to get it:**

```bash
# Replace YOUR_KEY_ID with your actual key ID (e.g., ABCD1234)
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc

# View the content
cat private-key.asc
```

The output will look like:
```
-----BEGIN PGP PRIVATE KEY BLOCK-----

lQdGBGb...
...multiple lines of encoded data...
...
-----END PGP PRIVATE KEY BLOCK-----
```

**Important:** Copy the **ENTIRE** content including the BEGIN and END markers!

**Add to GitHub:**
- Name: `SIGNING_KEY`
- Value: Paste the entire content of private-key.asc

⚠️ **Security Note:** After adding the key to GitHub Secrets, delete the private-key.asc file from your computer:
```bash
rm private-key.asc
```

---

## Verification Checklist

Before running the workflow, verify all secrets are set:

- [ ] `MAVEN_CENTRAL_USERNAME` - Set and matches your Maven Central token username
- [ ] `MAVEN_CENTRAL_PASSWORD` - Set and matches your Maven Central token password  
- [ ] `SIGNING_KEY_ID` - Set to 8-character key ID
- [ ] `SIGNING_PASSWORD` - Set to your GPG passphrase
- [ ] `SIGNING_KEY` - Set to full private key (starts with -----BEGIN PGP PRIVATE KEY BLOCK-----)

## Testing

After setting up secrets, test the workflow:

1. Go to **Actions** tab in your GitHub repository
2. Select **Manual Publish to Maven Central**
3. Click **Run workflow**
4. Enter a test version like `0.0.1-test`
5. Click **Run workflow**

The workflow will validate all credentials and report any issues.

## Common Issues

### "Invalid username or password"
- Regenerate your user token on Maven Central Portal
- Make sure you're using the token credentials, not your account login

### "Invalid signature"
- Check that SIGNING_KEY includes the full key with BEGIN/END markers
- Verify SIGNING_KEY_ID matches your actual key ID
- Test locally: `echo "test" | gpg --clearsign`

### "Unrecognized secret"
- Secret names are case-sensitive
- Make sure there are no spaces in the secret names
- Secrets should be under **Repository secrets** not Environment secrets

## Publishing to Maven Central Portal

Maven Central has moved to a new publishing portal. The workflow uses:
- **New Portal:** https://central.sonatype.com/ (recommended)
- **API Endpoint:** https://central.sonatype.com/api/v1/publisher/

The Vanniktech plugin handles the API communication automatically.

## Resources

- [Maven Central Portal](https://central.sonatype.com/)
- [GPG/PGP Key Guide](https://central.sonatype.org/publish/requirements/gpg/)
- [Vanniktech Plugin Documentation](https://vanniktech.github.io/gradle-maven-publish-plugin/)
- [GitHub Secrets Documentation](https://docs.github.com/en/actions/security-guides/encrypted-secrets)

## Need Help?

If you're still having trouble:
1. Check the GitHub Actions workflow logs for specific error messages
2. Refer to PUBLISHING.md for detailed publishing instructions
3. Test publishing locally first using `./gradlew publishToMavenLocal`
4. Open an issue in the repository with the error details

