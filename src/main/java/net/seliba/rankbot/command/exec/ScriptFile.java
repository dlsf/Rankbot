package net.seliba.rankbot.command.exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class ScriptFile {

  private static final String TEMPLATE;
  private static final Path SCRIPTS_DIRECTORY;

  static {
    String template = null;
    Path directory = null;
    try {
      template = String.join("\n", read(ScriptFile.class.getResourceAsStream("/ScriptTemplate.java")));
      directory = loadScriptsDirectory();
    } catch (IOException e) {
      e.printStackTrace();
    }
    TEMPLATE = template;
    SCRIPTS_DIRECTORY = directory;
  }
  
  private static List<String> read(InputStream inputStream) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      List<String> lines = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
      return Collections.unmodifiableList(lines);
    }
  }

  private static Path loadScriptsDirectory() throws IOException {
    Path directory = Paths.get("./temp/scripts");
    Files.createDirectories(directory);
    return directory;
  }

  private final String className;
  private final Path sourceFile;
  private final String script;
  
  private Path classFile;

  private ScriptFile(String script) throws IOException {
    this.script = script;
    this.className = generateNextName();
    this.sourceFile = SCRIPTS_DIRECTORY.resolve(className + ".java");
    sourceFile.toFile().deleteOnExit();
    Files.createFile(sourceFile);
    Files.write(sourceFile, fileContent().getBytes());
  }

  static ScriptFile create(String script) throws IOException {
    return new ScriptFile(script);
  }

  private static String generateNextName() throws IOException {
    return "Script_" + (scriptFileAmount() + 1);
  }

  private static long scriptFileAmount() throws IOException {
    return Files.list(SCRIPTS_DIRECTORY).count();
  }

  private String fileContent() {
    return String.format(TEMPLATE, className, script);
  }

  public void javac() {
    List<String> feedback = runProcess("javac", className + ".java");
    if (!feedback.isEmpty()) {
      throw new CompilationException(String.join("\n", feedback));
    } else {
      classFile = SCRIPTS_DIRECTORY.resolve(className + ".class");
      classFile.toFile().deleteOnExit();
    }
  }

  public List<String> java() {
    checkState(classFile != null && Files.exists(classFile), "Script is not compiled");
    return runProcess("java", className);
  }

  public void delete() {
    try {
      if (sourceFile != null) {
        Files.delete(sourceFile);
      }

      if (classFile != null) {
        Files.delete(classFile);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<String> runProcess(String... command) {
    try {
      Process process = new ProcessBuilder()
          .command(command)
          .directory(SCRIPTS_DIRECTORY.toFile())
          .start();
      boolean success = process.waitFor(30, TimeUnit.SECONDS);
      if (success) {
        return read(process.getInputStream());
      } else {
        process.destroyForcibly();
        throw new TimeoutException("Process timed out");
      }
    } catch (IOException | InterruptedException | TimeoutException e) {
      throw new ProcessException("Executing process was not successful", e);
    }
  }
}
