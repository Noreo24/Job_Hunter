package vn.noreo.jobhunter.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;
import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import vn.noreo.jobhunter.domain.Company;
import vn.noreo.jobhunter.domain.Job;
import vn.noreo.jobhunter.domain.Resume;
import vn.noreo.jobhunter.domain.User;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.domain.response.resume.ResCreateResumeDTO;
import vn.noreo.jobhunter.domain.response.resume.ResFetchResumeDTO;
import vn.noreo.jobhunter.domain.response.resume.ResUpdateResumeDTO;
import vn.noreo.jobhunter.service.ResumeService;
import vn.noreo.jobhunter.service.UserService;
import vn.noreo.jobhunter.util.SecurityUtil;
import vn.noreo.jobhunter.util.annotation.ApiMessage;
import vn.noreo.jobhunter.util.error.IdInvalidException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Resume", description = "Resume management APIs")
@RestController
@RequestMapping("/api/v1")
public class ResumeController {

    private final ResumeService resumeService;
    private final UserService userService;
    private final FilterSpecificationConverter filterSpecificationConverter;
    private final FilterBuilder filterBuilder;

    public ResumeController(ResumeService resumeService, UserService userService,
            FilterSpecificationConverter filterSpecificationConverter, FilterBuilder filterBuilder) {
        this.resumeService = resumeService;
        this.userService = userService;
        this.filterSpecificationConverter = filterSpecificationConverter;
        this.filterBuilder = filterBuilder;
    }

    @PostMapping("/resumes")
    @ApiMessage("Create new resume")
    public ResponseEntity<ResCreateResumeDTO> createNewResume(@Valid @RequestBody Resume newResume)
            throws IdInvalidException {
        // Check if the resume already exists for the user and job
        boolean isIdExists = this.resumeService.checkResumeExistsByUserAndJob(newResume);
        if (!isIdExists) {
            throw new IdInvalidException("User or Job not found for the provided Resume");
        }

        // Create the new resume
        return ResponseEntity.status(HttpStatus.CREATED).body(this.resumeService.handleCreateResume(newResume));
    }

    @PutMapping("/resumes")
    @ApiMessage("Update resume")
    public ResponseEntity<ResUpdateResumeDTO> updateResume(@RequestBody Resume updatedResume)
            throws IdInvalidException {
        Optional<Resume> currentResumeOpt = this.resumeService.handleFetchResumeById(updatedResume.getId());
        if (currentResumeOpt.isEmpty()) {
            throw new IdInvalidException("Resume with id " + updatedResume.getId() + " not found");
        }
        Resume currentResume = currentResumeOpt.get();
        currentResume.setStatus(updatedResume.getStatus());

        return ResponseEntity.ok().body(this.resumeService.handleUpdateResume(currentResume));
    }

    @DeleteMapping("/resumes/{id}")
    @ApiMessage("Delete resume by id")
    public ResponseEntity<Void> deleteResume(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Resume> currentResumeOpt = this.resumeService.handleFetchResumeById(id);
        if (!currentResumeOpt.isPresent()) {
            throw new IdInvalidException("Resume with id " + id + " not found");
        }
        this.resumeService.handleDeleteResume(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/resumes/{id}")
    @ApiMessage("Fetch resume by id")
    public ResponseEntity<ResFetchResumeDTO> fetchResumeById(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Resume> currentResumeOpt = this.resumeService.handleFetchResumeById(id);
        if (!currentResumeOpt.isPresent()) {
            throw new IdInvalidException("Resume with id " + id + " not found");
        }
        return ResponseEntity.ok().body(this.resumeService.convertToResFetchResumeDTO(currentResumeOpt.get()));
    }

    @GetMapping("/resumes")
    @ApiMessage("Fetch all resumes")
    public ResponseEntity<ResultPaginationDTO> fetchAllResumes(
            @Filter Specification<Resume> specification,
            Pageable pageable) {

        List<Long> jobIds = null;
        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        User currentUser = this.userService.handleFetchUserByUsername(email);
        if (currentUser != null) {
            Company userCompany = currentUser.getCompany();
            if (userCompany != null) {
                List<Job> companyJobs = userCompany.getJobs();
                if (companyJobs != null && companyJobs.size() > 0) {
                    jobIds = companyJobs.stream().map(x -> x.getId()).collect(Collectors.toList());
                }
            }
        }

        Specification<Resume> jobInSpec = filterSpecificationConverter
                .convert(filterBuilder.field("job").in(filterBuilder.input(jobIds)).get());

        Specification<Resume> finalSpec = jobInSpec.and(specification);
        return ResponseEntity.ok().body(this.resumeService.handleFetchAllResumes(finalSpec, pageable));
    }

    @PostMapping("/resumes/by-user")
    @ApiMessage("Fetch resumes by user")
    public ResponseEntity<ResultPaginationDTO> fetchResumesByUser(Pageable pageable) {
        return ResponseEntity.ok().body(this.resumeService.handleFetchResumesByUser(pageable));
    }
}
