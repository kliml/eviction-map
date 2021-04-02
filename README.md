# eviction-map
Generic structure that acts as key-value map with following time-based eviction policy: expire entries after the specified duration has passed since the entry was created, or the most recent replacement of the value. \
Lifetime duration is specified as a constructor parameter, time unit is milliseconds.
## Design
EvictionMap implements ConcurrentMap and uses ConcurrentHashMap internally, but some methods are not supported due to the eviction policy. \
Eviction tracking is based on DelayQueue with ExpiringKey and prior to any operation check for expired entries is being made.
