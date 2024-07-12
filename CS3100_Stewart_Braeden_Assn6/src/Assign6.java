import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Assign6 {
    static int MAX_PAGE_REFERENCE = 250;
    static int MIN_PAGE_REFERENCE = 1;

    public static void main(String[] args) throws InterruptedException {
        double startTime = System.nanoTime();
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        int lowsFIFO = 0;
        int lowsLRU = 0;
        int lowsMRU = 0;

        AnomalyObject anomFIFO = new AnomalyObject("", 0, 0);
        AnomalyObject anomLRU = new AnomalyObject("", 0, 0);
        AnomalyObject anomMRU = new AnomalyObject("", 0, 0);

        for (int i = 0; i < 1000; i++){
            int maxMemoryFrames = 100;
            int[] faultsFIFO = new int[maxMemoryFrames];
            int[] faultsLRU = new int[maxMemoryFrames];
            int[] faultsMRU = new int[maxMemoryFrames];
            int[] sequence = fillSeq(MAX_PAGE_REFERENCE);

            CountDownLatch latch = new CountDownLatch(300);

            for (int j = 1; j <= maxMemoryFrames; j++){

                TaskFIFO taskFIFO = new TaskFIFO(sequence, j, MAX_PAGE_REFERENCE, faultsFIFO, latch);
                TaskLRU taskLRU = new TaskLRU(sequence, j, MAX_PAGE_REFERENCE, faultsLRU, latch);
                TaskMRU taskMRU = new TaskMRU(sequence, j, MAX_PAGE_REFERENCE, faultsMRU, latch);

                pool.execute(taskFIFO);
                pool.execute(taskLRU);
                pool.execute(taskMRU);
            }
            latch.await();

            for (int j = 1; j <= maxMemoryFrames; j++){
                if (faultsLRU[j - 1] < faultsFIFO[j - 1]){

                    if (faultsMRU[j - 1] < faultsLRU[j - 1]){
                        lowsMRU++;
                    }
                    else if (faultsLRU[j - 1] == faultsMRU[j - 1]){
                        lowsLRU++;
                        lowsMRU++;
                    }
                    else{
                        lowsLRU++;
                    }
                }
                else if (faultsMRU[j - 1] < faultsFIFO[j - 1]){

                    if (faultsLRU[j - 1] < faultsMRU[j - 1]){
                        lowsLRU++;
                    }
                    else if (faultsLRU[j - 1] == faultsMRU[j - 1]){
                        lowsLRU++;
                        lowsMRU++;
                    }
                    else{
                        lowsMRU++;
                    }
                }
                else if (faultsLRU[j - 1] == faultsFIFO[j - 1]){

                    if (faultsMRU[j - 1] == faultsFIFO[j - 1]){
                        lowsLRU++;
                        lowsMRU++;
                        lowsFIFO++;
                    }
                    else{
                        lowsLRU++;
                        lowsFIFO++;
                    }
                }
                else if (faultsMRU[j - 1] == faultsFIFO[j - 1]){

                    if (faultsLRU[j - 1] == faultsFIFO[j - 1]){
                        lowsLRU++;
                        lowsMRU++;
                        lowsFIFO++;
                    }
                    else{
                        lowsMRU++;
                        lowsFIFO++;
                    }
                }
                else{
                    lowsFIFO++;
                }
                anomFIFO.checkBelady(faultsFIFO);
                anomLRU.checkBelady(faultsLRU);
                anomMRU.checkBelady(faultsMRU);
            }
        }
        pool.shutdown();
        double endTime = System.nanoTime();
        double deltaTime = (endTime - startTime)/1000000;

        System.out.println("Simulation took: " + deltaTime + " ms");

        System.out.println("FIFO min PF: " + lowsFIFO);
        System.out.println("LRU min PF: " + lowsLRU);
        System.out.println("MRU min PF: " + lowsMRU);

        System.out.println("Belady's Anomaly Report for FIFO");
        System.out.print(anomFIFO.anomalySTR);
        System.out.println("Anomaly detected " + anomFIFO.count + " times with a max difference of " + anomFIFO.maxDiff);

        System.out.println("Belady's Anomaly Report for LRU");
        System.out.print(anomLRU.anomalySTR);
        System.out.println("Anomaly detected " + anomLRU.count + " times with a max difference of " + anomLRU.maxDiff);

        System.out.println("Belady's Anomaly Report for MRU");
        System.out.print(anomMRU.anomalySTR);
        System.out.println("Anomaly detected " + anomMRU.count + " times with a max difference of " + anomMRU.maxDiff);

        System.exit(0);
    }

    public static int[] fillSeq(int maxPageReference){
        Random random = new Random();
        int[] sequence = new int[100];

        for (int i = 0; i < sequence.length; i++){
            sequence[i] = random.nextInt((maxPageReference - MIN_PAGE_REFERENCE) + 1) + MIN_PAGE_REFERENCE;
        }
        return sequence;
    }
}
class AnomalyObject {
    String anomalySTR;
    int maxDiff;
    int count;

    public AnomalyObject(String anomalySTR, int maxDiff, int count){
        this.anomalySTR = anomalySTR;
        this.maxDiff = maxDiff;
        this.count = count;
    }

    public void checkBelady(int[] faults){
        String report = "";

        for (int i = 1; i < faults.length; i++){
            int curr = faults[i];
            int prev = faults[i - 1];

            if (curr > prev){
                count++;
                int delta = curr - prev;
                report += "detected - Previous " + prev + " : Current " + curr + " (" + delta + ")\n";

                if (delta > maxDiff){
                    maxDiff = delta;
                }
            }
        }
        if (!report.equals("")){
            anomalySTR += report;
        }
    }
}
