import java.util.concurrent.TimeUnit;

public class DataFetcher {
    
    //references the CacheRequest file to manage the caching of files
    private CacheRequest cacheRequest;  

    //Constructor for initializing the DataFetcher with the CacheRequest
    public DataFetcher(){
        this.cacheRequest = new CacheRequest();
    }

    //get data for a specific file
    public String fetchData(String fileIdentifier){

        //attempts to get the data from the cache for that specific file
        String cachedData = cacheRequest.getFromCache(fileIdentifier);

        //checks if data is in the Cache and returns the cache data if there's any
        if (cachedData != null){
            System.out.println("Data Fetched from Cache: " + cachedData);
            return cachedData;
        }
        else{
            String newData = fetchDataFromSource(fileIdentifier);       //if data isn't in the cache, get it from the file

            cacheRequest.addToCache(fileIdentifier, newData);           //Add the new data from the file into the cache

            System.out.println("Data fetched from source: " + newData); //after finishing retrieving the data, return the output of the newdata
            return newData;

        }
     }

    //just a simulator for fetching data from a server for a specific file
    private String fetchDataFromSource(String fileIdentifier){
        return "Content of " + fileIdentifier;                      //it just returns some placehodler, not the actual data
     }


    //main method to test the DataFetcher program
    public static void main(String[] args) {
        //creates a instance of the DataFetcher
        DataFetcher dataFetcher = new DataFetcher();

        //fetches data for txt file without caching while measuring time
        long startTimeWithoutCache = System.nanoTime();
        for (int i = 0; i < 5; i++) {
            dataFetcher.fetchDataFromSource("examplefile.txt");
        }

        long endTimeWithoutCache = System.nanoTime();
        long durationWithoutCache = TimeUnit.MILLISECONDS.convert(endTimeWithoutCache - startTimeWithoutCache, TimeUnit.NANOSECONDS);
        System.out.println("Time taken without cache: " + durationWithoutCache + " ms");


        //fetches data for txt file with caching while measuring time
        long startTimeWithCache = System.nanoTime();
        for (int i = 0; i < 5; i++) {
            dataFetcher.fetchData("examplefile.txt");
        }
        long endTimeWithCache = System.nanoTime();
        long durationWithCache = TimeUnit.MILLISECONDS.convert(endTimeWithCache - startTimeWithCache, TimeUnit.NANOSECONDS);
        System.out.println("Time taken with cache: " + durationWithCache + " ms");

        //displays cache info
        System.out.println("Cache info: " + dataFetcher.cacheRequest);

     }
    
}
