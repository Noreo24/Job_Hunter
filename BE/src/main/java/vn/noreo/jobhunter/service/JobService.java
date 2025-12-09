package vn.noreo.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.noreo.jobhunter.domain.Company;
import vn.noreo.jobhunter.domain.Job;
import vn.noreo.jobhunter.domain.Skill;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.domain.response.job.ResCreateJobDTO;
import vn.noreo.jobhunter.domain.response.job.ResUpdateJobDTO;
import vn.noreo.jobhunter.repository.CompanyRepository;
import vn.noreo.jobhunter.repository.JobRepository;
import vn.noreo.jobhunter.repository.SkillRepository;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final CompanyRepository companyRepository;

    public JobService(JobRepository jobRepository, SkillRepository skillRepository,
            CompanyRepository companyRepository) {
        this.skillRepository = skillRepository;
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    public Optional<Job> handleFetchJobById(long id) {
        return this.jobRepository.findById(id);
    }

    public ResCreateJobDTO handleCreateJob(Job newJob) {

        // Check skill exists
        if (newJob.getSkills() != null) {
            List<Long> reqSkills = newJob.getSkills().stream()
                    .map(skill -> skill.getId())
                    .collect(Collectors.toList());
            List<Skill> listSkill = this.skillRepository.findAllById(reqSkills);
            newJob.setSkills(listSkill);
        }

        // Check company
        if (newJob.getCompany() != null) {
            Optional<Company> companyOptional = this.companyRepository.findById(newJob.getCompany().getId());
            if (companyOptional.isPresent()) {
                newJob.setCompany(companyOptional.get());
            }
        }

        // Create new job
        Job currentJob = this.jobRepository.save(newJob);

        // Convert to DTO
        ResCreateJobDTO jobDTO = new ResCreateJobDTO();
        jobDTO.setId(currentJob.getId());
        jobDTO.setName(currentJob.getName());
        jobDTO.setSalary(currentJob.getSalary());
        jobDTO.setQuantity(currentJob.getQuantity());
        jobDTO.setLocation(currentJob.getLocation());
        jobDTO.setLevel(currentJob.getLevel());
        jobDTO.setStartDate(currentJob.getStartDate());
        jobDTO.setEndDate(currentJob.getEndDate());
        jobDTO.setActive(currentJob.isActive());
        jobDTO.setCreatedAt(currentJob.getCreatedAt());
        jobDTO.setCreatedBy(currentJob.getCreatedBy());

        if (currentJob.getSkills() != null) {
            List<String> listSkills = currentJob.getSkills().stream()
                    .map(skill -> skill.getName())
                    .collect(Collectors.toList());
            jobDTO.setSkills(listSkills);
        }

        return jobDTO;
    }

    public ResUpdateJobDTO handleUpdateJob(Job updatedJob, Job dbJob) {

        // Check skill exists
        if (updatedJob.getSkills() != null) {
            List<Long> reqSkills = updatedJob.getSkills().stream()
                    .map(skill -> skill.getId())
                    .collect(Collectors.toList());
            List<Skill> listSkill = this.skillRepository.findAllById(reqSkills);
            dbJob.setSkills(listSkill);
        }

        // Check company
        if (updatedJob.getCompany() != null) {
            Optional<Company> companyOptional = this.companyRepository.findById(updatedJob.getCompany().getId());
            if (companyOptional.isPresent()) {
                dbJob.setCompany(companyOptional.get());
            }
        }

        dbJob.setName(updatedJob.getName());
        dbJob.setLocation(updatedJob.getLocation());
        dbJob.setSalary(updatedJob.getSalary());
        dbJob.setQuantity(updatedJob.getQuantity());
        dbJob.setLevel(updatedJob.getLevel());
        dbJob.setDescription(updatedJob.getDescription());
        dbJob.setStartDate(updatedJob.getStartDate());
        dbJob.setEndDate(updatedJob.getEndDate());
        dbJob.setActive(updatedJob.isActive());

        // Update job
        dbJob = this.jobRepository.save(dbJob);

        // Convert to DTO
        ResUpdateJobDTO jobDTO = new ResUpdateJobDTO();
        jobDTO.setId(dbJob.getId());
        jobDTO.setName(dbJob.getName());
        jobDTO.setSalary(dbJob.getSalary());
        jobDTO.setQuantity(dbJob.getQuantity());
        jobDTO.setLocation(dbJob.getLocation());
        jobDTO.setLevel(dbJob.getLevel());
        jobDTO.setStartDate(dbJob.getStartDate());
        jobDTO.setEndDate(dbJob.getEndDate());
        jobDTO.setActive(dbJob.isActive());
        jobDTO.setUpdatedAt(dbJob.getUpdatedAt());
        jobDTO.setUpdatedBy(dbJob.getUpdatedBy());

        if (dbJob.getSkills() != null) {
            List<String> listSkills = dbJob.getSkills().stream()
                    .map(skill -> skill.getName())
                    .collect(Collectors.toList());
            jobDTO.setSkills(listSkills);
        }

        return jobDTO;
    }

    public void handleDeleteJob(long id) {
        this.jobRepository.deleteById(id);
    }

    public ResultPaginationDTO handleFetchAllJobs(Specification<Job> specification, Pageable pageable) {
        Page<Job> jobPage = this.jobRepository.findAll(specification, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(jobPage.getTotalPages());
        meta.setTotal(jobPage.getTotalElements());

        resultPaginationDTO.setMeta(meta);

        resultPaginationDTO.setResult(jobPage.getContent());
        return resultPaginationDTO;
    }
}