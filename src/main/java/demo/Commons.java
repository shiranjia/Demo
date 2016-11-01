package demo;

/**
 * Created by jiashiran on 2016/10/31.
 */
public class Commons {

    public static void log(Object ...args){
        for (Object a : args){
            System.out.print(a);
        }
        System.out.println();
    }
}
