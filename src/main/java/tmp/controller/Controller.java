package tmp.controller;


import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import tmp.player.FinishedEvent;
import tmp.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmp.processor.AudioProcessor;
import tmp.processor.FinishedProcessEvent;

public class Controller {

    private int chunkToPlay = 0;
    private final int CHANNELS = 2;


    private List<Subscription> playerSubscriptions = new ArrayList<>();
    private List<Subscription> processSubscriptions = new ArrayList<>();
    private int returned;
    private int processed;
    private int chunkToProcess;
    private boolean playersRunning;


    private static final Controller INSTANCE = new Controller();
    private final Subject<Object, Object> mBusSubject = new SerializedSubject<>(PublishSubject.create());

    private Map<Integer, Map<Integer, Integer>> processedData = new HashMap<>();


    public void subscribeToPlayer(Player player) {
        playerSubscriptions.add(player.register(FinishedEvent.class, event -> {
            retrieved();
        }));
    }

    public void subscribeToProcessor(AudioProcessor processor) {
        processSubscriptions.add(processor.register(FinishedProcessEvent.class, event -> {
//            System.out.println("FINISHED PROCCES EVENT CAME TO CONTROLLER");
            saveProcessedResult(event);
            try {
                renewProcess();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    public Controller() {
        this.playersRunning = false;
    }

    public static Controller getInstance() {return INSTANCE; };

    public <T> Subscription register(final Class<T> eventClass, Action1<T> onNext) {
        return mBusSubject
                .filter(event -> event.getClass().equals(eventClass))
                .map(obj -> (T) obj)
                .subscribe(onNext);
    }

    private void retrieved() {
//        System.out.println("RETRIEVEEEEEEEEEEEEED " +  chunkToPlay);
        synchronized (this) {
            this.returned++;
        }
        if (returned == CHANNELS) {
            returned = 0;
            playersRunning = false;
            ifPlayerIsNotRunningStartNextChunk();
        }
    }

    private void renewProcess() throws InterruptedException {
        this.processed++;
        System.out.println("------------------PROCESSED CHANNELS: " + processed + "---------------------");
        System.out.println(processed);
        System.out.println(CHANNELS);
        if(processed == CHANNELS) {
            chunkToProcess++;
            processed = 0;

//            post(new StartProcessEvent(chunkToProcess));
//            Thread.sleep(10000);
            System.out.println("NEW START PROCESS EVENT THROWN " + chunkToProcess);

            this.getInstance().
                    post(new StartProcessEvent(chunkToProcess));

        }
    }

    private void saveProcessedResult(FinishedProcessEvent event) {
        if (!processedData.keySet().contains(event.getChunkId()))
            processedData.put(event.getChunkId(), new HashMap<>());
        processedData.get(event.getChunkId()).put(event.getProcessorId(), event.getResult());
//        System.out.println("---------PROCESSED RESULT----------");
//        System.out.println(event.getChunkId() + " " + event.getProcessorId() + " " + event.getResult());
//        System.out.println("MAP SIZE" + processedData.size());
//        System.out.println("MAP[0] size " + processedData.get(0).size());
    }

    private void ifPlayerIsNotRunningStartNextChunk() {
        if (!this.playersRunning) {
            if (chunkIsProcessed(chunkToPlay)) {
                playersRunning = true;
                post(new StartEvent(chunkToPlay++));
            }
        }
    }

    private boolean chunkIsProcessed(int chunkId) {
        return processedData.get(chunkId).size() == CHANNELS;
    }


    public void post(Object event) {
        mBusSubject.onNext(event);
    }
}
