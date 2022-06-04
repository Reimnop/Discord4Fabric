package me.reimnop.d4f;

public class Timer<T> {
    public interface Task<T> {
        void invoke(T arg);
    }

    private final int interval;
    private final Task<T> task;

    private int t = 0;

    public Timer(int interval, Task<T> task) {
        this.interval = interval;
        this.task = task;
    }

    public void tick(T arg) {
        if (t < interval) {
            t++;
            return;
        }
        t = 0;

        task.invoke(arg);
    }
}
