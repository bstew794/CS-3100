import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SchedulerPriority extends SchedulerBase implements Scheduler{
    private Logger myPlatform;
    private ArrayList<Process> ready = new ArrayList<>();

    public SchedulerPriority(Logger platform){
        this.myPlatform = platform;
    }
    public void notifyNewProcess(Process p){
        this.ready.add(p);

        Collections.sort(this.ready, Comparator.comparingInt(Process::getPriority));
    }
    public Process update(Process cpu){
        if (cpu != null){
            if (cpu.getBurstTime() == cpu.getElapsedBurst()){
                this.myPlatform.log("Process " + cpu.getName() + " burst complete");
                this.contextSwitches++;
                this.ready.remove(cpu);

                if (cpu.getTotalTime() == cpu.getElapsedTotal()){
                    this.myPlatform.log("Process " + cpu.getName() + " execution complete");
                }
                else{
                    this.ready.add(cpu);
                }
                return update(null);
            }
            else{
                if (!this.ready.isEmpty()){
                    Process possCPU = this.ready.get(0);

                    if (possCPU != cpu){
                        this.myPlatform.log("Preemptively removed: " + cpu.getName());
                        this.contextSwitches++;

                        return update(null);
                    }
                }
                return cpu;
            }
        }
        else{
            if (!this.ready.isEmpty()){
                cpu = this.ready.get(0);
                this.myPlatform.log("Scheduled: " + cpu.getName());
                this.contextSwitches++;
            }
            return cpu;
        }
    }
}
