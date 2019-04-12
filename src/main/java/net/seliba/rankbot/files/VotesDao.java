package net.seliba.rankbot.files;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import net.seliba.rankbot.Rankbot;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class VotesDao {

  private static final Logger LOGGER = LogManager.getLogger(Rankbot.class.getName());
  private JDA jda;
  private TomlData votesData;
  private TomlData votedData;

  public VotesDao(JDA jda, TomlData votes, TomlData voted) {
    this.jda = jda;
    this.votesData = votes;
    this.votedData = voted;
  }

  private static Map<String, Long> transformMap(Map<String, Object> map) {
    Map<String, Long> newMap = new HashMap<>();
    map.forEach((key, value) -> newMap.put(key, (Long) value));
    return newMap;
  }

  private static <K> Map<K, Long> sortMapByValue(Map<K, Long> mapToSort) {
    Map<K, Long> result = new LinkedHashMap<>();
    mapToSort.entrySet().stream()
        .sorted(Entry.comparingByValue())
        .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
    return result;
  }

  public void vote(User whoVoted, User whoGotVoted) {
    Long currentVotes = votesData.getLong(String.valueOf(whoGotVoted.getIdLong()));
    votesData.set(
        String.valueOf(whoGotVoted.getIdLong()), currentVotes == null ? 1 : currentVotes + 1);
    votedData.set(String.valueOf(whoVoted.getIdLong()), 1L);
  }

  public Boolean hasVoted(User user) {
    return votedData.getLong(String.valueOf(user.getIdLong())) != null
        && votedData.getLong(String.valueOf(user.getIdLong())) == 1;
  }

  public List<User> getWinners() {
    Map<String, Long> sortedResults = sortMapByValue(transformMap(votesData.getEntrys()));
    List<String> possbileWinners = new ArrayList<>();
    Iterator<Map.Entry<String, Long>> iterator = sortedResults.entrySet().iterator();
    Map.Entry<String, Long> lastEntry = null;
    Map.Entry<String, Long> entry = iterator.next();
    while (entry != null) {
      if (possbileWinners.size() == 0) {
        possbileWinners.add(entry.getKey());
      }
      if (lastEntry != null
          && sortedResults.get(entry.getKey()) > sortedResults.get(lastEntry.getKey())) {
        possbileWinners.clear();
        possbileWinners.add(entry.getKey());
      } else if (lastEntry != null
          && sortedResults.get(entry.getKey()) == sortedResults.get(lastEntry.getKey())) {
        possbileWinners.add(entry.getKey());
      }
      lastEntry = entry;
      entry = iterator.hasNext() ? iterator.next() : null;
    }
    List<User> possibleWinnerUsers = new ArrayList<>();
    possbileWinners.forEach(
        possibleWinnerId -> {
          possibleWinnerUsers.add(jda.getUserById(possibleWinnerId));
        });
    return possibleWinnerUsers;
  }

  public long getVotes(User user) {
    return votesData.getLong(user.getId()) == null ? 0 : votesData.getLong(user.getId());
  }

  public void deleteVotes() {
    LOGGER.info("Votes wurden entfernt");

    votesData.delete();
    votesData.reload();

    votedData.delete();
    votedData.reload();
  }
}
