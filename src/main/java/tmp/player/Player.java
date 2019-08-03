package tmp.player;

import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import tmp.controller.Controller;
import tmp.controller.StartEvent;
import tmp.controller.StopEvent;


public class Player extends Thread {

    private Subscription startSubscr;
    private Subscription stopSubscr;
    private Subject<Object, Object> threadSubject = PublishSubject.create();
    private boolean running;
    private int id;
    private int chunkId;

    public Player(int id, int chunkId) {
        this.id = id;
        this.chunkId = chunkId;
    }


    @Override
    public void run() {
        startSubscr = Controller.getInstance().register(StartEvent.class, event -> {
            System.out.println("START EVENT");
            running = true;
            chunkId = event.getChunkId();
            synchronized (this) {
                notify();
            }
        });

        stopSubscr = Controller.getInstance().register(StopEvent.class, stopEvent -> {
            running = false;
        });

        while (true) {
            if (Thread.interrupted())
                cleanUp();

            if (!running) {
//                System.out.println("STOPPING RUNNING");
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    cleanUp();
                    return;
                }
//                System.out.println("Resuming work");
            }

            try {
                playAudio();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void playAudio() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " PLAYING CHUNK ID " + chunkId);
        Thread.sleep(3000);
        post(new FinishedEvent());
    }

    private void cleanUp() {
        startSubscr.unsubscribe();
        stopSubscr.unsubscribe();
    }

    public boolean isRunning() {
        return this.running;
    }



    public <T> Subscription register(final Class<T> eventClass, Action1<T> onNext) {
        return threadSubject
                .filter(event -> event.getClass().equals(eventClass))
                .map(obj -> (T) obj)
                .subscribe(onNext);
    }

    public void post(Object event) {
        threadSubject.onNext(event);
    }
}
