package streamer;

//This class contains the model for a row of data
public class Row {
    Integer target;
    String text;

    public Row(String text, Integer target) {
        this.target = target;
        this.text = text;
    }
}
