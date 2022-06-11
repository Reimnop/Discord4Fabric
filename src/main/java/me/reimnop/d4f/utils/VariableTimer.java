package me.reimnop.d4f.utils;

public class VariableTimer<T> {
    public interface Task<T> {
        void invoke(T arg);
    }

    // over engineering go brr
    public interface IntervalGetter {
        Integer getInterval();
    }

    private final IntervalGetter intervalGetter;
    private final Task<T> task;

    private int t = 0;

    public VariableTimer(IntervalGetter intervalGetter, Task<T> task) {
        this.intervalGetter = intervalGetter;
        this.task = task;
    }

    public void tick(T arg) {
        if (t < intervalGetter.getInterval()) {
            t++;
            return;
        }
        t = 0;

        task.invoke(arg);
    }
}
