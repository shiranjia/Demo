package demo.thread;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static demo.Commons.log;

/**
 * Created by jiashiran on 2016/11/3.
 */
public class thread {
    Lock lock = new ReentrantLock();
    Condition condition1 = lock.newCondition();
    Condition condition2 = lock.newCondition();
    public static void main(String[] args) {
        thread t = new thread();
        t.countDownLatch();
        //t.cyclicBarrier();
        //t.semaphore();
        //t.exchanger();
        //t.lock();
    }

    //lock start
    private void lock(){
        thread t = new thread();
            for (int i=0;i<10;i++){
                new L(t).start();
                new U1(t).start();
                new U2(t).start();
            }
    }
    class L extends Thread{
        private thread t;
        public L(thread tt){
            this.t = tt;
        }
        public void run(){
            t.l();
        }
    }
    class U1 extends Thread{
        private thread t;
        public U1(thread tt){
            this.t = tt;
        }
        public void run(){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            t.u1();
        }
    }
    class U2 extends Thread{
        private thread t;
        public U2(thread tt){
            this.t = tt;
        }
        public void run(){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            t.u2();
        }
    }
    private void l(){
        try{
            log("run");
            lock.lock();
            log("await condition1");
            condition1.await();
            log("await condition2");
            condition2.await();
            log("execute ...");
            Thread.sleep(1000);
        }catch (Exception w){
            w.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    public void u1(){
        try{
            lock.lock();
            condition1.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    public void u2(){
        try{
            lock.lock();
            condition2.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    //end lock

    private void exchanger(){
        Exchanger<String> e = new Exchanger<String>();
        new Ex1(e).start();new Ex2(e).start();
    }
    class Ex1 extends Thread{
        private Exchanger<String> exchanger;
        public Ex1(Exchanger<String> x){
            this.exchanger=x;
        }
        public void run(){
            String a = "dddsss";
            try {
                String b = exchanger.exchange(a);
                log("1:",b+"11111");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    class Ex2 extends Thread{
        private Exchanger<String> exchanger;
        public Ex2(Exchanger<String> x){
            this.exchanger=x;
        }
        public void run(){
            String a = "sadawaf";
            try {
                String b = exchanger.exchange(a);
                log("2:",b+"22222");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void semaphore(){
        final Semaphore semaphore = new Semaphore(5);
        for (int i = 0;i<100;i++){
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        semaphore.acquire();
                        log("run..",finalI);
                        Thread.sleep(2000);
                        semaphore.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void cyclicBarrier(){
        CyclicBarrier barrier = new CyclicBarrier(5, new Runnable() {
            @Override
            public void run() {
                log("sdasd");
            }
        });
        for (int i=0;i<20;i++){
           new R(i,barrier).start();
         }

    }

    class R extends Thread{
        private int i;
        private CyclicBarrier barrier;
        public R(int a,CyclicBarrier b){
            this.barrier = b;
            i = a;
        }

        @Override
        public void run() {
            log("run",i);
            try {
                barrier.await();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            log("over",i);
        }
    }

    private void countDownLatch(){
        final CountDownLatch latch = new CountDownLatch(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                log("work1");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                log("start2");
                latch.countDown();
            }
        }).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log("over");
    }

}
