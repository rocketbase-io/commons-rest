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
        assertContains(content, "import type * as Types from \"./types\"");

        // Check schema export
        assertContains(content, "export const CreateUserCmdSchema = z.object({");

        // Check required fields (with @NotBlank, @NotNull, @NotEmpty)
        assertContains(content, "username: z.string().trim().min(1).min(3).max(50)");  // @NotBlank @Size
        assertContains(content, "email: z.string().email()");            // @NotNull @Email
        assertContains(content, "role: z.nativeEnum(UserRole)");         // @NotNull enum
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
        assertContains(content, "role: z.nativeEnum(UserRole)");

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
        assertContains(content, "import type * as Types from \"./types\"");
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

    private void assertContains(String content, String expected) {
        assertTrue(
            content.contains(expected),
            "Expected content to contain: '" + expected + "'\nBut got:\n" + content
        );
    }
}
