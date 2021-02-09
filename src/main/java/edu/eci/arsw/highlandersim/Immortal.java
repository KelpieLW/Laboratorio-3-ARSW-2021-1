package edu.eci.arsw.highlandersim;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private AtomicInteger health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean paused = true;

    private AtomicBoolean running=new AtomicBoolean(true);



    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = new AtomicInteger(health);
        this.defaultDamageValue=defaultDamageValue;

    }

    public void run() {

        while (running.get()) {
            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            this.fight(im);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (!getPaused()){
                pauseImmortal();
            }




        }
        System.out.println(immortalsPopulation.size());

    }

    public void fight(Immortal i2) {

        if (i2.getHealth().get() > 0) {
            synchronized (immortalsPopulation) {
                i2.changeHealth(new AtomicInteger(i2.getHealth().get() - defaultDamageValue));
                this.health.addAndGet(defaultDamageValue);
            }
            updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
        } else {
            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
            setRunning(new AtomicBoolean(false));
            this.stop();

        }

    }

    public void setRunning(AtomicBoolean runningState){
        running=runningState;
    }


    public synchronized void changeHealth(AtomicInteger v) {
        health = v;
    }

    public synchronized AtomicInteger getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public void setPaused(boolean pausedState){
        paused= pausedState;
    }

    public boolean getPaused(){
        return paused;
    }


    public void pauseImmortal() {
        synchronized (this) {

            try {
                //immortalsPopulation.wait();
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    public  void resumeImmortal(){
        synchronized (this) {
            setPaused(true);

            if (getPaused()){
                this.notifyAll();
            }
        }
    }





}
