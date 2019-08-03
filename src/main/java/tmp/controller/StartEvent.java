package tmp.controller;

public class StartEvent {
    private int chunkId;

    public StartEvent(int chunkId) {
        this.chunkId = chunkId;
    }

    public int getChunkId() {
        return chunkId;
    }
}
