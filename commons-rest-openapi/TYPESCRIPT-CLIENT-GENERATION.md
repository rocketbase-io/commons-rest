# TypeScript Client Generation

Dieses Modul generiert vollständige TypeScript Clients mit React Query Hooks aus deiner Spring Boot OpenAPI Spezifikation.

## ✨ Neues System (File System basiert)

Das neue System generiert den TypeScript Client **direkt ins Filesystem** ohne ZIP-Dateien oder bash-scripts.

### Verwendung mit Java 17

**Wichtig:** Stelle sicher, dass du Java 17 verwendest:

```bash
# Mit SDKMAN
sdk use java 17.0.6-amzn

# Oder setze JAVA_HOME manuell
export JAVA_HOME=~/.sdkman/candidates/java/17.0.6-amzn
export PATH=$JAVA_HOME/bin:$PATH
```

### Automatische Generierung während Maven Build

Die Client-Generierung erfolgt **automatisch** während der `prepare-package` Phase:

```bash
mvn clean install
```

**Output:** `target/typescript-client/`

### Programmatische Verwendung (in deinem Code)

```java
@Autowired
private OpenApiClientCreatorService openApiClientCreatorService;

public void generateClient() {
    // OpenAPI Spec laden
    OpenAPI openAPI = ...; // von springdoc oder aus File

    // Client generieren
    Path outputDir = Paths.get("target/typescript-client");
    openApiClientCreatorService.generateClientToFileSystem(
        ReactQueryVersion.v5,     // oder v4, v3
        outputDir,
        openAPI,
        "/api",                   // Base URL
        "MyApi"                   // Group Name
    );
}
```

### Maven Mojo (Custom Build)

Du kannst die Generierung auch manuell triggern:

```bash
mvn io.rocketbase.commons:commons-rest-openapi:generate \
    -Dopenapi.file=target/openapi.json \
    -Dtypescript.outputDirectory=target/typescript-client \
    -DreactQuery.version=V5 \
    -Dapi.baseUrl=/api \
    -Dapi.groupName=MyApi
```

### Konfiguration (application.yml)

```yaml
commons:
  openapi:
    generator:
      base-url: /api
      package-name: my-api-client
      group-name: MyApi
      enable-file-system-generation: true
      output-directory: typescript-client
      class-patterns:
        - "io.rocketbase.commons.**.dto.**"
        - "com.example.**.model.**"
      custom-type-mappings:
        "com.example.CustomType": "string"
        "com.example.ComplexType": "CustomInterface"
```

## 📦 Generierte Struktur

```
target/typescript-client/
├── package.json              # npm package mit react-query dependencies
├── src/
│   ├── index.ts             # Main export mit useApi() hook
│   ├── util.ts              # Pagination utilities
│   ├── model/
│   │   ├── index.ts         # Model exports
│   │   └── request.ts       # PageableRequest interface
│   ├── clients/             # Axios API clients
│   │   ├── activity-api.ts
│   │   ├── permission-api.ts
│   │   └── index.ts
│   └── hooks/               # React Query hooks
│       ├── activity-hooks.ts
│       ├── permission-hooks.ts
│       └── index.ts
```

## 🔄 React Query Versionen

Das System unterstützt alle React Query Versionen:

- **v3**: `react-query@^3.x`
- **v4**: `@tanstack/react-query@^4.x`
- **v5**: `@tanstack/react-query@^5.x` (empfohlen)

## 🚀 Integration im Frontend

Nach der Generierung kannst du den Client direkt verwenden:

```typescript
// 1. Client kopieren
cp -r target/typescript-client ../frontend/src/api

// 2. Dependencies installieren
cd ../frontend
npm install

// 3. In deiner App verwenden
import { useApi } from './api';
import { useActivityHooks } from './api/hooks/activity-hooks';

function MyComponent() {
  const api = useApi();
  const { useGetActivities } = useActivityHooks(api);

  const { data, isLoading } = useGetActivities();

  // ...
}
```

## 🔧 Architektur

### Komponenten

1. **TypeScriptModelGenerator** - Konvertiert Java DTOs zu TypeScript Interfaces (verwendet `typescript-generator` API)
2. **FileSystemClientWriter** - Schreibt Dateien direkt ins Filesystem
3. **OpenApiClientCreatorService** - Orchestriert die Generierung
4. **Pebble Templates** - Generiert Clients und Hooks mit maximaler Flexibilität

### Workflow

```
OpenAPI Spec → Parser → Controllers → Templates → TypeScript Files
                                    ↓
                              typescript-generator
                                    ↓
                              Model Interfaces
```

## 📋 Vergleich: Alt vs Neu

| Feature | Alt (ZIP + bash) | Neu (File System) |
|---------|------------------|-------------------|
| Output | ZIP Download | `target/typescript-client/` |
| Script benötigt | Ja (`build.sh`) | Nein |
| Maven Integration | Manuell | Automatisch |
| Type Generierung | Maven Plugin | Programmatisch |
| Flexibilität | Begrenzt | Hoch |

## ⚙️ typescript-generator Integration

Das neue System nutzt `typescript-generator` **programmatisch** statt über das Maven Plugin:

**Vorher:**
```xml
<!-- Auskommentiert in pom.xml -->
<plugin>
    <groupId>cz.habarta.typescript-generator</groupId>
    <artifactId>typescript-generator-maven-plugin</artifactId>
    ...
</plugin>
```

**Jetzt:**
```java
TypeScriptGenerator generator = new TypeScriptGenerator(settings);
String typescript = generator.generateTypeScript(Input.from(classes));
```

## 🔄 Migration von Alt zu Neu

1. **Entferne bash-script**
   ```bash
   rm client-generation/build.sh
   ```

2. **Verwende Maven Build**
   ```bash
   mvn clean install
   ```

3. **Client aus target/ kopieren**
   ```bash
   cp -r target/typescript-client/* ../frontend/src/api/
   ```

4. **Fertig!** Keine manuellen Schritte mehr nötig.

## 🧪 Tests

Führe die Tests aus um die Funktionalität zu verifizieren:

```bash
# FileSystem Generierung testen
mvn test -Dtest=FileSystemGenerationTest

# Alle Versionen testen
mvn test -Dtest=FileSystemGenerationTest#testGenerateForDifferentReactQueryVersions

# FileSystemClientWriter testen
mvn test -Dtest=FileSystemGenerationTest#testFileSystemClientWriter
```

## 🎯 Vorteile des neuen Systems

✅ **Kein bash-script mehr** - Alles in Java
✅ **Maven Integration** - Automatische Generierung während Build
✅ **Programmatisch** - typescript-generator API direkt genutzt
✅ **Wartbar** - Klare Java-Architektur
✅ **Flexibel** - Templates für Custom-Logik
✅ **Type-Safe** - Automatische DTO→TypeScript Konvertierung
✅ **Backwards Compatible** - ZIP-Download funktioniert weiterhin

## 📝 Hinweise

- **Java Version:** Verwende Java 17 (nicht Java 25)
- **Lombok:** Muss korrekt konfiguriert sein (Annotation Processing)
- **OpenAPI Spec:** Muss vorhanden sein (via springdoc)
- **React Query:** Wähle die passende Version für dein Frontend

## 🐛 Troubleshooting

### Lombok Fehler während Kompilierung

**Problem:** `cannot find symbol: variable log`

**Lösung:** Java 17 verwenden:
```bash
sdk use java 17.0.6-amzn
mvn clean compile
```

### Client wird nicht generiert

**Problem:** Keine Dateien in `target/typescript-client/`

**Lösung:** Prüfe, ob OpenAPI Spec vorhanden ist:
```bash
# OpenAPI Spec sollte generiert werden
mvn clean package
ls -la target/openapi.json
```

### Type Mappings fehlen

**Problem:** Bestimmte Java-Typen werden nicht korrekt konvertiert

**Lösung:** Füge Custom-Mappings hinzu:
```yaml
commons:
  openapi:
    generator:
      custom-type-mappings:
        "your.package.CustomType": "string"
```

---

**Erstellt mit:** Spring Boot 3.5.9 + typescript-generator 3.2.1263 + React Query v3/v4/v5 Support 🚀
