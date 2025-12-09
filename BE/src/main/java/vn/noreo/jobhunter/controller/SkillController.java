package vn.noreo.jobhunter.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import vn.noreo.jobhunter.domain.Skill;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.service.SkillService;
import vn.noreo.jobhunter.util.annotation.ApiMessage;
import vn.noreo.jobhunter.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Skill", description = "Skill management APIs")
@RestController
@RequestMapping("/api/v1")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping("/skills")
    @ApiMessage("Create new skill")
    public ResponseEntity<Skill> createNewSkill(@Valid @RequestBody Skill newSkill) throws IdInvalidException {

        // Check if the skill already exists
        if (this.skillService.checkSkillExistsByName(newSkill.getName())) {
            throw new IdInvalidException("Skill " + newSkill.getName() + " already exists");
        }

        // Create the new skill
        return ResponseEntity.ok(this.skillService.handleCreateSkill(newSkill));
    }

    @PutMapping("/skills")
    @ApiMessage("Update skill")
    public ResponseEntity<Skill> updateSkill(@Valid @RequestBody Skill updatedSkill) throws IdInvalidException {
        // Check if the skill already exists
        boolean isSkillExists = this.skillService.checkSkillExistsByName(updatedSkill.getName());
        if (isSkillExists) {
            throw new IdInvalidException("Skill " + updatedSkill.getName() + " already exists");
        }

        // Update the skill
        Skill currentSkill = this.skillService.handleUpdateSkill(updatedSkill);
        if (currentSkill == null) {
            throw new IdInvalidException("Skill with id " + updatedSkill.getId() + " not found");
        }
        return ResponseEntity.ok(currentSkill);
    }

    @GetMapping("/skills")
    @ApiMessage("Fetch all skills")
    public ResponseEntity<ResultPaginationDTO> getAllSkills(
            @Filter Specification<Skill> specification, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.skillService.handleFetchAllSkills(specification, pageable));
    }

    @DeleteMapping("/skills/{id}")
    @ApiMessage("Delete skill by id")
    public ResponseEntity<Void> deleteSkill(@PathVariable("id") long id) throws IdInvalidException {

        // Check if the skill exists
        Skill currentSkill = this.skillService.handleFetchSkillById(id);
        if (currentSkill == null) {
            throw new IdInvalidException("Skill with id " + id + " not found");
        }
        this.skillService.handleDeleteSkill(id);
        return ResponseEntity.ok(null);
    }

}
