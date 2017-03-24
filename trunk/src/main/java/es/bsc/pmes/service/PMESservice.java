package es.bsc.pmes.service;

import es.bsc.pmes.managers.InfrastructureManager;
import es.bsc.pmes.managers.JobManager;
import es.bsc.pmes.types.*;

import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Created by scorella on 8/5/16.
 */

public class PMESservice {
    private JobManager jm;
    private InfrastructureManager im;

    private static final Logger logger = LogManager.getLogger(PMESservice.class);

    public PMESservice() {
        this.startService();
    }

    public void startService(){
        logger.trace("Starting PMESService");
        this.im = InfrastructureManager.getInfrastructureManager();
        this.jm = JobManager.getJobManager();

    }

    public void endService(){
        // TODO: endService
    }

    public ArrayList<String> createActivity(ArrayList<JobDefinition> jobDefinitions) {
        ArrayList<String> jobIds = new ArrayList<>(jobDefinitions.size());
        for (JobDefinition jobDef:jobDefinitions) {
            Job newJob;
            logger.trace("Job Type: "+jobDef.getApp().getType());
            if (jobDef.getApp().getType().equals("COMPSs")){
                newJob = new COMPSsJob();
            } else {
                newJob = new SingleJob();
            }
            //** Create new job
            newJob.setUser(jobDef.getUser());
            newJob.setJobDef(jobDef);
            newJob.setDataIn(jobDef.getInputPaths());
            newJob.setDataOut(jobDef.getOutputPaths());
            logger.trace("JobDef: "+jobDef.getInputPaths().toString()+" "+jobDef.getOutputPaths().toString());
            logger.trace("newJob: "+newJob.getDataIn().toString()+" "+newJob.getDataOut().toString());
            //DEBUG
            logger.trace("user: "+newJob.getUser().getCredentials().toString());
            jobIds.add(newJob.getId());

            String type = newJob instanceof COMPSsJob ? "COMPSs" : "Single";
            logger.trace("New "+type+" Job created with id "+newJob.getId());
            this.jm.enqueueJob(newJob);
        }
        return jobIds;
    }

    public ArrayList<String> terminateActivity(ArrayList<String> jobIds) {
        ArrayList<String> messages = new ArrayList<>(jobIds.size());
        for (String id:jobIds) {
            String message = "";
            Job job = this.jm.getJobs().get(id);
            if (job != null) {
                job.setTerminate(Boolean.TRUE);

                if (job.getStatus().equals("FAILED")) {
                    message += "Job with id "
                            + id
                            + " cannot be cancelled, the job has been finished in Failed state.";
                } else {
                    if (job.getStatus().equals("FINISHED")) {
                        message += "Job with id "
                                + id
                                + " cannot be cancelled, the job has been finished.";
                    } else {
                        this.jm.deleteJob(job);
                        message += "Job with id "
                                + id
                                + " will be cancelled.";
                    }
                }
            }
            else {
                message += "Job not found";
            }
            messages.add(message);
        }
        return messages;
    }

    public ArrayList<JobStatus> getActivityStatus(ArrayList<String> jobids){
        ArrayList<JobStatus> status = new ArrayList<>(jobids.size());
        for (String id:jobids) {
            logger.trace("Asking status for job: "+id);
            Job job = this.jm.getJobs().get(id);
            if (job != null) {
                logger.trace("Job Found");
                logger.trace(job.getStatus());
                status.add(JobStatus.valueOf(job.getStatus()));
            }
            else {
                logger.trace("Job not found");
                status.add(JobStatus.valueOf("ALL"));
            }
        }
        logger.trace("Sending list status "+status.toString());
        return status;
    }

    public ArrayList<JobReport> getActivityReport(ArrayList<String> jobids){
        ArrayList<JobReport> reports = new ArrayList<>(jobids.size());
        for (String id:jobids) {
            logger.trace("Asking Activity for job: "+id);
            Job job = this.jm.getJobs().get(id);
            if (job != null) {
                logger.trace("Job Found");
                JobReport jr = job.getReport();
                reports.add(jr);
            }
            else {
                logger.trace("Job not found");
                reports.add(new JobReport());
            }
        }
        return reports;
    }

    public SystemStatus getSystemStatus(){
        logger.trace(im.getSystemStatus().print());
        return im.getSystemStatus();
    }

    /** GETTERS AND SETTERS*/
    public JobManager getJm() { return jm; }

    public void setJm(JobManager jm) { this.jm = jm; }

    public InfrastructureManager getIm() { return im; }

    public void setIm(InfrastructureManager im) { this.im = im; }

}
