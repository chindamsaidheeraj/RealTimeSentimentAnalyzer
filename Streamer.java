package streamer;

import com.azure.messaging.eventhubs.*;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Streamer {
    private static String connectionString = "Endpoint=sb://twittereventhubspace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=09uTz6h48XEuJJU6lPxwDy5A4stcXU/EKCCKWrPqTM8=";
    private static String eventHubName = "input";

    private static final ArrayList<Row> CSV = new ArrayList<Row>();

    public static void main(String[] args) throws FileNotFoundException {

        connectionString = args[0];
        eventHubName = args[1];

        //parsing a CSV file into Scanner class constructor
        Scanner sc = new Scanner(new File("test-2-3-mini.csv"));
        sc.useDelimiter("\n");   //sets the delimiter pattern
        while (sc.hasNext())  //returns a boolean value
        {
            String row = sc.next();
            String cols[] = row.split("\t");
            Integer target = Integer.valueOf(cols[0]);  //find and returns the next complete token from this scanner
//            System.out.println("Target is " + target);
            String text = cols[1];
//            System.out.println("Text is " + text);
            CSV.add(new Row(text, target));
        }
        sc.close();  //closes the scanner

        publishEvents();
    }

    /**
     * Method for sending events to Eventhub
     * @throws IllegalArgumentException if the EventData is bigger than the max batch size.
     */
    public static void publishEvents() {
        try {
            // create a producer client
            EventHubProducerClient producer = new EventHubClientBuilder()
                    .connectionString(connectionString, eventHubName)
                    .buildProducerClient();


            while (true) {
                // sample events in an array
                List<EventData> allEvents = new ArrayList<EventData>();

                for (int i = 0; i < 1000; i++) {
                    // generating the index using Math.random()
                    int index = (int) (Math.random() * CSV.size());

                    JSONObject json = new JSONObject();
                    json.put("target", CSV.get(index).target);
                    json.put("text", CSV.get(index).text);
                    allEvents.add(new EventData(json.toString()));
                }

//            System.out.println("Created a batch");

                // create a batch
                EventDataBatch eventDataBatch = producer.createBatch();

                for (EventData eventData : allEvents) {
                    // try to add the event from the array to the batch
                    if (!eventDataBatch.tryAdd(eventData)) {
                        // if the batch is full, send it and then create a new batch
                        producer.send(eventDataBatch);
                        eventDataBatch = producer.createBatch();

                        // Try to add that event that couldn't fit before.
                        if (!eventDataBatch.tryAdd(eventData)) {
                            throw new IllegalArgumentException("Event is too large for an empty batch. Max size: "
                                    + eventDataBatch.getMaxSizeInBytes());
                        }
                    }
                }
                // send the last batch of remaining events
                if (eventDataBatch.getCount() > 0) {
                    producer.send(eventDataBatch);
                }
//                System.out.println("Wrote a batch");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}