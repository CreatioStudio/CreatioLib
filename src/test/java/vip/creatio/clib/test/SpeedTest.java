package vip.creatio.clib.test;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.FutureTask;

public class SpeedTest {

    final static int intensity = 200000;
    final static int amount = 50;

    private final Queue<RunningTest> queue = new LinkedList<>();
    private long result = 0L;

    @Test
    public void speed_test() {
        for (int i = -1; i < amount; i++) {
            queue.add(new RunningTest());
        }
        int i = 0;
        int suc = 1;
        while (!queue.isEmpty()) {
            RunningTest task = queue.poll();
            task.run();
            try {
                System.out.println("Task." + i + " passed. Time used: " + task.get() + "ns (" + task.get()/1000000 + "ms)");
                if (i >= 1) {   /* For some reason the first attempt always takes 2-3 times longer than others, so we discard it. */
                    result += task.get();
                    suc++;
                }
            } catch (Exception e) {
                System.out.println("Task." + i  + " is interrupted: ");
                e.printStackTrace();
                break;
            }
            i++;
        }

        System.out.println("\n\nTOTAL " + (i - 1)  + " TASKS, AVERAGE TAKES: " + result / suc + "ns (" + result/(1000000 * suc) + "ms)");
    }

    private static class RunningTest extends FutureTask<Long> {

        public RunningTest() {
            super(() -> {
                //before countdown

                //init value


                long start = System.nanoTime();

                List<String> list = new LinkedList<>();
                for (int i = 0; i < 100000; i++) {
                    list.add("<R>SADASD<R>asd<R>asdasd<R>");
                }

                for (int i = 0; i < 100000; i++) {
                    list.set(i, list.get(i).replaceAll("<R>", ">N<"));
                }

                System.out.println(list.get(0));

                long end = System.nanoTime();

                return end - start;
            });
        }
    }
}
