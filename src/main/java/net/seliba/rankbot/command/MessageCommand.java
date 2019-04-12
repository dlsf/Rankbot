package net.seliba.rankbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public class MessageCommand extends Command {

  public MessageCommand() {
    this.name = "message";
  }

  @Override
  protected void execute(CommandEvent event) {
    if (event.getAuthor().getIdLong() == 450632370354126858L) {
      event.reply(event.getArgs());
    }
  }
}
