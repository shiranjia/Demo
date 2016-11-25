package demo.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static demo.Commons.log;

/**
 * Created by jiashiran on 2016/11/2.
 */
public class zk {
    static String zk = "192.168.150.119,192.168.150.120,192.168.150.121:2181";
    static CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString(zk)
            .sessionTimeoutMs(5000)
            .retryPolicy(new ExponentialBackoffRetry(1000,3))
            .build();

    static String path = "/ppp";

    public static void main(String[] args) {
        final zk z = new zk();

        //z.cgudl();
        //z.checkExits();
        z.EPHEMERAL();
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void EPHEMERAL(){
        try {
            client.start();
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/xConfig/test/cluster/127.0.0.1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkExits(){

        client.start();
        try {
            Stat stat = client.checkExists().forPath("/afs");
            log(stat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cgudl(){
        client.start();

        try {
            client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
            Thread.sleep(2000);
            client.create()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path,"init".getBytes());
            Stat stat = new Stat();
            final NodeCache cache = new NodeCache(client,path,false);
            cache.start();
            cache.getListenable().addListener(new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    log("event:",new String(cache.getCurrentData().getData()));
                }
            });
            PathChildrenCache childrenCache = new PathChildrenCache(client,path,true);
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework,
                                       PathChildrenCacheEvent event)
                        throws Exception {
                    log("...",event.getType());
                    switch (event.getType()){
                        case CHILD_ADDED:{
                            log("CHILD_ADDED:",event.getData());
                            break;
                        }
                        case CHILD_UPDATED:{
                            log("CHILD_UPDATED:",event.getData());
                            break;
                        }
                        case CHILD_REMOVED:{
                            log("CHILD_REMOVED:",event.getData());
                            break;
                        }
                        default:log("default:",event.getData());break;
                    }
                }
            });
            Thread.sleep(2000);

            client.getData().storingStatIn(stat).forPath(path);

            log(stat.getVersion());

            byte[] data = client.getData().forPath(path);
            log("brfore:",new String(data));

            stat = client.setData().withVersion(stat.getVersion()).forPath(path,"dddsa".getBytes());

            data = client.getData().forPath(path);
            log("after:",new String(data),",version:",stat.getVersion());
            stat = client.setData().withVersion(stat.getVersion()).forPath(path,"134adw323".getBytes());
            data = client.getData().forPath(path);
            log("after:",new String(data),",version:",stat.getVersion());

            data = client.getData().inBackground().forPath(path);
            Thread.sleep(1000);
            client.create().withMode(CreateMode.PERSISTENT).forPath(path + "/asv","123".getBytes());
            Thread.sleep(1000);
            client.setData().forPath(path + "/asv" ,"345".getBytes());
            Thread.sleep(1000);
            client.delete().forPath(path + "/asv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
