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
        assertContains(content, "email: z.string().email()");            // @NotNull @Email
        assertContains(content, "role: z.enum([\"ADMIN\", \"USER\", \"GUEST\", \"MODERATOR\"])");   // @NotNull enum, rendered as Zod string-literal union
        assertContains(content, "addresses: z.array(");                  // @NotEmpty

        // Check optional fields (no required annotation)
        assertContains(content, "password: z.string().min(8).max(100).nullish()");  // @Size but not @NotNull
        assertContains(content, "age: z.number().int().min(18).max(120).nullish()");  // @Min @Max but not @NotNull
        assertContains(content, "phoneNumber: z.string().regex(/^\\+?[1-9]\\d{1,14}$/).nullish()");  // @Pattern but not @NotNull
        assertContains(content, "birthDate: z.coerce.date().nullish()");  // no validation
        assertContains(content, "tags: z.array(z.string()).min(1).max(10).nullish()");  // @Size but not @NotNull
        assertContains(content, "metadata: z.record(z.string(), z.any()).nullish()");  // no validation
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

        // Then: Nested types should use custom type reference
        String content = Files.readString(outputFile);

        // CreateUserCmd should reference AddressDto as custom type
        assertContains(content, "addresses: z.array(z.custom<Types.AddressDto>())");
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
        assertContains(content, "email: z.string().email()");
        assertContains(content, "role: z.enum([\"ADMIN\", \"USER\", \"GUEST\", \"MODERATOR\"])");

        // @NotBlank but not @NotNull - now optional!
        assertContains(content, "username: z.string().trim().min(1).min(3).max(50).nullish()");

        // @NotEmpty but not @NotNull - now optional!
        assertContains(content, "addresses: z.array(z.custom<Types.AddressDto>()).min(1).nullish()");
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
        assertContains(content, "email: z.string().email()");
        assertContains(content, "export type CreateUserCmd = z.infer<typeof CreateUserCmdSchema>");
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
