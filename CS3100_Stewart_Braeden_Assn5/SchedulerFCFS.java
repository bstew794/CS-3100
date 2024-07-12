import java.util.LinkedList;
import java.util.Queue;

public class SchedulerFCFS extends SchedulerBase implements Scheduler{
    private Logger myPlatform;
    private Queue<Process> ready = new LinkedList<>();

    public SchedulerFCFS(Logger platform){
        this.myPlatform = platform;
    }
    public void notifyNewProcess(Process p){
        this.ready.add(p);
    }
    public Process update(Process cpu){
        if (cpu != null){
            if (cpu.getBurstTime() == cpu.getElapsedBurst()){
                this.myPlatform.log("Process " + cpu.getName() + " burst complete");
                this.contextSwitches++;

                if (cpu.getTotalTime() == cpu.getElapsedTotal()){
                    this.myPlatform.log("Process " + cpu.getName() + " execution complete");
                }
                else{
                    this.ready.add(cpu);
                }
                return update(null);
            }
            else{
                return cpu;
            }
        }
        else{
            cpu = this.ready.peek();

            if (cpu != null){
                this.myPlatform.log("Scheduled: " + cpu.getName());
                this.contextSwitches++;
                this.ready.remove();
            }
            return cpu;
        }
    }
}