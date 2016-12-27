package demo.es;

/**
 * Created by jiashiran on 2016/12/27.
 */
public class Node {
    private String transportAddress;

    private String httpAddress;

    public Node(String t,String h){
        this.transportAddress = t;
        this.httpAddress = h;
    }

    public String getTransportAddress() {
        return transportAddress;
    }

    public void setTransportAddress(String transportAddress) {
        this.transportAddress = transportAddress;
    }

    public String getHttpAddress() {
        return httpAddress;
    }

    public void setHttpAddress(String httpAddress) {
        this.httpAddress = httpAddress;
    }

    @Override
    public String toString(){
        return "transportAddress:"+transportAddress+",httpAddress:"+httpAddress;
    }
}
