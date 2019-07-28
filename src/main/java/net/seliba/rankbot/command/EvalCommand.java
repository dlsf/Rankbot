package net.seliba.rankbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.regex.Pattern;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public class EvalCommand extends Command {

  private static final ScriptEngineManager ENGINE_MANAGER = new ScriptEngineManager();
  private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("^```(java)?|```$");

  private final ScriptEngine engine;

  public EvalCommand() {
    this.name = "eval";
    this.engine = ENGINE_MANAGER.getEngineByName("nashorn");
  }

  @Override
  protected void execute(CommandEvent event) {
    String script = CODE_BLOCK_PATTERN.matcher(event.getArgs()).replaceAll("");
    if (script.contains("getToken()")) {
      event.reply(":clown:");
    } else {
      Bindings bindings = event.getMember().hasPermission(Permission.ADMINISTRATOR)
          ? createBindings(event)
          : engine.createBindings();
      try {
        Object result = engine.eval(script, bindings);
        event.reply("Result:```java\n" + result + "```");
      } catch (ScriptException e) {
        event.replyFormatted("%s:```\n%s```", e.getMessage(), getStackTraceAsString(e.getCause()));
      }
    }
  }

  private Bindings createBindings(CommandEvent event) {
    Bindings bindings = engine.createBindings();
    bindings.put("event", event);
    bindings.put("channel", event.getChannel());
    bindings.put("jda", event.getJDA());
    bindings.put("author", event.getMember());
    bindings.put("guild", event.getGuild());
    return bindings;
  }

  private String getStackTraceAsString(Throwable throwable) {
    StringBuilder builder = new StringBuilder().append("Exception in thread ").append(Thread.currentThread().getName()).append(" - ")
        .append(throwable.getClass().getName()).append(": ").append(throwable.getMessage());
    StackTraceElement[] elements = throwable.getStackTrace();
    for (int i = 0; i < elements.length; i++) {
      if (builder.length() > Message.MAX_CONTENT_LENGTH - 200) {
        builder.append("\n\t...").append(elements.length - i).append(" more");
        return builder.toString();
      }
      builder.append("\n\tat: ").append(elements[i]);
    }

    causes:
    while ((throwable = throwable.getCause()) != null) {
      builder.append("\nCaused by: ").append(throwable.getClass().getName()).append(": ").append(throwable.getMessage());
      elements = throwable.getStackTrace();
      for (int i = 0; i < elements.length; i++) {
        if (builder.length() > Message.MAX_CONTENT_LENGTH - 200) {
          builder.append("\n\t...").append(elements.length - i).append(" more");
          break causes;
        }
        builder.append("\n\tat: ").append(elements[i]);
      }
    }
    return builder.toString();
  }
}
