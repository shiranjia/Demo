package demo.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import demo.Commons;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by jiashiran on 2016/12/26.
 */
public class ores {

    private final Log log = LogFactory.getLog(ores.class);

    TransportClient client = null;

    public ores() {
        try {
            //client = new PreBuiltTransportClient(Settings.builder().put("client.transport.sniff", true).build());
            client = new PreBuiltTransportClient(Settings.EMPTY);
           /* List<Node> nodes = getNodes("http://192.168.200.196:9203/_nodes");
            for (Node node : nodes){
                String[] n = node.getTransportAddress().split(":");
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(n[0]), Integer.parseInt(n[1])));
            }*/
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.111.128"), 9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            log.error(e);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e);
        }
    }

    /**
     * 根据域名获取所有节点信息
     * @param host
     * @return
     */
    public static List<Node> getNodes(String host){
        List<Node> list = new ArrayList<Node>();
        try {
            URL url = new URL(host);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream in = connection.getInputStream();
            byte[] bytes = new byte[1024];
            int i;
            StringBuilder json = new StringBuilder(5000);
            while ((i = in.read(bytes)) > 0) {
                json.append(new String(bytes,0,i));
            }
            JSONObject object = JSON.parseObject(json.toString());
            Iterator<Map.Entry<String,Object>> iterator = object.getJSONObject("nodes").entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String,Object> map = iterator.next();
                JSONObject node = (JSONObject) map.getValue();
                String transportAddress = node.getString("transport_address");
                String httpAddress = node.getString("http_address");
                Node n = new Node(transportAddress,httpAddress);
                list.add(n);
            }
            Commons.log.info(object);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }



    public static void main(String[] args) {
        ores es = new ores();
        Emploee e = new Emploee();
        e.setAge(123);
        e.setId(1);
        e.setName("dddddddddddddddd");
        /*es.put("emploees","emploee",e);
        String json = es.get("emploees","emploee","1");
        System.out.println(json);*/
        SearchHit[] hits = es.search();
        Commons.log(hits);
        //es.delete("emploees","emploee","1");
    }

    public String get(String index,String type,String id){
        GetResponse response = client.prepareGet(index, type, id).get();
        log.info(response.toString());
        return response.getSourceAsString();
    }

    public SearchHit[] search(){
        SearchResponse response = client.prepareSearch("emploees")
                .setTypes("emploee")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery("id", "1"))                 // Query
                .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)                    //sort
                .setFrom(0)
                .setSize(60)
                .setExplain(true)
                .get();
        Commons.log(response.getHits().getHits()[0].getSourceAsString());
        return response.getHits().getHits();
    }

    public <T> boolean put(String index,String type,T t){
        Class tClass = t.getClass();
        Field[] fileds = tClass.getDeclaredFields();
        try {
        XContentBuilder xContentBuilder = jsonBuilder().startObject();
        String idValue = null;
        for (Field field : fileds){
                String name = field.getName();
                name = "get" + name.substring(0,1).toUpperCase() + name.substring(1,name.length());
                Method getMethod = tClass.getDeclaredMethod(name,new Class[]{});
                Object value = getMethod.invoke(t);
                if(field.getName().equals("id")){
                    idValue = value.toString();
                }
               xContentBuilder.field(field.getName(), value.toString());
               log.info(name + ":" + value);
        }
        IndexResponse response;
        if(idValue == null){
            response = client.prepareIndex(index, type)
                    .setSource(xContentBuilder.endObject()).get();
        }else {
            response = client.prepareIndex(index, type, idValue)
                    .setSource(xContentBuilder.endObject()).get();
        }
        log.info("status:" + response.status().getStatus());
        return 200 == response.status().getStatus();
        } catch (NoSuchMethodException e) {
            log.error(e);
        } catch (IllegalAccessException e) {
            log.error(e);
        } catch (InvocationTargetException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
        return false;
    }

    public boolean delete(String index,String type,String id){
        DeleteResponse response = client.prepareDelete(index, type, id).get();
        return 200 == response.status().getStatus();
    }



}
