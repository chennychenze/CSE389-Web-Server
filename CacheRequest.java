import java.util.HashMap;
import java.util.Map;

public class CacheRequest{
    
    private Map<String, String> cache;    //hashmap creation to store cached data with the file as a key

    
    public CacheRequest(){
        this.cache = new HashMap<>();     //Constructor for Cache initialization
    }

    public String getFromCache(String key){
        return cache.get(key);                  //used to get data from cache based on the file selected
    }

    public void addToCache(String key, String data){
        cache.put(key, data);                           //Adds data to cache for a specific file
    }





}
