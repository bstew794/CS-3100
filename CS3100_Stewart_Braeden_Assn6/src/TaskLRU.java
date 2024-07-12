import java.util.*;
import java.util.concurrent.CountDownLatch;

public class TaskLRU implements Runnable{
    private int[] sequence;
    private int maxMemoryFrames;
    private int maxPageReference;
    private int[] pageFaults;
    private int page_Faults;
    private CountDownLatch latch;

    public TaskLRU(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults, CountDownLatch latch){
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
            HashMap<Integer, Integer> pagesMap= new HashMap<>();

            for (int i = 0; i < sequence.length; i++){
                int page = sequence[i];

                if (frames.size() < maxMemoryFrames){

                    if (!frames.contains(page)){
                        frames.add(page);

                        page_Faults++;
                    }
                    pagesMap.put(page, i);
                }
                else{

                    if (!frames.contains(page)){
                        int oldest = Integer.MAX_VALUE;
                        int num = Integer.MIN_VALUE;
                        Iterator<Integer> integerIterator = frames.iterator();

                        while (integerIterator.hasNext()){
                            int temp = integerIterator.next();

                            if (pagesMap.get(temp) < oldest){
                                oldest = pagesMap.get(temp);
                                num = temp;
                            }
                        }
                        frames.remove(num);
                        pagesMap.remove(num);
                        frames.add(page);

                        page_Faults++;
                    }
                    pagesMap.put(page, i);
                }
            }
            pageFaults[maxMemoryFrames - 1] = page_Faults;
            latch.countDown();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
