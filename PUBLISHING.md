# Publishing Guide for Charty

This guide explains how to publish Charty to Maven Central using the Vanniktech Gradle Maven Publish Plugin.

## Prerequisites

Before you can publish, you need:

1. **Maven Central Account**
   - Create an account at https://central.sonatype.com/
   - Generate a user token for authentication

2. **GPG Key**
   - Generate a GPG key pair for signing artifacts
   - Export the private key

## Setup

### 1. Maven Central Credentials

Obtain your credentials from Maven Central Portal:
- Go to https://central.sonatype.com/
- Navigate to your account settings
- Generate a user token
- Save the username and password

### 2. GPG Key Setup

Generate a GPG key if you don't have one:

```bash
# Generate a new GPG key
gpg --full-generate-key

# List your keys to get the key ID
gpg --list-secret-keys --keyid-format=short

# Export the private key (in ASCII armor format)
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc
```

The output will look like:
```
/Users/username/.gnupg/pubring.kbx
----------------------------------
sec   rsa3072/ABCD1234 2025-01-01 [SC]
      FEDCBA9876543210FEDCBA9876543210ABCD1234
uid                   Your Name <your.email@example.com>
ssb   rsa3072/5678WXYZ 2025-01-01 [E]
```

In this example, `ABCD1234` is your short key ID (use the last 8 characters).

### 3. GitHub Secrets Configuration

Add the following secrets to your GitHub repository:

**Settings → Secrets and variables → Actions → New repository secret**

| Secret Name | Description | How to Get |
|-------------|-------------|------------|
| `MAVEN_CENTRAL_USERNAME` | Maven Central username from user token | From Maven Central Portal |
| `MAVEN_CENTRAL_PASSWORD` | Maven Central password from user token | From Maven Central Portal |
| `SIGNING_KEY_ID` | Last 8 characters of your GPG key ID | From `gpg --list-secret-keys --keyid-format=short` |
| `SIGNING_PASSWORD` | Passphrase for your GPG key | The password you set when creating the key |
| `SIGNING_KEY` | Your GPG private key in ASCII armor format | Content of the private-key.asc file from export command above |

#### Detailed Steps for SIGNING_KEY:

```bash
# Export your private key
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc

# Copy the entire contents of private-key.asc
cat private-key.asc

# Paste the entire output (including -----BEGIN PGP PRIVATE KEY BLOCK----- and -----END PGP PRIVATE KEY BLOCK-----)
# as the value for SIGNING_KEY secret
```

The key should look like:
```
-----BEGIN PGP PRIVATE KEY BLOCK-----

lQdGBGb...
...
-----END PGP PRIVATE KEY BLOCK-----
```

## Publishing via GitHub Actions

### Manual Release Workflow

1. Go to your repository on GitHub
2. Click on **Actions** tab
3. Select **Manual Publish to Maven Central** workflow
4. Click **Run workflow**
5. Enter the version name (e.g., `2.1.0` or `2.1.0-beta01`)
6. Click **Run workflow**

The workflow will:
- ✅ Validate all credentials
- ✅ Build and sign all artifacts
- ✅ Publish to Maven Central Portal
- ✅ Create a Git tag
- ✅ Create a GitHub release

### Version Naming Convention

Follow semantic versioning:
- **Release**: `1.0.0`, `1.2.3`
- **Beta**: `1.0.0-beta01`, `2.0.0-beta02`
- **Alpha**: `1.0.0-alpha01`, `2.0.0-alpha03`
- **Release Candidate**: `1.0.0-rc01`

## Local Publishing (for testing)

### Publishing to Maven Local

Test your configuration locally before publishing:

```bash
# Set environment variables (or add to local.properties)
export ORG_GRADLE_PROJECT_mavenCentralUsername="your_username"
export ORG_GRADLE_PROJECT_mavenCentralPassword="your_password"
export ORG_GRADLE_PROJECT_signingInMemoryKeyId="your_key_id"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="your_key_password"
export ORG_GRADLE_PROJECT_signingInMemoryKey="$(cat private-key.asc)"

# Publish to Maven Local
./gradlew :charty:publishToMavenLocal
```

Check `~/.m2/repository/com/himanshoe/charty/` to verify the artifacts.

### Using local.properties (Recommended for local development)

Create or edit `local.properties` in the project root:

```properties
mavenCentralUsername=your_username
mavenCentralPassword=your_password
signingInMemoryKeyId=ABCD1234
signingInMemoryKeyPassword=your_gpg_password
signingInMemoryKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
```

Then run:
```bash
./gradlew :charty:publishToMavenLocal
```

⚠️ **Never commit local.properties to version control!**

## Publishing to Maven Central Manually

If you need to publish from your local machine:

```bash
# Set the version
export VERSION_NAME="2.1.0"

# Publish and release automatically
./gradlew :charty:publishAndReleaseToMavenCentral --no-configuration-cache
```

The `publishAndReleaseToMavenCentral` task will:
1. Build all variants (Android, iOS, JVM, JS, WASM)
2. Sign all artifacts
3. Upload to Maven Central Portal
4. Automatically release (make publicly available)

### Manual Review Before Release

If you want to review artifacts before releasing:

```bash
# Publish without automatic release
./gradlew :charty:publishAllPublicationsToMavenCentral --no-configuration-cache
```

Then visit https://central.sonatype.com/ to review and manually release.

## Troubleshooting

### "Unresolved reference" errors in IDE

After adding the Vanniktech plugin, sync Gradle:
```bash
./gradlew --refresh-dependencies
```

Or in IntelliJ IDEA: **File → Invalidate Caches → Invalidate and Restart**

### Publishing fails with authentication error

Verify your credentials:
```bash
# Test Maven Central credentials
curl -u "username:password" https://central.sonatype.com/api/v1/publisher/status
```

### Signing fails

Check your GPG key:
```bash
# List secret keys
gpg --list-secret-keys --keyid-format=short

# Test signing
echo "test" | gpg --clearsign
```

### "Version already exists" error

Each version can only be published once. Increment the version number.

### Artifacts not appearing on Maven Central

After successful publishing:
- Artifacts appear in Maven Central Portal immediately
- Synchronization to Maven Central Search can take 10-30 minutes
- Check status at: https://central.sonatype.com/artifact/com.himanshoe/charty

## Gradle Tasks Reference

| Task | Description |
|------|-------------|
| `publishToMavenLocal` | Publish to local Maven repository (~/.m2/repository) |
| `publishAllPublicationsToMavenCentral` | Publish to Maven Central Portal (manual release required) |
| `publishAndReleaseToMavenCentral` | Publish and automatically release to Maven Central |
| `closeAndReleaseRepository` | Close and release a staging repository (if using OSSRH) |

## Resources

- [Vanniktech Maven Publish Plugin Documentation](https://vanniktech.github.io/gradle-maven-publish-plugin/)
- [Maven Central Portal](https://central.sonatype.com/)
- [Sonatype Central Publishing Documentation](https://central.sonatype.org/publish/)
- [Semantic Versioning](https://semver.org/)

## Support

If you encounter issues:
1. Check the GitHub Actions logs for detailed error messages
2. Review the troubleshooting section above
3. Consult the Vanniktech plugin documentation
4. Open an issue in the repository

