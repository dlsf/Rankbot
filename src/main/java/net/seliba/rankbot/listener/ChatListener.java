package net.seliba.rankbot.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.seliba.rankbot.files.LevelDao;

public class ChatListener extends ListenerAdapter {

  private static List<Long> disabledChannels = Arrays
      .asList(486921595047247872L, 486910521329844227L);
  private Map<User, Long> timestamps = new HashMap<>();

  private LevelDao levelDao;

  public ChatListener(LevelDao levelDao) {
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

    if (!timestamps.containsKey(event.getAuthor()) || timestamps.get(event.getAuthor()) > System
        .currentTimeMillis()) {
      levelDao.giveXP(event.getMember(), event.getGuild());
      timestamps.put(event.getAuthor(), System.currentTimeMillis());
    }
  }

}
