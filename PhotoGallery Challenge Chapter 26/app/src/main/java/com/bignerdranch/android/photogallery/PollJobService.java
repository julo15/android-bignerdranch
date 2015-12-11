package com.bignerdranch.android.photogallery;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by julianlo on 12/10/15.
 */
public class PollJobService extends JobService {

    private static final int JOB_ID = 1;

    public static void setServiceAlarm(Context context, boolean isOn) {
        // Schedule with JobScheduler
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (isOn) {
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(PollService.POLL_INTERVAL)
                    .setPersisted(true)
                    .build();
            scheduler.schedule(jobInfo);
        } else {
            scheduler.cancel(JOB_ID);
        }
    }

    public static boolean isServiceAlarmOn(Context context) {
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                return true;
            }
        }
        return false;
    }

    private PollTask mPollTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        mPollTask = new PollTask();
        mPollTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mPollTask != null) {
            mPollTask.cancel(true);
        }
        return true;
    }

    private class PollTask extends AsyncTask<JobParameters,Void,Void> {

        @Override
        protected Void doInBackground(JobParameters... params) {
            JobParameters jobParams = params[0];
            PollService.doWork(PollJobService.this);
            jobFinished(jobParams, false);
            return null;
        }
    }
}
