package net.seliba.rankbot;

import net.seliba.rankbot.files.LevelDao;
import net.seliba.rankbot.files.TomlData;
import org.junit.Assert;
import org.junit.Test;

public class LevelupTest {

  @Test
  public void levelupTest() {
    TomlData tomlData = new TomlData("levelupTest.toml");
    LevelDao levelDao = new LevelDao(tomlData);
    Assert.assertEquals(levelDao.getXpToLevelup(36), 150L);
    Assert.assertNotSame(levelDao.getXpToLevelup(1), 1L);

    tomlData.delete();
  }

}
