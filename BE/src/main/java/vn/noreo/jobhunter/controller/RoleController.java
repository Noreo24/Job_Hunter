package vn.noreo.jobhunter.controller;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import vn.noreo.jobhunter.domain.Role;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.service.RoleService;
import vn.noreo.jobhunter.util.annotation.ApiMessage;
import vn.noreo.jobhunter.util.error.IdInvalidException;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Role", description = "Role management APIs")
@RestController
@RequestMapping("/api/v1")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    @ApiMessage("Create new role")
    public ResponseEntity<Role> createNewRole(@Valid @RequestBody Role newRole) throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roleService.handleCreateRole(newRole));
    }

    @PutMapping("/roles")
    @ApiMessage("Update role")
    public ResponseEntity<Role> updateRole(@Valid @RequestBody Role updatedRole) throws IdInvalidException {
        return ResponseEntity.ok(this.roleService.handleUpdateRole(updatedRole));
    }

    @GetMapping("/roles")
    @ApiMessage("Fetch all roles")
    public ResponseEntity<ResultPaginationDTO> fetchAllRoles(@Filter Specification<Role> specification,
            Pageable pageable) {
        return ResponseEntity.ok().body(this.roleService.handleFetchAllRoles(specification, pageable));
    }

    @GetMapping("/roles/{id}")
    @ApiMessage("Fetch role by id")
    public ResponseEntity<Role> fetchRoleById(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Role> currentRoleOpt = this.roleService.handleFetchRoleById(id);
        if (!currentRoleOpt.isPresent()) {
            throw new IdInvalidException("Role with id " + id + " not found");
        }
        return ResponseEntity.ok(currentRoleOpt.get());
    }

    @DeleteMapping("/roles/{id}")
    @ApiMessage("Delete role by id")
    public ResponseEntity<Void> deleteRoleById(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Role> currentRoleOpt = this.roleService.handleFetchRoleById(id);
        if (!currentRoleOpt.isPresent()) {
            throw new IdInvalidException("Role with id " + id + " not found");
        }
        this.roleService.handleDeleteRole(id);
        return ResponseEntity.ok().body(null);
    }
}
