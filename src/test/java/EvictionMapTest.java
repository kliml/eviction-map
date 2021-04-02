import org.junit.Test;
import static org.junit.Assert.*;

public class EvictionMapTest {

  @Test
  public void test1() throws InterruptedException {
    EvictionMap<String, String> map = new EvictionMap(10000);
    String key = "key";
    String value = "value";
    map.put(key, value);
    String answer1 = map.get(key);
    Thread.sleep(10000);
    String answer2 = map.get(key);
    assertEquals(value, answer1);
    assertNull(answer2);
  }

  @Test
  public void test2() throws InterruptedException {
    EvictionMap<String, String> map = new EvictionMap(10000);
    String key = "key";
    String value = "value";
    map.put(key, value);
    String answer1 = map.get(key);
    map.remove(key);
    String answer2 = map.get(key);
    assertEquals(value, answer1);
    assertNull(answer2);
  }
}
