import org.junit.Test;
import static org.junit.Assert.*;

public class EvictionMapTest {

  @Test
  public void expirationTest() throws InterruptedException {
    EvictionMap<String, String> map = new EvictionMap<>(100);
    final String key = "key";
    final String value = "value";
    map.put(key, value);
    final String answer1 = map.get(key);
    Thread.sleep(100);
    final String answer2 = map.get(key);
    assertEquals(value, answer1);
    assertNull(answer2);
  }

  @Test
  public void multipleExpirationsTest() throws InterruptedException {
    EvictionMap<String, String> map = new EvictionMap<>(100);
    String key = "key";
    String value = "value";
    for(int i = 0; i < 10; i++) {
      map.put(key + i, value + i);
    }
    Thread.sleep(100);
    int expectedMapSize = 0;
    assertEquals(expectedMapSize, map.size());
  }

  @Test
  public void removeTest() {
    EvictionMap<String, String> map = new EvictionMap<>();
    String key = "key";
    String value = "value";
    map.put(key, value);
    String answer1 = map.get(key);
    map.remove(key);
    String answer2 = map.get(key);
    assertEquals(value, answer1);
    assertNull(answer2);
  }

  @Test
  public void replacementTest() throws InterruptedException {
    EvictionMap<String, String> map = new EvictionMap<>(100);
    String key = "key";
    String value1 = "value";
    String value2 = "value";
    map.put(key, value1);
    Thread.sleep(70);
    map.put(key, value2);
    String answer1 = map.get(key);
    Thread.sleep(70);
    String answer2 = map.get(key);
    assertEquals(value2, answer1);
    assertEquals(value2, answer2);
  }
}
