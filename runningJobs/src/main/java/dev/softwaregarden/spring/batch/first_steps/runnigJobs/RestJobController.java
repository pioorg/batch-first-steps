/*
 *
 *  Copyright (C) 2021 Piotr Przybył
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package dev.softwaregarden.spring.batch.first_steps.runnigJobs;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class RestJobController {

    private final ApplicationContext context;
    private final JobLauncher launcher;
    private final JobExplorer explorer;
    private final JobOperator operator;
    private final SimpleJobLauncher asyncLauncher;


    public RestJobController(ApplicationContext context,
                             JobLauncher launcher,
                             JobExplorer explorer,
                             JobOperator operator,
                             JobRepository repository) throws Exception {
        this.context = context;
        this.launcher = launcher;
        this.explorer = explorer;
        this.operator = operator;
        this.asyncLauncher = createAsyncLauncher(repository);
    }

    @PostMapping(path = "/run")
    public ExitStatus runJob(@RequestBody LaunchRequest request) throws Exception {
        Job job = context.getBean(request.getName(), Job.class);


        var paramsBuilder = new JobParametersBuilder(explorer)
            .addJobParameters(new JobParameters(request.getParameters()));
        if (job.getJobParametersIncrementer() != null) {
            paramsBuilder.getNextJobParameters(job);
        }
        JobParameters nextParams = paramsBuilder.toJobParameters();

        JobLauncher launcher = request.isAsync() ? asyncLauncher : this.launcher;
        JobExecution run = launcher.run(job, nextParams);
        return run.getExitStatus();
    }

    @GetMapping(path = "/jobs")
    public String[] getJobs() {
        return context.getBeanNamesForType(Job.class);
    }

    @GetMapping(path = "/jobs/{name}/last-instance")
    public JobInstance getLastInstance(@PathVariable("name") String jobName) {
        return explorer.getLastJobInstance(jobName);
    }

    @GetMapping(path = "/jobs/{name}/instances")
    public List<JobInstance> getInstances(@PathVariable("name") String jobName) throws NoSuchJobException {
        var count = explorer.getJobInstanceCount(jobName);
        return explorer.getJobInstances(jobName, 0, count);
    }

    @GetMapping(path = "/instances/{instanceId}")
    public JobInstance getInstance(@PathVariable("instanceId") Long instanceId) {
        return explorer.getJobInstance(instanceId);
    }

    @GetMapping(path = "/instances/{instanceId}/executions")
    public List<Map<String, ?>> getExecutionsDorInstance(@PathVariable("instanceId") Long instanceId) {
        return explorer.getJobExecutions(explorer.getJobInstance(instanceId))
            .stream().map(this::getExecutionDetails).collect(Collectors.toList());
    }

    @GetMapping (path = "/executions/{executionId}")
    public Map<String, ?> getExecution(@PathVariable("executionId") Long executionId) {
        return getExecutionDetails(explorer.getJobExecution(executionId));
    }

    @DeleteMapping(path = "/executions/{executionId}")
    public boolean stopExecution(@PathVariable("executionId") Long executionId) throws NoSuchJobExecutionException, JobExecutionNotRunningException {
        return operator.stop(executionId);
    }

    private SimpleJobLauncher createAsyncLauncher(JobRepository repository) throws Exception {
        final SimpleJobLauncher asyncLauncher;
        asyncLauncher = new SimpleJobLauncher();
        asyncLauncher.setJobRepository(repository);
        asyncLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        asyncLauncher.afterPropertiesSet();
        return asyncLauncher;
    }

    private Map<String, ?> getExecutionDetails(JobExecution e) {
        var steps = e.getStepExecutions()
            .stream().map(se -> Map.of(
                "name", se.getStepName(),
                "readCount", se.getReadCount(),
                "writeCount", se.getReadCount()))
            .collect(Collectors.toList());
        return Map.of(
            "id", e.getId(),
            "instanceId", e.getJobInstance().getInstanceId(),
            "jobName", e.getJobInstance().getJobName(),
            "params", e.getJobParameters(),
            "status", e.getStatus(),
            "started", e.getStartTime(),
            "ended", Optional.ofNullable(e.getEndTime()).map(Date::toString).orElse(""),
            "lastUpdated", e.getLastUpdated(),
            "steps", steps);
    }

}


