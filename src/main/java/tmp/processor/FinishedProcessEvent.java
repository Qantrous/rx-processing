package tmp.processor;

public class FinishedProcessEvent {

    private int processorId;
    private int result;
    private int chunkId;

    public FinishedProcessEvent(int processorId, int result, int chunkId) {
        this.processorId = processorId;
        this.result = result;
        this.chunkId = chunkId;
    }

    public int getProcessorId() {
        return processorId;
    }

    public int getResult() {
        return result;
    }

    public int getChunkId() {
        return chunkId;
    }
}
