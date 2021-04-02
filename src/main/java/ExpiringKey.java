import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Generic class that is meant to be used in DelayQueue,
 * representing key used in EvictionMap.
 * Implements Delayed.
 * @param <K> key type
 * @see EvictionMap
 */
public class ExpiringKey<K> implements Delayed {

  private final long startTime;
  private final long maxLifeTime;
  private final K key;

  public ExpiringKey(long lifeTime, K key) {
    this.startTime = System.currentTimeMillis();
    this.maxLifeTime = lifeTime;
    this.key = key;
  }

  public K getKey() {
    return key;
  }

  @Override
  public long getDelay(TimeUnit timeUnit) {
    return timeUnit.convert(getDelayMillis(), TimeUnit.MILLISECONDS);
  }

  private long getDelayMillis() {
    return (startTime + maxLifeTime) - System.currentTimeMillis();
  }

  @Override
  public int compareTo(Delayed delayed) {
    return Long.compare(this.getDelayMillis(), ((ExpiringKey<K>) delayed).getDelayMillis());
  }
}
