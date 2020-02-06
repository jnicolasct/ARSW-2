package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;

    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private Boolean continuar;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
        this.continuar = true;
    }

    public void run() {

        while (getHealth()>0) {
            Immortal im;
            synchronized (this) {
                if (!continuar) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Immortal.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            synchronized (immortalsPopulation){
                int myIndex = immortalsPopulation.indexOf(this);

                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                //avoid self-fight
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                im = immortalsPopulation.get(nextFighterIndex);
                
                if (immortalsPopulation.get(nextFighterIndex).getHealth()>0){
                    this.fight(im);
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public synchronized void fight(Immortal i2) {

        if (i2.getHealth() > 0) {
            i2.changeHealth(i2.getHealth() - defaultDamageValue);
            this.health += defaultDamageValue;
            updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
        } else {
            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
        }

    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public void cambieEstado(int valor) {
        if (valor == 1) {
            continuar = false;
        } else {
            continuar = true;
            synchronized(this){
                notifyAll();
            }
        }

    }

}
