package vn.noreo.jobhunter.controller;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import vn.noreo.jobhunter.domain.Job;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.domain.response.job.ResCreateJobDTO;
import vn.noreo.jobhunter.domain.response.job.ResUpdateJobDTO;
import vn.noreo.jobhunter.service.JobService;
import vn.noreo.jobhunter.util.annotation.ApiMessage;
import vn.noreo.jobhunter.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Job", description = "Job management APIs")
@RestController
@RequestMapping("/api/v1")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/jobs")
    @ApiMessage("Create new job")
    public ResponseEntity<ResCreateJobDTO> createNewJob(@Valid @RequestBody Job newJob) {
        return ResponseEntity.ok().body(this.jobService.handleCreateJob(newJob));
    }

    @PutMapping("/jobs")
    @ApiMessage("Update job")
    public ResponseEntity<ResUpdateJobDTO> updateJob(@Valid @RequestBody Job updatedJob) throws IdInvalidException {
        Optional<Job> currentJobOpt = this.jobService.handleFetchJobById(updatedJob.getId());
        if (!currentJobOpt.isPresent()) {
            throw new IdInvalidException("Job with id " + updatedJob.getId() + " not found");
        }
        return ResponseEntity.ok().body(this.jobService.handleUpdateJob(updatedJob, currentJobOpt.get()));
    }

    @DeleteMapping("/jobs/{id}")
    @ApiMessage("Delete job by id")
    public ResponseEntity<Void> deleteJob(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Job> currentJobOpt = this.jobService.handleFetchJobById(id);
        if (!currentJobOpt.isPresent()) {
            throw new IdInvalidException("Job with id " + id + " not found");
        }
        this.jobService.handleDeleteJob(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/jobs/{id}")
    @ApiMessage("Fetch job by id")
    public ResponseEntity<Job> fetchJobById(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Job> currentJobOpt = this.jobService.handleFetchJobById(id);
        if (!currentJobOpt.isPresent()) {
            throw new IdInvalidException("Job with id " + id + " not found");
        }
        return ResponseEntity.ok().body(currentJobOpt.get());
    }

    @GetMapping("/jobs")
    @ApiMessage("Fetch all jobs")
    public ResponseEntity<ResultPaginationDTO> fetchAllJobs(@Filter Specification<Job> specification,
            Pageable pageable) {
        return ResponseEntity.ok().body(this.jobService.handleFetchAllJobs(specification, pageable));
    }

}
