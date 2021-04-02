import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Generic structure that acts as key-value map with following time-based eviction policy:
 * expire entries after the specified duration has passed since the entry was created,
 * or the most recent replacement of the value.
 * Lifetime duration is specified as a constructor parameter, time unit is milliseconds.
 * EvictionMap implements ConcurrentMap and uses ConcurrentHashMap internally,
 * but some methods are not supported due to the eviction policy.
 * DelayQueue with ExpiringKey is used for time-based eviction policy.
 * @param <K> key type.
 * @param <V> value type.
 * @see ConcurrentMap
 * @see ConcurrentHashMap
 * @see DelayQueue
 * @see ExpiringKey
 */
public class EvictionMap<K, V> implements ConcurrentMap<K, V>  {

  private final Map<K, V> map = new ConcurrentHashMap<>();
  private final DelayQueue<ExpiringKey<K>> delayQueue = new DelayQueue<>();
  private final long maxLifeTime;

  /**
   * Entry lifetime duration set to max value of long,
   * meaning entries will not expire.
   */
  public EvictionMap() {
    this.maxLifeTime = Long.MAX_VALUE;
  }

  /**
   * Constructor with user specifying entry lifetime duration.
   * @param maxLifeTime time in millis.
   */
  public EvictionMap(long maxLifeTime) {
    this.maxLifeTime = maxLifeTime;
  }

  @Override
  public V putIfAbsent(K key, V value) {
    removeExpiredKeys();
    V returnValue = map.putIfAbsent(key, value);
    if (returnValue == null) {
      ExpiringKey<K> expiringKey = new ExpiringKey<>(maxLifeTime, key);
      delayQueue.offer(expiringKey);
    }
    return returnValue;
  }

  @Override
  public boolean remove(Object key, Object value) {
    if (map.remove(key, value)) {
      findInDelayQueue((K)key).ifPresent(delayQueue::remove);
      return true;
    }
    return false;
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    if (map.replace(key, oldValue, newValue)) {
      findInDelayQueue(key).ifPresent(delayQueue::remove);
      ExpiringKey<K> expiringKey = new ExpiringKey<>(maxLifeTime, key);
      delayQueue.offer(expiringKey);
      return true;
    }
    return false;
  }

  @Override
  public V replace(K key, V value) {
    V returnValue = map.replace(key, value);
    if (returnValue != null) {
      findInDelayQueue(key).ifPresent(delayQueue::remove);
      ExpiringKey<K> expiringKey = new ExpiringKey<>(maxLifeTime, key);
      delayQueue.offer(expiringKey);
    }
    return returnValue;
  }

  @Override
  public int size() {
    removeExpiredKeys();
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    removeExpiredKeys();
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    removeExpiredKeys();
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    removeExpiredKeys();
    return map.containsValue(value);
  }

  @Override
  public V get(Object key) {
    removeExpiredKeys();
    return map.get(key);
  }

  @Override
  public V put(K key, V value) {
    removeExpiredKeys();
    ExpiringKey<K> expiringKey = new ExpiringKey<>(maxLifeTime, key);
    if (map.put(key, value) != null) {
      findInDelayQueue(key).ifPresent(delayQueue::remove);
    }
    delayQueue.offer(expiringKey);
    return map.put(key, value);
  }

  @Override
  public V remove(Object key) {
    V value = map.remove(key);
    findInDelayQueue((K)key).ifPresent(delayQueue::remove);
    return value;
  }

  @Override
  public void clear() {
    map.clear();
    delayQueue.clear();
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<K> keySet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<V> values() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks for expired keys and removes them
   * @see ExpiringKey
   */
  private void removeExpiredKeys() {
    ExpiringKey<K> expiringKey = delayQueue.poll();
    while (expiringKey != null) {
      map.remove(expiringKey.getKey());
      expiringKey = delayQueue.poll();
    }
  }

  /**
   * Returns ExpiringKey from delay queue if found, otherwise null, all wrapped in Optional
   * @param k key value
   * @return result of search
   * @see Optional
   */
  private Optional<ExpiringKey<K>> findInDelayQueue(K k) {
    return delayQueue.stream().filter(x -> x.getKey().equals(k)).findFirst();
  }

}
