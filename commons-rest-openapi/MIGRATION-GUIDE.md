# Migration Guide: TypeScript Client Generation

## Overview

Version `LATEST-SNAPSHOT` introduces a major refactoring of the TypeScript client generation system. The old string-based type conversion has been replaced with automatic type generation using [typescript-generator](https://github.com/vojtechhabarta/typescript-generator).

## Breaking Changes

### 1. **OpenApiConverter Interface (Deprecated)**

The `OpenApiConverter` interface and its implementation `DefaultOpenApiConverter` have been deprecated and will be removed in a future major release.

**Old Code:**
```java
@Autowired
OpenApiConverter converter;

String tsType = converter.getReturnType("io.example.MyDto");
```

**New Code:**
```java
@Autowired
TypeScriptTypeConverter converter;

String tsType = converter.toTypeScript("io.example.MyDto");
```

### 2. **Type Generation Approach**

**Before:** Types were converted using complex string manipulation and regex patterns.

**Now:** Types are automatically generated from Java classes using typescript-generator.

## What Changed?

### New Classes

- **`TypeScriptTypeConverter`**: New interface for type conversion
- **`OpenApiTypeMapper`**: Default implementation using typescript-generator output
- **`TypeScriptModelGenerator`**: Generates TypeScript interfaces from Java classes
- **`TypeScriptGenerationResult`**: Holds generated types and mappings
- **`OpenApiSchemaAnalyzer`**: Extracts Java classes from OpenAPI extensions
- **`FileSystemClientWriter`**: Writes client files directly to filesystem
- **`StandaloneClientGenerator`**: Standalone tool for client generation
- **`ReflectionPropertyHelper`**: Utility for reflection-based property setting

### Enhanced Features

1. **Automatic Type Generation**
   - All DTOs are now generated as proper TypeScript interfaces
   - No more manual type mapping needed
   - Supports generics, enums, inheritance, and more

2. **Configurable TypeScript Generator**
   ```java
   TypeScriptGeneratorConfig config = new TypeScriptGeneratorConfig();
   config.setMapEnum(EnumMapping.asUnion);  // or asEnum
   config.setMapDate(DateMapping.asString);
   config.setNonConstEnums(false);
   ```

3. **Direct Filesystem Generation**
   ```bash
   # Generate client without ZIP files or bash scripts
   ./generate-client.sh
   ```

## Migration Steps

### For Spring Boot Applications

**No changes required!** The AutoConfiguration automatically provides the new implementation.

If you were injecting `OpenApiConverter`:

```java
// Old (deprecated)
@Autowired
OpenApiConverter converter;

// New
@Autowired
TypeScriptTypeConverter converter;
```

### For Custom Implementations

If you had custom type conversions:

**Before:**
```java
public class MyCustomConverter extends DefaultOpenApiConverter {
    @Override
    public String getReturnType(String genericReturnType) {
        if (genericReturnType.startsWith("com.mycompany.")) {
            return customConversion(genericReturnType);
        }
        return super.getReturnType(genericReturnType);
    }
}
```

**After:**
```java
// Configure typescript-generator instead
TypeScriptGeneratorConfig config = new TypeScriptGeneratorConfig();
Map<String, String> customMappings = new HashMap<>();
customMappings.put("com.mycompany.SpecialType", "MyCustomType");
config.setAdditionalTypeMappings(customMappings);

TypeScriptModelGenerator generator = new TypeScriptModelGenerator(config);
```

### For Standalone Usage

**New standalone tool:**
```bash
# Option 1: Use the script
./generate-client.sh http://localhost:8080

# Option 2: Use Maven
mvn exec:java \
  -Dexec.mainClass="io.rocketbase.commons.openapi.StandaloneClientGenerator" \
  -Dexec.classpathScope=test \
  -Dexec.args="openapi.json output-dir v5 /api MyApi"
```

## Configuration

### TypeScript Generator Settings

Create a configuration bean to customize typescript-generator behavior:

```java
@Bean
public TypeScriptGeneratorConfig typeScriptGeneratorConfig() {
    TypeScriptGeneratorConfig config = new TypeScriptGeneratorConfig();

    // Enum handling (asUnion is modern TypeScript idiom)
    config.setMapEnum(EnumMapping.asUnion);  // or asEnum

    // Date handling
    config.setMapDate(DateMapping.asString);

    // Non-const enums (only needed for asEnum)
    config.setNonConstEnums(false);

    // Additional type mappings
    Map<String, String> customMappings = new HashMap<>();
    customMappings.put("com.example.CustomType", "string");
    config.setAdditionalTypeMappings(customMappings);

    // Required annotations (only fields with these will be required, all others optional)
    config.setRequiredAnnotations(List.of(
        jakarta.validation.constraints.NotNull.class,
        jakarta.validation.constraints.NotBlank.class
    ));

    return config;
}
```

## Benefits of the New Approach

### 1. **No More String Manipulation**
- Eliminates complex regex patterns
- Reduces bugs from manual type conversion
- Easier to maintain

### 2. **Proper TypeScript Types**
- Generated interfaces match Java classes exactly
- Supports all TypeScript features (generics, unions, etc.)
- Better IDE support and type safety

### 3. **Modern TypeScript Idioms**
- Enums as union types by default
- Proper optional handling
- Clean, readable generated code

### 4. **Easier Customization**
- Configure via `TypeScriptGeneratorConfig`
- No need to override complex methods
- Simple type mapping system

## Examples

### Generated Types (Before vs After)

**Before (String Manipulation):**
```typescript
// Manual string replacement, prone to errors
export interface PageableResult {
    content: unknown[];  // Generic type lost
    totalElements: number;
}
```

**After (typescript-generator):**
```typescript
// Automatically generated with proper generics
export interface PageableResult<E> extends Iterable<E>, Serializable {
    totalElements: number;
    totalPages: number;
    page: number;
    pageSize: number;
    content: E[];
}

export interface Activity {
    type: "comment" | "dossier";  // Discriminated union
    id: string;
    dated: string;
    user: string;
}

export type TileType = "search-config" | "pinboard" | "briefing" | "showroom";
```

### Usage in Your Application

```java
@Service
public class MyService {

    @Autowired
    private OpenApiClientCreatorService clientCreator;

    public void generateClient() {
        Path outputDir = Paths.get("target/typescript-client");

        clientCreator.generateClientToFileSystem(
            ReactQueryVersion.v5,
            outputDir,
            openAPI,
            "/api",
            "MyApi"
        );
    }
}
```

## Troubleshooting

### Issue: Deprecated Warnings

If you see deprecation warnings, update your code to use the new interfaces:

```java
// Replace OpenApiConverter with TypeScriptTypeConverter
// Replace DefaultOpenApiConverter with OpenApiTypeMapper
```

### Issue: Custom Types Not Generated

Ensure your DTOs are referenced in the OpenAPI spec with the `x-generic-return-type` or `x-parameter-types` extensions.

### Issue: Wrong Type Mapping

Configure custom mappings via `TypeScriptGeneratorConfig`:

```java
config.setAdditionalTypeMappings(Map.of(
    "java.time.LocalDate", "string",
    "com.example.CustomId", "string"
));
```

## Timeline

- **Current Release (LATEST-SNAPSHOT)**: Deprecated old implementation, new implementation available
- **Next Major Release**: Old implementation will be removed

## Need Help?

- Check the [TypeScript Generator Documentation](https://github.com/vojtechhabarta/typescript-generator)
- Review the test cases in `FileSystemGenerationTest`
- Examine the generated client in `target/typescript-client/`

## Summary

The new TypeScript client generation system provides:
- ✅ Automatic type generation from Java classes
- ✅ No manual string manipulation
- ✅ Modern TypeScript idioms (union types, proper generics)
- ✅ Easy customization via configuration
- ✅ Direct filesystem generation (no ZIP files needed)
- ✅ Standalone tool for external projects

The migration is straightforward for most users, and the benefits far outweigh the effort!
