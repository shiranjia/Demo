package demo.nio;

import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.google.common.util.concurrent.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static demo.nio.Commons.*;
import static com.google.common.base.Preconditions.*;

/**
 * Created by jiashiran on 2016/10/31.
 */
public class Guava {
    private float ran = 0;

    public Guava(){}
    public Guava(float a){this.ran = a;}

    public static void main(String[] args) {
        Guava g = new Guava();
        //g.optional();
        //g.checkArguments();
        //g.commonObject();
        //g.ordering();
        //g.immu();
        g.string();
    }

    private void string(){
        Joiner joiner = Joiner.on(";").skipNulls();
        joiner.join("sdasd",null,"sdad","dfsger");
        joiner.join(Arrays.asList("asdas",null,"asda"));
        log(Splitter.on(",").trimResults().omitEmptyStrings().split("asda,,dfsf,,,,sdfawf"));
        String a = "asf";
        a.getBytes(Charsets.UTF_8);
    }

    private void concurrecent(){
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        ListenableFuture future = service.submit(new Callable() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });
        Futures.addCallback(future, new FutureCallback() {
            @Override
            public void onSuccess(Object result) {
                log("success");
            }

            @Override
            public void onFailure(Throwable t) {
                log("fail");
            }
        });
    }

    private void collectionUtils(){
        List<Guava> list = Lists.newArrayList();
        Map<String,Guava> map = Maps.newHashMap();
    }

    private void immu(){
        ImmutableList<String> s = ImmutableList.of("a","b","c");//不可变集合
        ImmutableMap<String,String> map = ImmutableMap.of("a","234","v","d");
        Multimap<String,String> map1 = HashMultimap.create();
        map1.put("a","123");map1.put("a","asdfe");//一key多value
        map1.put("b","asdfaef");
    }

    private void exception(){
        try{
                if(ran==1){
                    throw new IOException("io");
                }else {
                    throw new SQLException("sql");
                }
        }catch (IOException | SQLException e){
            try {
                Throwables.throwIfInstanceOf(e , IOException.class);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void ordering(){//排序
        Ordering<Guava> o = Ordering.natural().nullsFirst().onResultOf(new Function<Guava, Comparable>() {
            @Override
            public Comparable apply(Guava input) {
                return input.ran;
            }
        }).reverse();
        List<Guava> g = new ArrayList<>();
        g.add(new Guava(8));g.add(new Guava(3));g.add(new Guava(1));g.add(new Guava(2));
        g = o.sortedCopy(g);
        log(g);
    }

    private void commonObject(){//object 方法
        log(Objects.equal("a",null)) ;
        log(Objects.hashCode("a","b",123,'c',2.56));
        log(MoreObjects.toStringHelper("a").add("asds",123));
        log(ComparisonChain.start().compare(155,42).compare(5,4).compare(6,1).result());
    }

    private void checkArguments(){//条件检查
        checkArgument(1==0);
        checkNotNull(null);
        checkState(1==1);
        checkElementIndex(1,5);
        checkPositionIndex(5,5);
        checkPositionIndexes(2,4,5);
    }

    private void optional(){//判断null
        Optional<Integer> o = Optional.of(new Integer(1));
        o = Optional.fromNullable(null);
        log(o.isPresent());
        log(o.or(2));
        log(o.get());
    }
}
