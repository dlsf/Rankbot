package net.seliba.rankbot.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.seliba.rankbot.files.LevelDao;

public class ChatListener extends ListenerAdapter {

  private static List<User> sendMessageUsers = new ArrayList<>();
  private static List<Long> disabledChannels = Arrays
      .asList(486921595047247872L, 486910521329844227L);

  private ScheduledExecutorService scheduledExecutorService;
  private LevelDao levelDao;

  public ChatListener(LevelDao levelDao) {
    this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    this.levelDao = levelDao;
  }

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    if (disabledChannels.contains(event.getChannel().getIdLong())) {
      return;
    }

    if (event.getAuthor().isBot()) {
      return;
    }

    scheduledExecutorService.schedule(() ->
            sendMessageUsers.remove(event.getAuthor())
        , 1, TimeUnit.MINUTES);
    sendMessageUsers.add(event.getAuthor());
    levelDao.giveXP(event.getMember(), event.getGuild());
  }

}
