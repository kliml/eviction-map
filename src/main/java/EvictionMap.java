import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

public class EvictionMap<K, V> implements ConcurrentMap<K, V>  {

  private final Map<K, V> map = new ConcurrentHashMap<>();

  private final DelayQueue<ExpiringKey<K>> delayQueue = new DelayQueue<>();

  private final long maxLifeTime;

  public EvictionMap() {
    this.maxLifeTime = Long.MAX_VALUE;
  }

  public EvictionMap(long maxLifeTime) {
    this.maxLifeTime = maxLifeTime;
  }

  @Override
  public V putIfAbsent(K k, V v) {
    return null;
  }

  @Override
  public boolean remove(Object o, Object o1) {
    return false;
  }

  @Override
  public boolean replace(K k, V v, V v1) {
    return false;
  }

  @Override
  public V replace(K k, V v) {
    return null;
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
  public boolean containsKey(Object o) {
    removeExpiredKeys();
    return map.containsKey(o);
  }

  @Override
  public boolean containsValue(Object o) {
    removeExpiredKeys();
    return map.containsValue(o);
  }

  @Override
  public V get(Object o) {
    removeExpiredKeys();
    return map.get(o);
  }

  @Override
  public V put(K k, V v) {
    removeExpiredKeys();
    ExpiringKey<K> expiringKey = new ExpiringKey<>(maxLifeTime, k);
    if (map.put(k, v) != null) {
      findInDelayQueue(k).ifPresent(delayQueue::remove);
    }
    delayQueue.offer(expiringKey);
    return map.put(k, v);
  }

  @Override
  public V remove(Object o) {
    V value = map.remove(o);
    findInDelayQueue((K)o).ifPresent(delayQueue::remove);
    return value;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {

  }

  @Override
  public void clear() {
    map.clear();
    delayQueue.clear();
  }

  @Override
  public Set<K> keySet() {
    removeExpiredKeys();
    return map.keySet();
  }

  @Override
  public Collection<V> values() {
    removeExpiredKeys();
    return map.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    removeExpiredKeys();
    return map.entrySet();
  }

  private void removeExpiredKeys() {
    ExpiringKey<K> expiringKey = delayQueue.poll();
    while (expiringKey != null) {
      map.remove(expiringKey.getKey());
      expiringKey = delayQueue.poll();
    }
  }

  private Optional<ExpiringKey<K>> findInDelayQueue(K k) {
    return delayQueue.stream().filter(x -> x.getKey().equals(k)).findFirst();
  }

}
