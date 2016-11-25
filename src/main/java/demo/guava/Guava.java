package demo.guava;

import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.google.common.hash.*;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.reflect.Reflection;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.*;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.*;
import static demo.Commons.log;

/**
 * Created by jiashiran on 2016/10/31.
 */
public class Guava implements GuaveInterface {
    private float ran = 0;

    public Guava(){}
    public Guava(float a){this.ran = a;}

    public static void main(String[] args) {
        Guava g = new Guava();
        g.optional();
        //g.checkArguments();
        //g.commonObject();
        //g.ordering();
        //g.immu();
        //g.string();
        //g.range();
        //g.hash();
        //g.refliect();
    }

    public void refliect(){
        List<String> list = Arrays.asList();
        TypeToken t = TypeToken.of(list.getClass());
        log(t.getType());

        GuaveInterface o = Reflection.newProxy(GuaveInterface.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                log("before");
                Object o = method.invoke(new Guava(),args);
                log("after");
                return o;
            }
        });
        o.toStr("asdasd");
        Reflection.initialize(Guava.class);
    }

    private void hash(){
        HashFunction hs = Hashing.crc32c();
        Hasher hasher = hs.newHasher();
        HashCode hashCode = hasher.putBoolean(false).putByte(Byte.valueOf("23")).putDouble(3.14).putInt(43).hash();
        log(hashCode.toString());
    }

    private void range(){//范围
        Range<Integer> r = Range.closed(1,99);
        log(r.contains(100));
        r = Range.openClosed(1,99);
        log(r.contains(99));
        r = Range.atLeast(30);
        log(r.contains(29));
        log(r.isConnected(Range.open(31,32)));//测试是否有区间同时包含于这两个区间
        log(r.intersection(Range.closedOpen(34,35)));//交集
        log(r.span(Range.open(22,33)));//跨区间
    }

    private void type(){//原生类型加强
        List<Byte> list = Bytes.asList(Byte.parseByte("1"),Byte.parseByte("2"),Byte.parseByte("3"));
        byte[] b = new byte[]{};
        Bytes.contains(b , Byte.parseByte("3"));
        List<Integer> l =  Ints.asList(1,2,3,4);

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
        String s = new String(new byte[]{},Charsets.UTF_8);
        log(o.or(2));
        //log(o.get());
    }

    @Override
    public void toStr(String a) {
        log("toStr:",a);
    }
}
