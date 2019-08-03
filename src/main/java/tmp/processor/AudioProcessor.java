package tmp.processor;

import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import tmp.controller.*;

import java.util.Random;


public class AudioProcessor extends Thread {

    private Subscription startSubsc;
    private Subscription stopSubsc;
    private Subject<Object, Object> threadSubject = PublishSubject.create();
    private boolean running;
    private int id;
    private int chunkId;
    private int chunkToProcess;
    int rand;


    public AudioProcessor(int id) {
        this.id = id;
    }


    @Override
    public void run() {
//        System.out.println("PROCESSOR WITH ID " + id + " REGISTRATION STARTED");
        startSubsc = Controller.getInstance().register(StartProcessEvent.class, event -> {
            running = true;
            chunkToProcess = event.getChunkToProcess();
            System.out.println("PROCESSOR WITH ID: " + id + " RECIEVED EVENT WITH CHUNK ID:" + chunkToProcess);

            System.out.println("PROCESSOR NOTIFIED " + id);
            synchronized (this) {

                try{

                    Thread.sleep(4000);
                    System.out.println("CUGRUM");
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                notifyAll();
            }

//                System.out.println(Thread.currentThread().getName() + " RESUMED WORKING ************");
            System.out.println("PROCESSOR WITH ID: " + id + " RESUMED WORKING ************");
        });

//        stopSubsc = Controller.getInstance().register(StopEvent.class, stopEvent -> {
//            synchronized (this) {
//                running = false;
//            }
//        });

        while(true) {
            try {
                System.out.println("-*-*-PROCESSOR ID WITH: " + id + " STARTED PROCESSING-*-*-");
                rand = processChunks();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                post(new FinishedProcessEvent(id, rand, chunkId));
//                    System.out.println(Thread.currentThread().getName() + " STARTED WAITING **************");
                synchronized (this) {

                    System.out.println("PROCESSOR WITH ID " + id + " STARTED WAITING **************");
                    System.out.println("ku");
                    wait();
                    System.out.println((Thread.currentThread().getState()) + " 1st");

                    System.out.println((Thread.currentThread().getState()));
                    System.out.println("RRRRRRRRRRRR");

                }

            } catch (InterruptedException er) {
                cleanUp();
                er.printStackTrace();
            }
        }


    }

    private int processChunks() throws InterruptedException {
        System.out.println("PROCESSOR WITH ID: " + id + " STARTED PROCESSING CHUNK: " + chunkToProcess);
        Random random = new Random();
        int rand = random.nextInt(3);
//        Thread.sleep((3+rand) * 3000);
        System.out.println("PROCESSOR WITH ID: " + id + " FINISHED PROCESSING CHUNK: " + chunkToProcess);

        return rand;
    }

    private void cleanUp() {
        startSubsc.unsubscribe();
        stopSubsc.unsubscribe();
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
