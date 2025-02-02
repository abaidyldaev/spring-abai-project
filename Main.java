import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class CircularBuffer {
    private final int[] buffer;
    private int count = 0;
    private int head = 0;  
    private int tail = 0;  
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public CircularBuffer(int size) {
        buffer = new int[size];
    }

    public void put(int value) throws InterruptedException {
        lock.lock();
        try {
            while (count == buffer.length) {
                notFull.await(); 
            }
            buffer[tail] = value;
            tail = (tail + 1) % buffer.length;
            count++;
            notEmpty.signal(); 
        } finally {
            lock.unlock();
        }
    }

    public int get() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            int value = buffer[head];
            head = (head + 1) % buffer.length;
            count--;
            notFull.signal();
            return value;
        } finally {
            lock.unlock();
        }
    }
}

class Producer implements Runnable {
    private final CircularBuffer buffer;
    private final int id;

    public Producer(CircularBuffer buffer, int id) {
        this.buffer = buffer;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                buffer.put(i);
                System.out.println("Producer " + id + " produced: " + i);
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Consumer implements Runnable {
    private final CircularBuffer buffer;
    private final int id;

    public Consumer(CircularBuffer buffer, int id) {
        this.buffer = buffer;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            while (true) {
                int value = buffer.get();
                System.out.println("Consumer " + id + " consumed: " + value);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        CircularBuffer buffer = new CircularBuffer(5);

        Thread producer1 = new Thread(new Producer(buffer, 1));
        Thread producer2 = new Thread(new Producer(buffer, 2));

        Thread consumer1 = new Thread(new Consumer(buffer, 1));
        Thread consumer2 = new Thread(new Consumer(buffer, 2));

        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
    }
}
