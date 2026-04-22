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
    private static final Set<String> APP_ALLOWED_API_IMPORTS = Set.of(
        "com.jw.github_issue_manager.comment.api.",
        "com.jw.github_issue_manager.connection.api.",
        "com.jw.github_issue_manager.issue.api.",
        "com.jw.github_issue_manager.repository.api."
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
    void appModuleUsesOnlyPublicModuleApis() throws IOException {
        Path appSourceRoot = backendRoot().resolve("app/src/main/java");
        try (Stream<Path> sourceFiles = Files.walk(appSourceRoot)) {
            List<String> violations = sourceFiles
                .filter(path -> path.toString().endsWith(".java"))
                .flatMap(this::importsFrom)
                .filter(importLine -> importLine.startsWith("import com.jw.github_issue_manager.domain.")
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

    private Set<String> projectDependencies(String moduleName) throws IOException {
        String buildGradle = Files.readString(backendRoot().resolve(moduleName).resolve("build.gradle"));
        Matcher matcher = PROJECT_DEPENDENCY.matcher(buildGradle);
        Set<String> dependencies = new java.util.TreeSet<>();
        while (matcher.find()) {
            dependencies.add(matcher.group("module"));
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

    private Path backendRoot() {
        return Path.of("").toAbsolutePath().getParent();
    }
}
