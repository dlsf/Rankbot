package net.seliba.rankbot.command.exec;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.seliba.rankbot.command.EvalCommand;

import java.io.IOException;
import java.util.List;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public class ExecCommand extends Command {

  public ExecCommand() {
    this.name = "exec";
  }

  @Override
  protected void execute(CommandEvent event) {
    String script = EvalCommand.CODE_BLOCK_PATTERN.matcher(event.getArgs()).replaceAll("");
    event.async(() -> {
      try {
        ScriptFile file = ScriptFile.create(script);
        file.javac();
        List<String> output = file.java();
        event.reply("Output:```java\n" + String.join("\n", output) + "```");
        file.delete();
      } catch (IOException e) {
        e.printStackTrace();
        event.reply("An I/O exception occurred: " + e.getMessage());
      } catch (CompilationException e) {
        event.reply("Error while compiling script: ```\n" + e.getMessage() + "```");
      } catch (ProcessException e) {
        e.printStackTrace();
        event.reply("Script could not be executed successfully, cause: " + e.getCause());
      }
    });

  }
}
