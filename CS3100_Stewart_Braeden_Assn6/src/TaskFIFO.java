import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public class TaskFIFO implements Runnable{
    private int[] sequence;
    private int maxMemoryFrames;
    private int maxPageReference;
    private int[] pageFaults;
    private int page_Faults;
    private CountDownLatch latch;

    public TaskFIFO(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults, CountDownLatch latch){
        this.sequence = sequence;
        this.maxMemoryFrames = maxMemoryFrames;
        this.maxPageReference = maxPageReference;
        this.pageFaults = pageFaults;
        this.latch = latch;
    }

    @Override
    public void run() {
        try{
            page_Faults = 0;
            HashSet<Integer> frames = new HashSet<>(maxMemoryFrames);
            Queue<Integer> pagesQ = new LinkedList<>();

            for (int i = 0; i < sequence.length; i++){
                int page = sequence[i];

                if (frames.size() < maxMemoryFrames){

                    if (!frames.contains(page)){
                        frames.add(page);

                        page_Faults++;

                        pagesQ.add(page);
                    }
                }
                else{

                    if (!frames.contains(sequence[i])){
                        int oldPage = pagesQ.peek();

                        pagesQ.poll();

                        frames.remove(oldPage);
                        frames.add(page);

                        pagesQ.add(page);

                        page_Faults++;
                    }
                }
            }
            pageFaults[maxMemoryFrames - 1] = page_Faults;
            latch.countDown();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
