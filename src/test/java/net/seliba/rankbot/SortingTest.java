package net.seliba.rankbot;

import java.util.HashMap;
import java.util.Map;
import net.seliba.rankbot.files.TomlData;
import net.seliba.rankbot.files.VotesDao;
import org.junit.Assert;
import org.junit.Test;

public class SortingTest {

  @Test
  public void sortingTest() throws Exception {
    TomlData tomlData = new TomlData("sortingTest.toml");
    VotesDao votesDao = new VotesDao(null, tomlData, tomlData);

    Map<String, Long> testMap = new HashMap<>();
    testMap.put("test7", 70L);
    testMap.put("test9", 50L);
    testMap.put("test71", 49L);

    Map<String, Long> sortedTestMap = votesDao.sortMapByValue(testMap);
    Assert.assertEquals(sortedTestMap.size(), 3);
    Assert.assertEquals(sortedTestMap.values().toArray()[0], 49L);
    Assert.assertEquals(sortedTestMap.values().toArray()[1], 50L);
    Assert.assertEquals(sortedTestMap.values().toArray()[2], 70L);
    Assert.assertEquals(sortedTestMap.keySet().toArray()[0], "test71");
    Assert.assertEquals(sortedTestMap.keySet().toArray()[1], "test9");
    Assert.assertEquals(sortedTestMap.keySet().toArray()[2], "test7");
  }

}
