package com.jw.github_issue_manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ModuleBoundaryTest {

    private static final Pattern PROJECT_DEPENDENCY = Pattern.compile("project\\(':(?<module>[^']+)'\\)");
    private static final Pattern SCOPED_PROJECT_DEPENDENCY = Pattern.compile(
        "(?m)^\\s*(?<scope>api|implementation)\\s+project\\(':(?<module>[^']+)'\\)"
    );
    private static final Set<String> APP_ALLOWED_API_IMPORTS = Set.of(
        "com.jw.github_issue_manager.comment.api.",
        "com.jw.github_issue_manager.connection.api.",
        "com.jw.github_issue_manager.issue.api.",
        "com.jw.github_issue_manager.repository.api."
    );
    private static final Map<String, String> MODULE_API_PREFIXES = Map.of(
        "comment", "com.jw.github_issue_manager.comment.api.",
        "connection", "com.jw.github_issue_manager.connection.api.",
        "issue", "com.jw.github_issue_manager.issue.api.",
        "repository", "com.jw.github_issue_manager.repository.api."
    );

    @Test
    void gradleModuleDependenciesFollowDocumentedDirection() throws IOException {
        Map<String, Set<String>> expectedDependencies = Map.of(
            "app", Set.of("comment", "connection", "issue", "platform", "repository", "shared-kernel"),
            "comment", Set.of("connection", "issue", "platform", "repository", "shared-kernel"),
            "connection", Set.of("platform", "shared-kernel"),
            "issue", Set.of("connection", "platform", "repository", "shared-kernel"),
            "platform", Set.of(),
            "repository", Set.of("connection", "platform", "shared-kernel"),
            "shared-kernel", Set.of("platform")
        );

        for (Map.Entry<String, Set<String>> entry : expectedDependencies.entrySet()) {
            assertEquals(
                entry.getValue(),
                projectDependencies(entry.getKey()),
                entry.getKey() + " module dependencies changed; update docs and boundary rules together."
            );
        }
    }

    @Test
    void gradleModuleApiDependenciesExposeOnlyPublicContracts() throws IOException {
        Map<String, Set<String>> expectedApiDependencies = Map.of(
            "app", Set.of(),
            "comment", Set.of("platform"),
            "connection", Set.of("platform"),
            "issue", Set.of("platform", "shared-kernel"),
            "platform", Set.of(),
            "repository", Set.of("platform", "shared-kernel"),
            "shared-kernel", Set.of()
        );

        for (Map.Entry<String, Set<String>> entry : expectedApiDependencies.entrySet()) {
            assertEquals(
                entry.getValue(),
                projectDependenciesByScope(entry.getKey(), "api"),
                entry.getKey() + " module api dependencies changed; keep transitive exposure intentional."
            );
        }
    }

    @Test
    void appModuleUsesOnlyPublicModuleApis() throws IOException {
        Path appSourceRoot = backendRoot().resolve("app/src/main/java");
        try (Stream<Path> sourceFiles = Files.walk(appSourceRoot)) {
            List<String> violations = sourceFiles
                .filter(path -> path.toString().endsWith(".java"))
                .flatMap(this::importsFrom)
                .filter(importLine -> importLine.contains(".internal.")
                    || importLine.startsWith("import com.jw.github_issue_manager.domain.")
                    || importLine.startsWith("import com.jw.github_issue_manager.repository.")
                    || importLine.startsWith("import com.jw.github_issue_manager.service."))
                .filter(importLine -> APP_ALLOWED_API_IMPORTS.stream().noneMatch(importLine::contains))
                .toList();

            assertTrue(
                violations.isEmpty(),
                "app module must depend on module api packages only: " + violations
            );
        }
    }

    @Test
    void publicApiPackagesOnlyUseTheirOwnInternalImplementation() throws IOException {
        List<String> violations = MODULE_API_PREFIXES.entrySet().stream()
            .flatMap(entry -> internalImportsFromPublicApi(entry.getKey(), entry.getValue()))
            .toList();

        assertTrue(
            violations.isEmpty(),
            "public api packages must not depend on other module internals: " + violations
        );
    }

    @Test
    void moduleDtosStayUnderApiPackages() throws IOException {
        Path backendRoot = backendRoot();
        try (Stream<Path> sourceFiles = Files.walk(backendRoot)) {
            List<String> violations = sourceFiles
                .filter(path -> path.toString().endsWith(".java"))
                .flatMap(this::importsAndPackagesFrom)
                .filter(line -> line.contains("com.jw.github_issue_manager.dto.auth")
                    || line.contains("com.jw.github_issue_manager.dto.comment")
                    || line.contains("com.jw.github_issue_manager.dto.issue")
                    || line.contains("com.jw.github_issue_manager.dto.repository")
                    || line.contains("com.jw.github_issue_manager.dto.sync"))
                .toList();

            assertTrue(
                violations.isEmpty(),
                "module DTOs must live under module api dto packages: " + violations
            );
        }
    }

    private Set<String> projectDependencies(String moduleName) throws IOException {
        String buildGradle = Files.readString(backendRoot().resolve(moduleName).resolve("build.gradle"));
        Matcher matcher = PROJECT_DEPENDENCY.matcher(buildGradle);
        Set<String> dependencies = new java.util.TreeSet<>();
        while (matcher.find()) {
            dependencies.add(matcher.group("module"));
        }
        return dependencies;
    }

    private Set<String> projectDependenciesByScope(String moduleName, String scope) throws IOException {
        String buildGradle = Files.readString(backendRoot().resolve(moduleName).resolve("build.gradle"));
        Matcher matcher = SCOPED_PROJECT_DEPENDENCY.matcher(buildGradle);
        Set<String> dependencies = new java.util.TreeSet<>();
        while (matcher.find()) {
            if (scope.equals(matcher.group("scope"))) {
                dependencies.add(matcher.group("module"));
            }
        }
        return dependencies;
    }

    private Stream<String> importsFrom(Path sourceFile) {
        try {
            return Files.readAllLines(sourceFile).stream()
                .map(String::trim)
                .filter(line -> line.startsWith("import "));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + sourceFile, exception);
        }
    }

    private Stream<String> importsAndPackagesFrom(Path sourceFile) {
        try {
            return Files.readAllLines(sourceFile).stream()
                .map(String::trim)
                .filter(line -> line.startsWith("import ") || line.startsWith("package "));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + sourceFile, exception);
        }
    }

    private Stream<String> internalImportsFromPublicApi(String moduleName, String apiPackagePrefix) {
        Path moduleSourceRoot = backendRoot().resolve(moduleName).resolve("src/main/java");
        String ownInternalPrefix = "import " + apiPackagePrefix.replace(".api.", ".internal.");
        try {
            return Files.walk(moduleSourceRoot)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> packageName(path).startsWith(apiPackagePrefix))
                .flatMap(this::importsFrom)
                .filter(importLine -> importLine.contains(".internal."))
                .filter(importLine -> !importLine.startsWith(ownInternalPrefix))
                .map(importLine -> moduleName + ": " + importLine);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to inspect api package for " + moduleName, exception);
        }
    }

    private String packageName(Path sourceFile) {
        try {
            return Files.readAllLines(sourceFile).stream()
                .map(String::trim)
                .filter(line -> line.startsWith("package "))
                .findFirst()
                .map(line -> line.substring("package ".length(), line.length() - 1))
                .orElse("");
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + sourceFile, exception);
        }
    }

    private Path backendRoot() {
        return Path.of("").toAbsolutePath().getParent();
    }
}
