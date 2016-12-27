package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by jiashiran on 2016/10/31.
 */
public class Commons {

    public static Log log = LogFactory.getLog(Commons.class);

    public static void log(Object ...args){
        for (Object a : args){
            System.out.print(a);
        }
        System.out.println();
    }
}
