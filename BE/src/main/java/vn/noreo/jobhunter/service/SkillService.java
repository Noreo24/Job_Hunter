package vn.noreo.jobhunter.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.noreo.jobhunter.domain.Skill;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.repository.SkillRepository;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public boolean checkSkillExistsByName(String name) {
        return this.skillRepository.existsByName(name);
    }

    public Skill handleCreateSkill(Skill newSkill) {
        return this.skillRepository.save(newSkill);
    }

    public Skill handleFetchSkillById(long id) {
        return this.skillRepository.findById(id).orElse(null);
    }

    public Skill handleUpdateSkill(Skill updatedSkill) {
        Skill currentSkill = this.handleFetchSkillById(updatedSkill.getId());
        if (currentSkill != null) {
            currentSkill.setName(updatedSkill.getName());

            currentSkill = this.skillRepository.save(currentSkill);
        }
        return currentSkill;
    }

    public ResultPaginationDTO handleFetchAllSkills(Specification<Skill> specification, Pageable pageable) {
        Page<Skill> skillPage = this.skillRepository.findAll(specification, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(skillPage.getTotalPages());
        meta.setTotal(skillPage.getTotalElements());

        resultPaginationDTO.setMeta(meta);

        resultPaginationDTO.setResult(skillPage.getContent());
        return resultPaginationDTO;
    }

    public void handleDeleteSkill(long id) {
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        Skill skill = skillOptional.get();

        // Delete in the job_skill table
        skill.getJobs().forEach(job -> {
            job.getSkills().remove(skill);
        });

        // Delete in the skill_subscriber table
        skill.getSubscribers().forEach(subscriber -> {
            subscriber.getSkills().remove(skill);
        });

        // Delete the skill
        this.skillRepository.delete(skill);
    }
}
