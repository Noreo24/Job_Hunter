package vn.noreo.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.turkraft.springfilter.converter.FilterSpecification;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import com.turkraft.springfilter.parser.FilterParser;
import com.turkraft.springfilter.parser.node.FilterNode;

import vn.noreo.jobhunter.domain.Job;
import vn.noreo.jobhunter.domain.Resume;
import vn.noreo.jobhunter.domain.User;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.domain.response.resume.ResCreateResumeDTO;
import vn.noreo.jobhunter.domain.response.resume.ResFetchResumeDTO;
import vn.noreo.jobhunter.domain.response.resume.ResUpdateResumeDTO;
import vn.noreo.jobhunter.repository.JobRepository;
import vn.noreo.jobhunter.repository.ResumeRepository;
import vn.noreo.jobhunter.repository.UserRepository;
import vn.noreo.jobhunter.util.SecurityUtil;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final FilterParser filterParser;
    private final FilterSpecificationConverter filterSpecificationConverter;

    public ResumeService(ResumeRepository resumeRepository,
            UserRepository userRepository,
            JobRepository jobRepository, FilterParser filterParser,
            FilterSpecificationConverter filterSpecificationConverter) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.filterParser = filterParser;
        this.filterSpecificationConverter = filterSpecificationConverter;
    }

    public Optional<Resume> handleFetchResumeById(long id) {
        return this.resumeRepository.findById(id);
    }

    public boolean checkResumeExistsByUserAndJob(Resume resume) {

        // Check user by id
        if (resume.getUser() == null) {
            return false;
        }
        Optional<User> userOpt = this.userRepository.findById(resume.getUser().getId());
        if (userOpt.isEmpty()) {
            return false;
        }

        // Check job by id
        if (resume.getJob() == null) {
            return false;
        }
        Optional<Job> jobOpt = this.jobRepository.findById(resume.getJob().getId());
        if (jobOpt.isEmpty()) {
            return false;
        }

        return true;
    }

    public ResCreateResumeDTO handleCreateResume(Resume newResume) {
        // Save the new resume
        newResume = this.resumeRepository.save(newResume);

        // Convert to DTO
        ResCreateResumeDTO resCreateResumeDTO = new ResCreateResumeDTO();
        resCreateResumeDTO.setId(newResume.getId());
        resCreateResumeDTO.setCreatedAt(newResume.getCreatedAt());
        resCreateResumeDTO.setCreatedBy(newResume.getCreatedBy());
        return resCreateResumeDTO;
    }

    public ResUpdateResumeDTO handleUpdateResume(Resume updatedResume) {
        updatedResume = this.resumeRepository.save(updatedResume);

        // Convert to DTO
        ResUpdateResumeDTO resUpdateResumeDTO = new ResUpdateResumeDTO();
        resUpdateResumeDTO.setUpdatedAt(updatedResume.getUpdatedAt());
        resUpdateResumeDTO.setUpdatedBy(updatedResume.getUpdatedBy());
        return resUpdateResumeDTO;
    }

    public void handleDeleteResume(long id) {
        this.resumeRepository.deleteById(id);
    }

    public ResFetchResumeDTO convertToResFetchResumeDTO(Resume resume) {
        ResFetchResumeDTO resumeDTO = new ResFetchResumeDTO();
        ResFetchResumeDTO.User userDTO = new ResFetchResumeDTO.User();
        userDTO.setId(resume.getUser().getId());
        userDTO.setName(resume.getUser().getName());

        ResFetchResumeDTO.Job jobDTO = new ResFetchResumeDTO.Job();
        jobDTO.setId(resume.getJob().getId());
        jobDTO.setName(resume.getJob().getName());

        resumeDTO.setId(resume.getId());
        resumeDTO.setEmail(resume.getEmail());
        resumeDTO.setUrl(resume.getUrl());
        resumeDTO.setStatus(resume.getStatus());
        resumeDTO.setCreatedAt(resume.getCreatedAt());
        resumeDTO.setUpdatedAt(resume.getUpdatedAt());
        resumeDTO.setCreatedBy(resume.getCreatedBy());
        resumeDTO.setUpdatedBy(resume.getUpdatedBy());
        if (resume.getJob() != null) {
            resumeDTO.setCompanyName(resume.getJob().getCompany().getName());
        }
        resumeDTO.setUser(userDTO);
        resumeDTO.setJob(jobDTO);
        return resumeDTO;
    }

    public ResultPaginationDTO handleFetchAllResumes(Specification<Resume> specification, Pageable pageable) {
        Page<Resume> resumePage = this.resumeRepository.findAll(specification, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(resumePage.getTotalPages());
        meta.setTotal(resumePage.getTotalElements());

        resultPaginationDTO.setMeta(meta);

        // List<ResFetchResumeDTO> resumeDTOs = resumePage.getContent().stream()
        // .map(item -> new ResFetchResumeDTO(
        // item.getId(),
        // item.getEmail(),
        // item.getUrl(),
        // item.getState(),
        // item.getCreatedAt(),
        // item.getUpdatedAt(),
        // item.getCreatedBy(),
        // item.getUpdatedBy(),
        // new ResFetchResumeDTO.User(
        // item.getUser().getId(),
        // item.getUser().getName()),
        // new ResFetchResumeDTO.Job(
        // item.getJob().getId(),
        // item.getJob().getName())))
        // .collect(Collectors.toList());

        // or

        // List<ResFetchResumeDTO> resumeDTOs = resumePage.getContent().stream()
        // .map(item -> this.convertToResFetchResumeDTO(item))
        // .collect(Collectors.toList());

        List<ResFetchResumeDTO> resumeDTOs = resumePage.stream()
                .map(this::convertToResFetchResumeDTO)
                .collect(Collectors.toList());

        resultPaginationDTO.setResult(resumeDTOs);
        return resultPaginationDTO;
    }

    public ResultPaginationDTO handleFetchResumesByUser(Pageable pageable) {
        // Query builder to fetch resumes by user
        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        FilterNode filterNode = filterParser.parse("email='" + email + "'");
        FilterSpecification<Resume> filterSpecification = filterSpecificationConverter.convert(filterNode);

        // Fetch resumes by user
        Page<Resume> resumePage = this.resumeRepository.findAll(filterSpecification, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(resumePage.getTotalPages());
        meta.setTotal(resumePage.getTotalElements());

        resultPaginationDTO.setMeta(meta);

        List<ResFetchResumeDTO> resumeDTOs = resumePage.stream()
                .map(this::convertToResFetchResumeDTO)
                .collect(Collectors.toList());

        resultPaginationDTO.setResult(resumeDTOs);
        return resultPaginationDTO;
    }
}
