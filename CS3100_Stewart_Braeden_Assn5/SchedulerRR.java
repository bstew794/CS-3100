import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class SchedulerRR extends SchedulerBase implements Scheduler {
    private Logger myPlatform;
    private int timeQuantum;
    private Queue<Process> ready = new LinkedList<>();
    private int iterator = 0;

    public SchedulerRR(Logger platform, int num){
        this.myPlatform = platform;
        this.timeQuantum = num;
    }
    public void notifyNewProcess(Process p){
        this.ready.add(p);
    }
    public Process update(Process cpu){
        if (cpu != null){
            if (cpu.getTotalTime() == cpu.getElapsedTotal()) {
                this.myPlatform.log("Process " + cpu.getName() + " execution complete");
                this.contextSwitches++;

                return update(null);
            }
            else{
                if (cpu.getTotalTime() <= this.timeQuantum) {
                    return cpu;
                }
                else{
                    this.iterator++;

                    if (iterator >= this.timeQuantum){
                        this.myPlatform.log("Time quantum complete for process " + cpu.getName());
                        this.contextSwitches++;
                        this.ready.add(cpu);
                        this.iterator = 0;

                        return update(null);
                    }
                    return cpu;
                }
            }
        }
        else{
            if (!this.ready.isEmpty()){
                cpu = this.ready.peek();
                this.myPlatform.log("Scheduled: " + cpu.getName());
                this.contextSwitches++;
                this.ready.remove();
            }
            return cpu;
        }
    }
}
