package message;

public class FileContentMessage extends Message{
    private byte [] content;
    private long startPosition;
    private boolean last;

    public void setLast(boolean last) {
        this.last = last;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }


    public boolean isLast() {
        return last;
    }


    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }



}
