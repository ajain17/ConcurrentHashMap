import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


/**
 * An expiring map is a Map in which entries 
 * are evicted of the map after their time to live
 * expired.
 * 
 * If an map entry hasn't been accessed for <code>
 * timeToLiveMillis</code> the map entry is evicted
 * out of the map, subsequent to which an attempt
 * to get the key from the map will return null. 
 * 
 * @param <K>
 * @param <V>
 */
class KeyValuePair < K, V > implements Map.Entry < K, V > {
 K key;
 V value;
 Long ttl;
 KeyValuePair < K,
 V > next = null;
 public KeyValuePair() {

 }
 public KeyValuePair < K,
 V > getNext() {
  return next;
 }

 public void setNext(KeyValuePair < K, V > next) {
  this.next = next;
 }

 public KeyValuePair(K key, V value, Long ttl) {
  super();
  this.key = key;
  this.value = value;
  this.ttl = ttl;
 }

 public K getKey() {
  return key;
 }

 public void setKey(K key) {
  this.key = key;
 }

 public V getValue() {
  return value;
 }

 public V setValue(V value) {
  this.value = value;
  return this.value;
 }

 public Long getTtl() {
  return ttl;
 }

 public void setTtl(Long ttl) {
  this.ttl = ttl;
 }
}
public class ExpiringMap < K, V > implements Map < K, V > {
 Timer timer;
 static int CAPACITY = 100;
 KeyValuePair < K,
 V > mapList[] = new KeyValuePair[CAPACITY];
 private int size = 0;
 static final long MAX_TTL = 9223372036854775807 L;
 static final long MIN_TTL = 0;
 private long ttl;

 public ExpiringMap(long timeToLiveInMillis) {
  if (timeToLiveInMillis < MIN_TTL)
   throw new IllegalArgumentException("Illegal initial time to live: " + timeToLiveInMillis);
  else if (timeToLiveInMillis > MAX_TTL)
   timeToLiveInMillis = MAX_TTL;
  ttl = timeToLiveInMillis;
  timer = new Timer();
  timer.schedule(new EvictStaleKeys(), 0, //initial delay
   1 * 1000);
 }

 @Override
 public int size() {
  // TODO Auto-generated method stub
  return size;
 }

 @Override
 public boolean isEmpty() {
  // TODO Auto-generated method stub
  return (size == 0);
 }

 @Override
 public boolean containsKey(Object key) {
  // TODO Auto-generated method stub
  int index = getHash(key);
  KeyValuePair < K, V > list = mapList[index];
  if (list == null)
   return false;
  while (list != null) {
   if (list.getKey().equals(key)) {
    list.setTtl(ttl);
    return true;
   }
   list = list.next;
  }
  return false;
 }

 @Override
 public boolean containsValue(Object value) {
  // TODO Auto-generated method stub
  if (size == 0)
   return false;
  KeyValuePair < K, V > list;
  for (int i = 0; i < CAPACITY; i++) {
   if (mapList[i] != null) {
    list = mapList[i];
    //only one key value pair at this index
    if (list.next == null && list.getValue() == value) {
     list.setTtl(ttl); //update ttl for this key value pair since it has been accessed
     return true;
    } else //other key value pairs hashed to this index
    {
     while (list.next != null) {
      if (list.getValue() == value) {
       list.setTtl(ttl); //update ttl for this key value pair
       return true;
      }
      list = list.next;
     }
    }
   }
  }
  return false;
 }

 @Override
 public Vget(Object key) {
  // TODO Auto-generated method stub
  int index = getHash(key);
  KeyValuePair < K, V > list = mapList[index];
  return getValueForKey(list, key);
 }

 private V getValueForKey(KeyValuePair < K, V > list, Object key) {
  while (list != null) {
   if (list.getKey().equals(key)) {
    list.setTtl(ttl); //update ttl for this key value pair
    return list.getValue();
   }
   list = list.next;
  }
  return null;
 }

 @Override
 public V put(K key, V value) {
  int index = getHash(key);
  insertValue(index, key, value);
  return value;
 }

 private void insertValue(int index, K key, V value) {
  KeyValuePair < K, V > list = mapList[index];
  // if list is empty , enter as first element
  if (list == null) {
   mapList[index] = new KeyValuePair < K, V > (key, value, ttl);
   size++;
  } else {
   boolean done = false;
   //updating the first value itself
   if (list.next == null && list.getKey().equals(key)) {
    list.setValue(value);
    list.setTtl(ttl);
    done = true;
    return;
   }

   while (list.next != null) {
    if (list.getKey().equals(key)) {
     list.setValue(value);
     list.setTtl(ttl);
     done = true;
     break;
    }
    list = list.next;
   }
   // add at the end of the list
   if (!done) {
    list.next = new KeyValuePair < K, V > (key, value, ttl);
    size++;
   }
  }

 }

 @Override
 public V remove(Object key) {
  // TODO Auto-generated method stub
  int index = getHash(key);
  V value;
  KeyValuePair < K, V > list = mapList[index];
  if (list == null)
   return null;
  // if only one element is present in the list ,set the index to null
  if (list.getKey().equals(key)) {
   if (list.next == null) {
    value = list.getValue();
    mapList[index] = null;
    size--;
    return value; //value that is removed.
   }
  }
  KeyValuePair < K, V > prev = null;
  //if more than one element is present in the list
  do {
   //if first element, prev will be null. 
   if (list.getKey().equals(key)) {
    if (prev == null) {
     list = list.getNext();
     size--;
    } else {
     prev.next = list.getNext();
     size--;
    }
    break;
   }
   list = list.next;
  } while (list != null);
  mapList[index] = list;
  return null;
 }
 @Override
 public void putAll(Map < ? extends K, ? extends V > m) {
  for (java.util.Map.Entry < ? extends K, ? extends V > entry : m.entrySet()) {
   int index = getHash(entry.getKey());
   if (mapList[index] == null) {
    System.out.println("null: key = " + entry.getKey() + " " + entry.getValue());
    mapList[index] = new KeyValuePair < K, V > (entry.getKey(), entry.getValue(), ttl);
    size++;
   } else {
    KeyValuePair < K, V > list = mapList[index];
    boolean done = false;
    //updating the first value itself
    if (list.next == null && list.getKey().equals(entry.getKey())) {
     list.setValue(entry.getValue());
     list.setTtl(ttl);
    }
    while (list.next != null) {
     if (list.getKey().equals(entry.getKey())) {
      list.setValue(entry.getValue());
      list.setTtl(ttl);
      done = true;
      break;
     }
     list = list.next;
    }
    // add at the end of the list
    if (!done) {
     list.next = new KeyValuePair < K, V > (entry.getKey(), entry.getValue(), ttl);
     size++;
    }
   }
  }
 }

 @Override
 public void clear() {
  // TODO Auto-generated method stub
  for (int i = 0; i < CAPACITY; i++) {
   mapList[i] = null;
  }
  size = 0;
 }

 @Override
 public Set < K > keySet() {
  Set < K > ks = new HashSet < K > ();
  for (int i = 0; i < CAPACITY; i++) {
   if (mapList[i] != null) {
    KeyValuePair < K, V > list = mapList[i];
    //only one key value pair at this index
    if (list.next == null) {
     list.setTtl(ttl); //update ttl for this key value pair
     ks.add(list.getKey());
    } else //other key value pairs hashed to this index
    {
     while (list.next != null) {
      list.setTtl(ttl); //update ttl for this key value pair
      ks.add(list.getKey());
      list = list.next;
     }

    }
   }
  }
  return ks;

 }

 @Override
 public Collection < V > values() {
  // TODO Auto-generated method stub
  ArrayList < V > values = new ArrayList < V > ();
  for (int i = 0; i < CAPACITY; i++) {
   if (mapList[i] != null) {
    KeyValuePair < K, V > list = mapList[i];
    //only one key value pair at this index
    if (list.next == null) {
     list.setTtl(ttl); //update ttl for this key value pair
     values.add(list.getValue());
    } else //other key value pairs hashed to this index
    {
     while (list.next != null) {
      list.setTtl(ttl); //update ttl for this key value pair
      values.add(list.getValue());
      list = list.next;
     }

    }
   }
  }
  return values;
 }

 @Override
 public Set < java.util.Map.Entry < K,
 V >> entrySet() {
  Set < java.util.Map.Entry < K, V >> entrySet = new java.util.HashSet < > ();
  for (int i = 0; i < CAPACITY; i++) {
   if (mapList[i] != null) {
    KeyValuePair < K, V > list = mapList[i];
    //only one key value pair at this index
    while (list.next != null) {
     list.setTtl(ttl); //update ttl for this key value pair
     entrySet.add(list);
     list = list.next;
    };
    if (list.next == null) {
     list.setTtl(ttl);
     entrySet.add(list);
    }
   }
  }

  return entrySet;
 }
 private int getHash(Object key) {
  int hash = key.hashCode();
  return hash % 100;
 }
 class EvictStaleKeys extends TimerTask {
  public void evict(KeyValuePair < K, V > list) {
   if (list.getTtl() > 0) {
    list.setTtl(list.getTtl() - 1);
   } else {
    remove(list.getKey());
   }
  }
  public void run() {
   try {
    Thread.sleep(1000);
   } catch (InterruptedException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
   }
   KeyValuePair < K, V > list;
   for (int index = 0; index < CAPACITY; index++) {
    list = mapList[index];
    if (list != null) {
     //if this is the only element
     if (list.next == null) {
      evict(list);
     } else {
      while (list.next != null) {
       evict(list);
       list = list.next;
      }
     }
    }
   }
  }
 }
}
