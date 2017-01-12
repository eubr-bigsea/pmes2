package es.bsc.pmes.managers.execution;

import es.bsc.conn.exceptions.ConnectorException;
import es.bsc.conn.types.HardwareDescription;
import es.bsc.conn.types.SoftwareDescription;
import es.bsc.pmes.managers.InfrastructureManager;
import es.bsc.pmes.types.COMPSsJob;
import es.bsc.pmes.types.Job;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by scorella on 8/23/16.
 */
public class COMPSsExecutionThread extends Thread implements ExecutionThread{
    //private Job job;
    private COMPSsJob job;
    private InfrastructureManager im = InfrastructureManager.getInfrastructureManager();
    private static final Logger logger = LogManager.getLogger(COMPSsExecutionThread.class);
    private Process process = null;

    public COMPSsExecutionThread(COMPSsJob job){
        this.job = job;
    }

    public void run() {
        executeJob();
    }

    public void cancel() throws Exception {
        if (this.process != null){
            this.process.destroy();
            logger.trace("Job cancelled: Execution stopped");
            this.job.setStatus("CANCELLED");
        }
        //reset interrupted flag
        //Thread.currentThread().interrupt();
    }

    public void executeJob(){
        // Create Resource
        if (job.getTerminate()){
            job.setStatus("CANCELLED");
            return;
        }
        String Id = createResource();
        logger.trace("Resource created with Id: "+ Id);


        //StageIn
        if (job.getTerminate()){
            //Destroy VM if the user cancel the job.
            logger.trace("Job cancelled: Destroying resource with Id: "+Id);
            destroyResource(Id);
            job.setStatus("CANCELLED");
            return;
        }
        logger.trace("Stage in");
        stageIn();

        //Configure execution
        String resourceAddress = job.getResource(0).getIp(); //get master IP
        String user = job.getUser().getUsername();
        String address = user+"@"+resourceAddress;
        String source = job.getJobDef().getApp().getSource();
        String target = job.getJobDef().getApp().getTarget();
        HashMap<String, String> args = job.getJobDef().getApp().getArgs();

        // Create command to execute
        ArrayList<String> cmd = new ArrayList<>();
        /*cmd.add("ssh");
        cmd.add(address);
        cmd.add(target+"/./"+source);
        for (Object o : args.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            cmd.add((String) pair.getValue());
        }*/
        //TODO if it's a COMPSs job...
        // BEGIN COMPSs
        cmd.add("ssh");
        cmd.add(address);
        cmd.add("runcompss");
        for (Object o: job.getJobDef().getCompss_flags().entrySet()){
            Map.Entry pair = (Map.Entry) o;
            cmd.add((String) pair.getValue());
        }
        if (job.getJobDef().getApp().getSource().endsWith(".py")){
            cmd.add("--lang=python");
            cmd.add("--pythonpath="+target);
        } else {
            cmd.add("--classpath="+target);
        }
        cmd.add(target+"/"+source);
        for (Object o : args.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            cmd.add((String) pair.getValue());
        }
        //END COMPSs

        String[] command = new String[cmd.size()];
        job.setCmd(cmd.toArray(command));
        logger.trace(Arrays.toString(command));

        //Wait until vm is ready at login stage
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Run job
        if (job.getTerminate()){
            //Destroy VM if the user cancel the job.
            logger.trace("Job cancelled: Destroying resource with Id: "+Id);
            destroyResource(Id);
            job.setStatus("CANCELLED");
            return;
        }

        logger.trace("runnning");
        Integer exitValue = executeCommand(command);

        logger.trace("exit code"+ exitValue);
        if (exitValue > 0){
            if (exitValue == 143){
                job.setStatus("CANCELLED");
                // TODO: Si cancelan un job cuando ya se ha ejecutado, traemos datos? salida de compss
            } else {
                job.setStatus("FAILED");
            }
        } else {
            //StageOut
            logger.trace("Stage out");
            stageOut();
            job.setStatus("FINISHED");
        }

        //Destroy Resource
        logger.trace("Destroy resource");
        destroyResource(Id);
    }

    public String createResource(){
        // Create Resource
        // ** configure Resource Petition
        logger.trace("Configuring Job " + job.getId());
        // Configuring Hardware
        HardwareDescription hd = new HardwareDescription();
        hd.setMemorySize(job.getJobDef().getMemory());
        hd.setTotalComputingUnits(job.getJobDef().getCores()*job.getJobDef().getNumNodes());
        hd.setImageType(job.getJobDef().getImg().getImageType());
        hd.setImageName(job.getJobDef().getImg().getImageName());

        // Configure software
        SoftwareDescription sd = new SoftwareDescription();


        // Configure properties
        HashMap<String, String> prop = this.im.configureResource(job.getJobDef());

        //** create resource
        logger.trace("Creating new Resource");
        String Id = this.im.createResource(hd, sd, prop);
        logger.trace("Resource Id " + Id);
        job.addResource(this.im.getActiveResources().get(Id));
        return Id;
    }

    public void stageIn(){
        logger.trace("Staging in");
        //TODO: stageIN
    }

    public void stageOut(){
        logger.trace("Staging out");
        //TODO: stageOUT
    }

    public void destroyResource(String Id){
        logger.trace("Deleting Resource");
        this.im.destroyResource(Id);
    }

    public Integer executeCommand(String[] cmd){
        Runtime runtime = Runtime.getRuntime();
        Integer times = 3;
        Integer exitValue = 1;
        Integer i = 0;
        while (i < times && exitValue != 0) {
            logger.trace("Round "+String.valueOf(i));

            if (i > 0){
                //Wait until vm will be ready
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                this.process = runtime.exec(cmd);

                BufferedReader in = new BufferedReader(new
                        InputStreamReader(this.process.getInputStream()));

                BufferedReader err = new BufferedReader(new
                        InputStreamReader(this.process.getErrorStream()));

                // Output log
                String outStr = "";
                String line = null;
                while ((line = in.readLine()) != null) {
                    outStr += line;
                }
                in.close();
                logger.trace("out: " + outStr);

                //Error log
                line = null;
                String errStr = "";
                while ((line = err.readLine()) != null) {
                    errStr += line;
                }
                err.close();
                logger.trace("err: " + errStr);

                this.process.waitFor();
                exitValue = this.process.exitValue();
                logger.trace("Exit Value "+String.valueOf(exitValue));
                //return exitValue;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i += 1;
        }
        return exitValue;
    }
}
