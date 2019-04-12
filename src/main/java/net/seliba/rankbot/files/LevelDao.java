package net.seliba.rankbot.files;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.seliba.rankbot.Rankbot;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LevelDao {

  private static final Logger LOGGER = LogManager.getLogger(Rankbot.class.getName());
  private TomlData tomlData;

  public LevelDao(TomlData tomlData) {
    this.tomlData = tomlData;
  }

  private static <K> Map<K, Long> sortMapByValue(Map<K, Long> mapToSort) {
    Map<K, Long> result = new LinkedHashMap<>();
    mapToSort.entrySet().stream()
        .sorted(Entry.comparingByValue())
        .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
    return result;
  }

  public void giveXP(Member member, Guild guild) {
    Long currentXp = getXp(member.getUser());
    tomlData.set(member.getUser().getIdLong() + ".xp", currentXp == null ? 5L : currentXp + 5L);

    if (currentXp == 0L) {
      guild
          .getController()
          .addSingleRoleToMember(member, member.getJDA().getRoleById(554734490359037996L))
          .queue();
    }

    if (tomlData.getLong(member.getUser().getIdLong() + ".level") == null) {
      tomlData.set(member.getUser().getIdLong() + ".level", 1L);
    }

    if (tomlData.getLong(member.getUser().getIdLong() + ".xp") == null) {
      tomlData.set(member.getUser().getIdLong() + ".xp", 0L);
    }

    if (getXp(member.getUser()) >= getXpToLevelup(getLevel(member.getUser()))) {
      levelUp(member, getXp(member.getUser()) - getXpToLevelup(getLevel(member.getUser())), guild);
    }
  }

  public Long getLevel(User user) {
    Long level = tomlData.getLong(user.getIdLong() + ".level");
    return level == null ? 1L : level;
  }

  public Long getXp(User user) {
    Long xp = tomlData.getLong(user.getIdLong() + ".xp");
    return xp == null ? 0L : xp;
  }

  private void levelUp(Member member, long xpLeft, Guild guild) {
    long currentLevel = tomlData.getLong(member.getUser().getIdLong() + ".level");
    currentLevel++;

    tomlData.set(member.getUser().getIdLong() + ".xp", xpLeft);
    tomlData.set(member.getUser().getIdLong() + ".level", currentLevel);
    tomlData.save();

    if (currentLevel == 35) {
      guild
          .getController()
          .addSingleRoleToMember(member, member.getJDA().getRoleById(554734662472433677L))
          .queue();
    } else if (currentLevel == 20) {
      guild
          .getController()
          .addSingleRoleToMember(member, member.getJDA().getRoleById(554734647893032962L))
          .queue();
      guild
          .getController()
          .removeSingleRoleFromMember(member, member.getJDA().getRoleById(554734631866335233L))
          .queue();
    } else if (currentLevel == 10) {
      guild
          .getController()
          .addSingleRoleToMember(member, member.getJDA().getRoleById(554734631866335233L))
          .queue();
      guild
          .getController()
          .removeSingleRoleFromMember(member, member.getJDA().getRoleById(554734613365391361L))
          .queue();
    } else if (currentLevel == 5) {
      guild
          .getController()
          .addSingleRoleToMember(member, member.getJDA().getRoleById(554734613365391361L))
          .queue();
      guild
          .getController()
          .removeSingleRoleFromMember(member, member.getJDA().getRoleById(554734490359037996L))
          .queue();
    }

    LOGGER.info(member.getEffectiveName() + " ist nun Level " + getLevel(member.getUser()));
  }

  public LinkedList<User> getSortedUsers(JDA jda) {
    HashMap<String, Long> rankedUsers = new HashMap<>();
    for (Map.Entry<String, Object> entry : tomlData.getEntrys().entrySet()) {
      if (entry.getKey().contains(".level")) {
        rankedUsers.put(entry.getKey().split("\\.")[0], (Long) entry.getValue());
      }
    }
    Map<String, Long> sortedUsers = sortMapByValue(rankedUsers);
    LinkedList topUsers = new LinkedList();
    sortedUsers
        .keySet()
        .forEach(
            consumerString -> {
              topUsers.add(jda.getUserById(consumerString));
            });

    return topUsers;
  }

  public long getXpToLevelup(long level) {
    return (long) (25 * Math.sqrt(level));
  }
}
