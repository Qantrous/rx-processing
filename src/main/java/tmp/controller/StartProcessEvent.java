package tmp.controller;

public class StartProcessEvent {


    private int chunkToProcess;

    public StartProcessEvent(int chunkToProcess) {
        System.out.println("START PROCESS EVENT " + chunkToProcess);
        this.chunkToProcess = chunkToProcess;
    }

    public int getChunkToProcess() {
        return chunkToProcess;
    }
}
