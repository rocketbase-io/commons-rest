# Publishing OpenAPI TypeScript Clients

This guide explains how to generate and publish TypeScript clients from your OpenAPI-enabled Spring Boot application.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Local Development](#local-development)
- [Publishing to npm Registry](#publishing-to-npm-registry)
- [Publishing to GitHub Packages](#publishing-to-github-packages)
- [CI/CD Integration](#cicd-integration)
- [Configuration Reference](#configuration-reference)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Tools

- Java 17+ (for running the generator)
- Node.js 18+ and npm
- Maven 3.8+
- Your Spring Boot application with springdoc-openapi

### Add Dependency

Add the OpenAPI generator to your project's `pom.xml`:

```xml
<dependency>
    <groupId>io.rocketbase.commons</groupId>
    <artifactId>commons-rest-openapi</artifactId>
    <version>LATEST</version>
    <scope>test</scope>
</dependency>
```

Note: `test` scope is sufficient since you only need it for client generation, not at runtime.

## Local Development

### 1. Create Integration Test for Client Generation

The recommended approach is to use a Maven integration test that starts your application, downloads the client, and optionally publishes it.

Create `src/test/java/com/yourproject/GenerateClientIT.java`:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GenerateClientIT {

    @Value("http://localhost:${local.server.port}")
    private String baseUrl;

    @Test
    public void generateTypeScriptClient() throws Exception {
        // Download client ZIP
        URL clientUrl = new URL(baseUrl + "/generator/client/v5/my-api-client.zip");
        File destination = new File("target/typescript-client.zip");

        try (ReadableByteChannel rbc = Channels.newChannel(clientUrl.openStream());
             FileOutputStream fos = new FileOutputStream(destination)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }

        System.out.println("✅ Client generated: " + destination.getAbsolutePath());

        // Optional: Extract and build
        extractAndBuild(destination);
    }

    private void extractAndBuild(File zipFile) throws Exception {
        // Extract ZIP
        File outputDir = new File("target/typescript-client");
        // ... extraction logic ...

        // Build client
        ProcessBuilder pb = new ProcessBuilder("npm", "install");
        pb.directory(outputDir);
        pb.inheritIO().start().waitFor();

        pb = new ProcessBuilder("npm", "run", "build");
        pb.directory(outputDir);
        pb.inheritIO().start().waitFor();
    }
}
```

### 2. Run Integration Test

```bash
# Run only the integration test
mvn verify -Dit.test=GenerateClientIT

# Or run all integration tests
mvn verify
```

Maven will:
1. Build your application
2. Start it on a random port
3. Run the test (download client)
4. Stop the application

The generated client will be in `target/typescript-client.zip` or extracted in `target/typescript-client/`.

### Simple Alternative: Use Existing Test

If you just want to download the client manually, you can also directly call the endpoint:

```bash
# Make sure your app is running
mvn spring-boot:run &

# Wait for startup, then download
curl http://localhost:8080/generator/client/v5/my-client.zip -o client.zip
unzip client.zip -d typescript-client
```

**Endpoint Format:** `/generator/client/{version}/{filename}`

**Parameters:**
- `version` - React Query version: `v3`, `v4`, or `v5`
- `filename` - Name for the ZIP file (e.g., `my-api-client.zip`)

The generated client includes:
- Complete TypeScript types from your DTOs
- API client classes for all controllers
- React Query hooks (queries and mutations)
- Zod validation schemas
- All custom OpenAPI extensions and configurations

### 3. Configure Generated Client

Navigate to the generated client directory and customize the `package.json`:

```bash
cd typescript-client
```

Edit `package.json` to set your package name and version:

```json
{
  "name": "@yourorg/your-api-client",
  "version": "1.0.0",
  "description": "TypeScript client for Your API",
  "repository": {
    "type": "git",
    "url": "https://github.com/yourorg/your-repo.git"
  }
}
```

### 4. Test Locally

```bash
cd typescript-client

# Install dependencies
npm install

# Build the client
npm run build

# Test with dry-run
npm publish --dry-run
```

### 5. Stop Application (if running in background)

```bash
# If you started with spring-boot:run &
kill $SERVER_PID

# Or find and kill the process
pkill -f "spring-boot:run"
```

## Publishing to npm Registry

### Setup Authentication

Login to npm:

```bash
npm login
```

Or use an npm token in CI:

```bash
echo "//registry.npmjs.org/:_authToken=${NPM_TOKEN}" > .npmrc
```

### Publish

```bash
cd typescript-client

# Bump version (patch, minor, or major)
npm version patch

# Publish to public npm
npm publish --access public
```

## Publishing to GitHub Packages

### 1. Configure .npmrc

Create `.npmrc` in the generated client directory:

```properties
@yourorg:registry=https://npm.pkg.github.com
//npm.pkg.github.com/:_authToken=${GITHUB_TOKEN}
```

### 2. Update package.json

Add `publishConfig` to `package.json`:

```json
{
  "name": "@yourorg/your-api-client",
  "publishConfig": {
    "registry": "https://npm.pkg.github.com/@yourorg"
  }
}
```

### 3. Publish

```bash
cd typescript-client

# Authenticate (use Personal Access Token with write:packages scope)
export GITHUB_TOKEN=your_token_here

# Publish
npm publish
```

### 4. Consuming from GitHub Packages

Users need to configure their `.npmrc`:

```properties
@yourorg:registry=https://npm.pkg.github.com
//npm.pkg.github.com/:_authToken=${GITHUB_TOKEN}
```

Then install normally:

```bash
npm install @yourorg/your-api-client
```

## CI/CD Integration

### GitHub Actions Example

Create `.github/workflows/publish-client.yml`:

```yaml
name: Publish OpenAPI Client

on:
  push:
    branches: [main, master]
    tags:
      - 'v*'

jobs:
  publish-client:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'

      - name: Generate TypeScript client via integration test
        run: |
          mvn verify -Dit.test=GenerateClientIT
          # Client is now in target/typescript-client/

      - name: Configure client package
        run: |
          cd target/typescript-client
          # Update package.json with proper name and version
          npm version ${{ github.ref_name }} --no-git-tag-version || true

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          registry-url: 'https://npm.pkg.github.com'
          scope: '@yourorg'

      - name: Install dependencies and build
        run: |
          cd target/typescript-client
          npm install
          npm run build

      - name: Publish to GitHub Packages
        run: |
          cd target/typescript-client
          npm publish
        env:
          NODE_AUTH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### Maven Plugin Integration

Alternatively, use the Maven plugin to generate the client during build:

Add to your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.rocketbase.commons</groupId>
            <artifactId>commons-rest-openapi</artifactId>
            <version>LATEST</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate-typescript-client</goal>
                    </goals>
                    <configuration>
                        <openApiFile>${project.build.directory}/openapi.json</openApiFile>
                        <outputDirectory>${project.build.directory}/typescript-client</outputDirectory>
                        <reactQueryVersion>v5</reactQueryVersion>
                        <baseUrl>/api</baseUrl>
                        <groupName>MyApi</groupName>
                        <packageName>@yourorg/your-api-client</packageName>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Configuration Reference

### Generator REST Endpoint

The client generator exposes a REST endpoint in your running Spring Boot application:

```
GET /generator/client/{version}/{filename}
```

**Path Parameters:**
- **version**: React Query version - `v3`, `v4`, or `v5`
- **filename**: Name for the downloaded ZIP file (e.g., `my-api-client.zip`)

**Response:**
- Content-Type: `application/zip`
- Complete TypeScript client as ZIP archive

**Example URLs:**
```
http://localhost:8080/generator/client/v5/my-client.zip
http://localhost:8080/generator/client/v4/user-api-client.zip
```

### Spring Boot Configuration

Configure the generator behavior via `application.properties` or `application.yml`:

```properties
# API base URL (default: /api)
commons.openapi.generator.base-url=/api

# Client group name (default: ModuleApi)
commons.openapi.generator.group-name=MyApi

# NPM package name (default: openapi-module)
commons.openapi.generator.package-name=@myorg/my-api-client

# Folder structure
commons.openapi.generator.hook-folder=hooks
commons.openapi.generator.client-folder=clients
commons.openapi.generator.model-folder=model

# Default stale time in seconds (default: 2)
commons.openapi.generator.default-stale-time=5

# Enable/disable features
commons.openapi.generator.model-create=true
commons.openapi.generator.enable-file-system-generation=true
```

### Generated Package Structure

```
typescript-client/
├── package.json
├── tsconfig.json
├── rollup.config.js
├── src/
│   ├── index.ts              # Main entry point
│   ├── util.ts               # Utility functions
│   ├── model/
│   │   ├── types.ts          # Generated TypeScript types
│   │   ├── request.ts        # Request type definitions
│   │   ├── zod-schemas.ts    # Zod validation schemas
│   │   └── index.ts
│   ├── clients/
│   │   ├── [GroupName]Api.ts # API client class
│   │   └── index.ts
│   └── hooks/
│       ├── use[GroupName]Query.ts    # React Query hooks
│       ├── use[GroupName]Mutation.ts
│       └── index.ts
└── dist/                     # Built output (after npm run build)
```

### Package.json Scripts

The generated client includes these npm scripts:

```json
{
  "scripts": {
    "build": "rollup -c",
    "typecheck": "tsc --noEmit"
  }
}
```

## Versioning Strategies

### Semantic Versioning

Use semantic versioning based on API changes:

```bash
npm version patch  # Bug fixes (1.0.0 -> 1.0.1)
npm version minor  # New features (1.0.0 -> 1.1.0)
npm version major  # Breaking changes (1.0.0 -> 2.0.0)
```

### Build-based Versioning

Use build numbers or timestamps:

```bash
npm version 1.0.0-build.${{ github.run_number }}
# or
npm version 1.0.0-$(date +%Y%m%d%H%M%S)
```

### Git Tag-based Versioning

Extract version from git tags:

```bash
VERSION=$(git describe --tags --abbrev=0)
npm version ${VERSION#v} --no-git-tag-version
```

## Troubleshooting

### "Cannot find module '@rocketbase/commons-rest-client'"

The generated client depends on `@rocketbase/commons-rest-client`. Ensure it's installed:

```bash
npm install @rocketbase/commons-rest-client
```

This is automatically added to `package.json` dependencies during generation.

### TypeScript Compilation Errors

If you see TypeScript errors after generation:

1. Check that all dependencies are installed: `npm install`
2. Verify TypeScript version: `npm list typescript`
3. Run type-check: `npm run typecheck`

### Build Fails in CI

Common issues:

1. **Missing OpenAPI spec**: Ensure `openapi.json` is generated before client generation
2. **Java version mismatch**: Use Java 17+ in CI
3. **Node version**: Use Node.js 18+ for the build

### Publishing Permission Denied

For GitHub Packages:

1. Ensure workflow has `packages: write` permission
2. Use `secrets.GITHUB_TOKEN`, not a custom PAT
3. Package name must match repository org: `@yourorg/package-name`

For npm registry:

1. Verify npm token has publishing rights
2. For scoped packages, ensure you have access to the scope
3. Use `--access public` for public scoped packages

## Example Project Setup

Here's a complete example of integrating client generation into your project:

### 1. pom.xml

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>
        <dependency>
            <groupId>io.rocketbase.commons</groupId>
            <artifactId>commons-rest-openapi</artifactId>
            <version>LATEST</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-maven-plugin</artifactId>
                <version>1.4</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                        <configuration>
                            <outputFileName>openapi.json</outputFileName>
                            <outputDir>${project.build.directory}</outputDir>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2. .github/workflows/publish-client.yml

See [CI/CD Integration](#cicd-integration) section above.

### 3. Local Development Script

Create `scripts/generate-and-publish-client.sh`:

```bash
#!/bin/bash
set -e

echo "🧪 Running integration test to generate client..."
mvn verify -Dit.test=GenerateClientIT

echo "📦 Building client..."
cd target/typescript-client
npm install
npm run build

echo "✅ Client generated successfully!"
echo "   Location: ./target/typescript-client"
echo ""
echo "To publish:"
echo "   cd target/typescript-client"
echo "   npm version patch  # or minor/major"
echo "   npm publish"
```

Make executable and use:

```bash
chmod +x scripts/generate-and-publish-client.sh

# Generate client
./scripts/generate-and-publish-client.sh

# Or just use Maven directly
mvn verify -Dit.test=GenerateClientIT
```

## Additional Resources

- [React Query Documentation](https://tanstack.com/query/latest)
- [TypeScript Generator](https://github.com/vojtechhabarta/typescript-generator)
- [OpenAPI Specification](https://swagger.io/specification/)
- [GitHub Packages Documentation](https://docs.github.com/en/packages)
- [npm Publishing Guide](https://docs.npmjs.com/packages-and-modules/contributing-packages-to-the-registry)

## Support

For issues or questions:
- Check [GitHub Issues](https://github.com/rocketbase-io/commons-rest/issues)
- Review [commons-rest-client documentation](./typescript-runtime/README.md)
- Examine test cases in `commons-rest-openapi/src/test/java`
