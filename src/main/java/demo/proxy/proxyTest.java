package demo.proxy;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import static demo.Commons.log;

/**
 * Created by jiashiran on 2016/11/25.
 */
public class proxyTest {

    public static void main(String[] args) {
        try {
            A a = (A) proxy.newProxyInstance("demo.proxy.A", "demo.proxy.InterceptorHandler");
            //A p = (A) Proxy.newProxyInstance(a.getClass().getClassLoader(),A.class.getInterfaces(),new InterceptorHandler());
            log(a.say("test 111")) ;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
