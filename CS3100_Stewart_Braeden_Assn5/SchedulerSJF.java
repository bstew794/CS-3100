import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SchedulerSJF extends SchedulerBase implements Scheduler {
    private Logger myPlatform;
    private ArrayList<Process> ready = new ArrayList<>();

    public SchedulerSJF(Logger platform){
        this.myPlatform = platform;
    }
    public void notifyNewProcess(Process p){
        this.ready.add(p);
        Collections.sort(this.ready , Comparator.comparingInt(Process::getTotalTime));
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
            cpu = null;

            if (!this.ready.isEmpty()){
                cpu = this.ready.get(0);
                this.myPlatform.log("Scheduled: " + cpu.getName());
                this.contextSwitches++;
                this.ready.remove(0);
            }
            return cpu;
        }
    }
}
