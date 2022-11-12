package ru.RSOI.Gateway.FaultTolerance;

import java.util.Timer;
import java.util.TimerTask;

public class FTCircuitBreaker {

    private static int Cnt = 0;

    public enum State
    {
        Open,
        HalfOpen,
        Closed
    }

    public FTCircuitBreaker()
    {
        this.CircuitState = State.Closed;
        this.FailsCounter = 0;
        this.MaxFails = 1;

        this.RetryTime = 10 * 1000;
    }

    public State GetState()
    {
        return CircuitState;
    }
    public void SetState(State NewState) {CircuitState = NewState;}

    public void SetMaxFails(int MaxFails)
    {
        this.MaxFails = MaxFails;
    }

    public void SetRetryTimerTask(TimerTask OnRetry)
    {
        RetryTimerTask = OnRetry;
    }

    public long GetRetryTime()
    {
        return RetryTime;
    }
    public void SetRetryTime(long TimeMs)
    {
        RetryTime = TimeMs;
    }

    public void OnSuccess()
    {
        if (CircuitState == State.HalfOpen)
        {
            CircuitState = State.Closed;
            FailsCounter = 0;
        }
    }

    public void OnFail()
    {
        if (CircuitState == State.Closed)
        {
            FailsCounter++;
            if (FailsCounter == MaxFails)
            {
                CircuitState = State.Open;
                RetryTimer = new Timer("RetryTimer_" + Integer.toString(Cnt++));
                RetryTimer.schedule(RetryTimerTask, RetryTime);
            }
        }
        else if (CircuitState == State.HalfOpen)
        {
            CircuitState = State.Open;
            RetryTimer = new Timer("RetryTimer_" + Integer.toString(Cnt++));
            RetryTimer.schedule(RetryTimerTask, RetryTime);
        }
    }

    private State CircuitState;
    private int FailsCounter;
    private int MaxFails;

    private Timer RetryTimer;
    private TimerTask RetryTimerTask;
    private long RetryTime;

}
