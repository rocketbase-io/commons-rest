package io.rocketbase.commons.openapi;

import io.rocketbase.commons.openapi.sample.dto.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ZodSchemaGenerator with complex types, records, and validation annotations.
 */
class ZodSchemaGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void testGenerateZodSchemaForSimpleCommand() throws Exception {
        // Given: OpenAPI with CreateUserCmd
        OpenAPI openAPI = createOpenAPIWithCommand(CreateUserCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        // When: Generate Zod schemas
        Path outputFile = tempDir.resolve("zod-schemas.ts");
        String result = generator.generateZodSchemas(outputFile);

        // Then: File should be created and contain expected content
        assertTrue(Files.exists(outputFile), "Zod schema file should be created");
        String content = Files.readString(outputFile);

        // Check imports
        assertContains(content, "import { z } from \"zod\"");
        assertContains(content, "import * as Types from \"./types\"");

        // Check schema export
        assertContains(content, "export const CreateUserCmdSchema = z.object({");

        // Check required fields (with @NotBlank, @NotNull, @NotEmpty)
        assertContains(content, "username: z.string().trim().min(1).min(3).max(50)");  // @NotBlank @Size
        assertContains(content, "email: z.email()");                     // @NotNull @Email — Zod v4 top-level
        assertContains(content, "role: z.enum([\"ADMIN\", \"USER\", \"GUEST\", \"MODERATOR\"])");   // @NotNull enum, rendered as Zod string-literal union
        // @NotEmpty List<AddressDto> — nested DTO gets its own schema, referenced (not z.custom)
        assertContains(content, "addresses: z.array(AddressDtoSchema).min(1)");
        // The nested AddressDto schema is emitted too, with its own field validation
        assertContains(content, "export const AddressDtoSchema = z.object({");
        assertContains(content, "zipCode: z.string().trim().min(1).max(10)");

        // Check optional fields (no required annotation)
        assertContains(content, "password: z.string().min(8).max(100).nullish()");  // @Size but not @NotNull
        assertContains(content, "age: z.number().int().min(18).max(120).nullish()");  // @Min @Max but not @NotNull
        assertContains(content, "phoneNumber: z.string().regex(/^\\+?[1-9]\\d{1,14}$/).nullish()");  // @Pattern but not @NotNull
        assertContains(content, "birthDate: z.coerce.date().nullish()");  // no validation
        assertContains(content, "tags: z.array(z.string()).min(1).max(10).nullish()");  // @Size but not @NotNull
        assertContains(content, "metadata: z.record(z.string(), z.string()).nullish()");  // Map<String,String> value resolved
        assertContains(content, "departmentId: z.number().int().positive().nullish()");  // @Positive but not @NotNull
        assertContains(content, "active: z.boolean().nullish()");  // no validation

        // Check type export
        assertContains(content, "export type CreateUserCmd = z.infer<typeof CreateUserCmdSchema>");
    }

    @Test
    void testGenerateZodSchemaForRecord() throws Exception {
        // Given: OpenAPI with UpdateProfileCmd (Java Record)
        OpenAPI openAPI = createOpenAPIWithCommand(UpdateProfileCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        // When: Generate Zod schemas
        Path outputFile = tempDir.resolve("zod-schemas-record.ts");
        String result = generator.generateZodSchemas(outputFile);

        // Then: Record fields should be processed correctly
        String content = Files.readString(outputFile);

        assertContains(content, "export const UpdateProfileCmdSchema = z.object({");
        assertContains(content, "displayName: z.string().trim().min(1).min(1).max(100)");  // @NotBlank @Size - required
        assertContains(content, "bio: z.string().max(500).nullish()");       // @Size but not @NotBlank - optional
        assertContains(content, "avatarUrl: z.string().nullish()");          // no validation - optional
    }

    @Test
    void testGenerateZodSchemaForNestedTypes() throws Exception {
        // Given: OpenAPI with CreateUserCmd which has nested AddressDto
        OpenAPI openAPI = createOpenAPIWithCommand(CreateUserCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        // When: Generate Zod schemas
        Path outputFile = tempDir.resolve("zod-schemas-nested.ts");
        String result = generator.generateZodSchemas(outputFile);

        // Then: nested DTOs get their own validated schema and are referenced (not z.custom)
        String content = Files.readString(outputFile);

        // CreateUserCmd references the generated AddressDtoSchema for its nested list
        assertContains(content, "addresses: z.array(AddressDtoSchema)");
        // ...and that schema is emitted with the nested field constraints intact
        assertContains(content, "export const AddressDtoSchema = z.object({");
        assertContains(content, "street: z.string().trim().min(1)");
        assertFalse(content.contains("z.custom<Types.AddressDto>()"),
                "nested DTO must be a validated schema reference, not an unvalidated z.custom");
    }

    @Test
    void testCustomRequiredAnnotations() throws Exception {
        // Given: Custom required annotations config
        OpenAPI openAPI = createOpenAPIWithCommand(CreateUserCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();

        // Only @NotNull is required, @NotBlank is optional
        config.setRequiredAnnotations(java.util.List.of(
            jakarta.validation.constraints.NotNull.class
        ));

        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        // When: Generate Zod schemas
        Path outputFile = tempDir.resolve("zod-schemas-custom.ts");
        String result = generator.generateZodSchemas(outputFile);

        // Then: Only @NotNull fields are required
        String content = Files.readString(outputFile);

        // @NotNull - required
        assertContains(content, "email: z.email()");
        assertContains(content, "role: z.enum([\"ADMIN\", \"USER\", \"GUEST\", \"MODERATOR\"])");

        // @NotBlank but not @NotNull - now optional!
        assertContains(content, "username: z.string().trim().min(1).min(3).max(50).nullish()");

        // @NotEmpty but not @NotNull - now optional! Nested DTO referenced by schema.
        assertContains(content, "addresses: z.array(AddressDtoSchema).min(1).nullish()");
    }

    @Test
    void testNoMutationCommands() throws Exception {
        // Given: OpenAPI without any mutation commands
        OpenAPI openAPI = new OpenAPI();
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        // When: Generate Zod schemas
        Path outputFile = tempDir.resolve("zod-schemas-empty.ts");
        String result = generator.generateZodSchemas(outputFile);

        // Then: File should contain only imports and no schemas
        String content = Files.readString(outputFile);

        assertContains(content, "import { z } from \"zod\"");
        assertContains(content, "import * as Types from \"./types\"");
        assertFalse(content.contains("export const"), "Should not contain any schema exports");
    }

    @Test
    void testAdvancedValidations() throws Exception {
        // Given: OpenAPI with AdvancedValidationCmd
        OpenAPI openAPI = createOpenAPIWithCommand(AdvancedValidationCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        // When: Generate Zod schemas
        Path outputFile = tempDir.resolve("zod-schemas-advanced.ts");
        String result = generator.generateZodSchemas(outputFile);

        // Then: Advanced validations should be present
        String content = Files.readString(outputFile);

        // @NotBlank - should have trim and min
        assertContains(content, "name: z.string().trim().min(1)");

        // @DecimalMin/Max with inclusive (BigDecimal is custom type)
        assertContains(content, "price: z.custom<Types.BigDecimal>().min(0.01).max(999.99)");

        // @DecimalMin/Max with exclusive (gt/lt)
        assertContains(content, "percentage: z.number().gt(0.0).lt(1.0)");

        // @Digits with integer only
        assertContains(content, "accountNumber: z.number().int().regex(/^-?\\\\d{1,10}$/)");

        // @Digits with fraction (BigDecimal is custom type)
        assertContains(content, "amount: z.custom<Types.BigDecimal>().regex(/^-?\\\\d{0,5}(\\\\.\\\\d{0,2})?$/)");

        // @Positive
        assertContains(content, "quantity: z.number().int().positive()");

        // @PositiveOrZero
        assertContains(content, "stockLevel: z.number().int().nonnegative()");

        // @Negative
        assertContains(content, "debt: z.number().int().negative()");

        // @NegativeOrZero
        assertContains(content, "balance: z.number().int().nonpositive()");
    }

    @Test
    void testRecursiveNestedTypeUsesZodLazy() throws Exception {
        // Given: a self-referential command (TreeNodeCmd.children: List<TreeNodeCmd>).
        OpenAPI openAPI = createOpenAPIWithCommand(TreeNodeCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        Path outputFile = tempDir.resolve("zod-schemas-recursive.ts");
        generator.generateZodSchemas(outputFile);

        // Then: the self-reference is wrapped in z.lazy so the const can reference itself.
        String content = Files.readString(outputFile);
        assertContains(content, "export const TreeNodeCmdSchema = z.object({");
        assertContains(content, "children: z.array(z.lazy(() => TreeNodeCmdSchema))");
        assertContains(content, "label: z.string().trim().min(1)");
    }

    @Test
    void testEnumWithJsonValueRendersWireFormatLiterals() throws Exception {
        // Given: a command whose Status enum exposes lowercase wire values via @JsonValue.
        // The Zod schema must validate the wire format Jackson produces, NOT the Java
        // constant name — otherwise the deserialised "active" string would fail Zod
        // validation that expects "ACTIVE".
        OpenAPI openAPI = createOpenAPIWithCommand(JsonValueStatusCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        Path outputFile = tempDir.resolve("zod-schemas-jsonvalue-enum.ts");
        generator.generateZodSchemas(outputFile);

        String content = Files.readString(outputFile);
        assertContains(content, "status: z.enum([\"active\", \"disabled\"])");
    }

    @Test
    void testCustomerTypeMappingsCollapseToZodPrimitives() throws Exception {
        // Given: a TypeScriptGenerationResult that maps a complex Java type (here we
        // reuse String → "string" as a stand-in for the real-world case of Tsid → "string"
        // applied by a TypeScriptGeneratorCustomizer in consumer projects). The Zod
        // generator must NOT emit z.custom<Types.String>() — Types.string does not exist
        // in the generated types.ts and the consumer's pnpm typecheck would break.
        OpenAPI openAPI = createOpenAPIWithCommand(UpdateProfileCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();

        Map<String, String> typeMapping = new HashMap<>();
        typeMapping.put("java.lang.String", "string");
        TypeScriptGenerationResult tsResult = new TypeScriptGenerationResult("", typeMapping);

        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config, tsResult);

        // When
        Path outputFile = tempDir.resolve("zod-schemas-primitive-mapping.ts");
        generator.generateZodSchemas(outputFile);

        // Then: String fields render as z.string(), not z.custom<Types.string>()
        String content = Files.readString(outputFile);
        assertContains(content, "displayName: z.string().trim().min(1).min(1).max(100)");
        assertFalse(content.contains("z.custom<Types.String>()"), "must not emit z.custom<Types.String>()");
        assertFalse(content.contains("z.custom<Types.string>()"), "must not emit z.custom<Types.string>()");
    }

    @Test
    void testGenerateZodSchemaFromOperationExtension() throws Exception {
        // Given: OpenAPI where the request body schema has NO x-java-type extension,
        // but the operation itself carries OpenApiCustomExtractor.REQUEST_BODY_TYPE_NAME.
        // This mirrors the real spec produced by OpenApiCustomExtractor for every
        // @MutationHook endpoint — springdoc-openapi does not populate x-java-type on
        // request body schemas, so the generator must rely on the operation extension.
        OpenAPI openAPI = createOpenAPIWithCommandViaOperationExtension(CreateUserCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        // When: Generate Zod schemas
        Path outputFile = tempDir.resolve("zod-schemas-from-operation-extension.ts");
        generator.generateZodSchemas(outputFile);

        // Then: The command class is discovered and its schema is emitted.
        String content = Files.readString(outputFile);
        assertContains(content, "export const CreateUserCmdSchema = z.object({");
        assertContains(content, "email: z.email()");
        assertContains(content, "export type CreateUserCmd = z.infer<typeof CreateUserCmdSchema>");
    }

    @Test
    void testPatternDecimalAndMapEdgeCases() throws Exception {
        OpenAPI openAPI = createOpenAPIWithCommand(EdgeCaseCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        Path outputFile = tempDir.resolve("zod-schemas-edge-cases.ts");
        generator.generateZodSchemas(outputFile);

        String content = Files.readString(outputFile);

        // #3: forward slashes in the @Pattern are escaped for the JS regex literal
        assertContains(content, "path: z.string().regex(/^\\/api\\/[a-z]+$/).nullish()");

        // #4: @DecimalMin value emitted verbatim, no Double precision loss
        assertContains(content, "preciseMin: z.custom<Types.BigDecimal>().min(0.10000000000000001)");
        assertFalse(content.contains("0.1)"), "decimal must not collapse to a lossy double");

        // #6: Map<String, AddressDto> value type resolves to the nested schema reference
        assertContains(content, "attributes: z.record(z.string(), AddressDtoSchema).nullish()");
        assertContains(content, "export const AddressDtoSchema = z.object({");
    }

    @Test
    void testFieldLevelZodSchemaAnyAndIgnore() throws Exception {
        // Given: a command with @ZodSchema(ANY) and @ZodSchema(IGNORE) fields.
        OpenAPI openAPI = createOpenAPIWithCommand(ZodAnnotatedCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        Path outputFile = tempDir.resolve("zod-schemas-field-annotations.ts");
        generator.generateZodSchemas(outputFile);

        String content = Files.readString(outputFile);
        // ANY field → z.any() regardless of its Java type
        assertContains(content, "rawPayload: z.any()");
        // IGNORE field → omitted from the schema entirely
        assertFalse(content.contains("internalNote"), "@ZodSchema(IGNORE) field must be dropped");
        // unrelated field is still validated normally
        assertContains(content, "name: z.string().trim().min(1)");
    }

    @Test
    void testTypeLevelZodSchemaIgnoreDegradesToAny() throws Exception {
        // Given: a command whose field type is annotated @ZodSchema(IGNORE) at type level.
        OpenAPI openAPI = createOpenAPIWithCommand(IgnoredValueHolderCmd.class);
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config);

        Path outputFile = tempDir.resolve("zod-schemas-type-ignore.ts");
        generator.generateZodSchemas(outputFile);

        String content = Files.readString(outputFile);
        // The ignored value object must NOT get its own schema...
        assertFalse(content.contains("export const IgnoredValueSchema"),
                "type-level @ZodSchema(IGNORE) must not produce a schema");
        // ...and the referencing field degrades to z.any() (not z.custom<Types.IgnoredValue>())
        assertContains(content, "payload: z.any()");
        assertFalse(content.contains("z.custom<Types.IgnoredValue>()"),
                "ignored type must not be referenced via z.custom");
    }

    @Test
    void testTypeLevelZodSchemaIncludeAddsRoot() throws Exception {
        // Given: an OpenAPI with NO mutation endpoints, but a TypeScriptGenerationResult whose
        // type-mapping contains a @ZodSchema(INCLUDE) class — i.e. the class appears in the API
        // surface (a response/param) but is not a mutation request body.
        OpenAPI openAPI = new OpenAPI();
        openAPI.setPaths(new io.swagger.v3.oas.models.Paths());
        TypeScriptModelGenerator.TypeScriptGeneratorConfig config = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();

        Map<String, String> typeMapping = new HashMap<>();
        typeMapping.put(StandaloneIncludeCmd.class.getName(), "StandaloneIncludeCmd");
        TypeScriptGenerationResult tsResult = new TypeScriptGenerationResult("", typeMapping);

        ZodSchemaGenerator generator = new ZodSchemaGenerator(openAPI, config, tsResult);

        Path outputFile = tempDir.resolve("zod-schemas-include-root.ts");
        generator.generateZodSchemas(outputFile);

        String content = Files.readString(outputFile);
        assertContains(content, "export const StandaloneIncludeCmdSchema = z.object({");
        assertContains(content, "sku: z.string().trim().min(1)");
        assertContains(content, "quantity: z.number().int().positive()");
    }

    // Helper methods

    private OpenAPI createOpenAPIWithCommand(Class<?> commandClass) {
        OpenAPI openAPI = new OpenAPI();
        openAPI.setPaths(new io.swagger.v3.oas.models.Paths());

        PathItem pathItem = new PathItem();
        Operation postOperation = new Operation();

        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType mediaType = new MediaType();

        Schema schema = new Schema();
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("x-java-type", commandClass.getName());
        schema.setExtensions(extensions);

        mediaType.setSchema(schema);
        content.addMediaType("application/json", mediaType);
        requestBody.setContent(content);
        postOperation.setRequestBody(requestBody);
        pathItem.setPost(postOperation);

        openAPI.getPaths().addPathItem("/test", pathItem);

        return openAPI;
    }

    /**
     * Builds an OpenAPI fixture that matches what {@link OpenApiCustomExtractor}
     * emits at runtime: the request body schema carries no {@code x-java-type},
     * and the operation has {@link OpenApiCustomExtractor#REQUEST_BODY_TYPE_NAME}
     * set to the fully-qualified command class name.
     */
    private OpenAPI createOpenAPIWithCommandViaOperationExtension(Class<?> commandClass) {
        OpenAPI openAPI = new OpenAPI();
        openAPI.setPaths(new io.swagger.v3.oas.models.Paths());

        PathItem pathItem = new PathItem();
        Operation postOperation = new Operation();

        Map<String, Object> operationExtensions = new HashMap<>();
        operationExtensions.put(OpenApiCustomExtractor.REQUEST_BODY_TYPE_NAME, commandClass.getName());
        postOperation.setExtensions(operationExtensions);

        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType mediaType = new MediaType();
        Schema schema = new Schema();
        mediaType.setSchema(schema);
        content.addMediaType("application/json", mediaType);
        requestBody.setContent(content);
        postOperation.setRequestBody(requestBody);
        pathItem.setPost(postOperation);

        openAPI.getPaths().addPathItem("/test-operation-extension", pathItem);

        return openAPI;
    }

    private void assertContains(String content, String expected) {
        assertTrue(
            content.contains(expected),
            "Expected content to contain: '" + expected + "'\nBut got:\n" + content
        );
    }
}
