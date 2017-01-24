An expiring map is a Map in which entries are evicted of the map after their time to live expired. 
If a map entry hasn't been accessed for <code> timeToLiveMillis</code> the map entry is evicted out of the map, subsequent to which an attempt to get the key from the map will return null. 
