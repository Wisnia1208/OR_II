/*
Skład sekcji:
Kowalska Dorota
Sułkowski Andrzej
Wiśniewski Jakub

zadanie 18
 */
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DormitoryBathroom {
    private static final Semaphore bathroom = new Semaphore(1);
    private static final Semaphore maleQueue = new Semaphore(1);
    private static final Semaphore femaleQueue = new Semaphore(1);
    private static int maleCount = 0;
    private static int femaleCount = 0;

    public static void main(String[] args) {
        int nm = 5; // number of men
        int nk = 5; // number of women

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(nm + nk + 1);

        for (int i = 0; i < nm; i++) {
            executorService.execute(new Person("Male"));
        }

        for (int i = 0; i < nk; i++) {
            executorService.execute(new Person("Female"));
        }

        // Schedule a task to shut down the executor service after 10 seconds
        executorService.schedule(() -> {
            System.out.println("Stopping the program...");
            executorService.shutdownNow();
        }, 10, TimeUnit.SECONDS);
    }

    static class Person implements Runnable {
        private final String gender;
        private final Random random = new Random();

        Person(String gender) {
            this.gender = gender;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Do something
                    Thread.sleep(random.nextInt(1000));

                    // Enter the queue
                    if (gender.equals("Male")) {
                        maleQueue.acquire();
                        synchronized (DormitoryBathroom.class) {
                            if (maleCount == 0) {
                                femaleQueue.acquire();
                            }
                            maleCount++;
                        }
                        maleQueue.release();
                    } else {
                        femaleQueue.acquire();
                        synchronized (DormitoryBathroom.class) {
                            if (femaleCount == 0) {
                                maleQueue.acquire();
                            }
                            femaleCount++;
                        }
                        femaleQueue.release();
                    }
                    System.out.println(gender + " is waiting in the queue.");

                    // Enter the bathroom if available
                    bathroom.acquire();
                    System.out.println(gender + " is using the bathroom.");
                    Thread.sleep(random.nextInt(100));

                    // Exit the bathroom
                    System.out.println(gender + " exited the bathroom.");
                    bathroom.release();

                    // Leave the queue
                    synchronized (DormitoryBathroom.class) {
                        if (gender.equals("Male")) {
                            maleCount--;
                            if (maleCount == 0) {
                                femaleQueue.release();
                            }
                        } else {
                            femaleCount--;
                            if (femaleCount == 0) {
                                maleQueue.release();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}