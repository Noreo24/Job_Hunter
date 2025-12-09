package vn.noreo.jobhunter.controller;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import vn.noreo.jobhunter.domain.Permission;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.service.PermissionService;
import vn.noreo.jobhunter.util.annotation.ApiMessage;
import vn.noreo.jobhunter.util.error.IdInvalidException;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Permission", description = "Permission management APIs")
@RestController
@RequestMapping("/api/v1")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/permissions")
    @ApiMessage("Create new permission")
    public ResponseEntity<Permission> createNewPermission(@Valid @RequestBody Permission newPermission)
            throws IdInvalidException {
        // Check if the permission already exists
        if (this.permissionService.checkPermissionExists(newPermission)) {
            throw new IdInvalidException("Permission already exists: " + newPermission.getName());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.permissionService.handleCreatePermission(newPermission));
    }

    @PutMapping("/permissions")
    @ApiMessage("Update permission")
    public ResponseEntity<Permission> updatePermission(@Valid @RequestBody Permission updatedPermission)
            throws IdInvalidException {
        // Check if the permission already exists
        if (this.permissionService.checkPermissionExists(updatedPermission)) {
            if (this.permissionService.isSameName(updatedPermission)) {
                throw new IdInvalidException("Permission already exists: " + updatedPermission.getName());
            }
        }
        return ResponseEntity.ok(this.permissionService.handleUpdatePermission(updatedPermission));
    }

    @GetMapping("/permissions")
    @ApiMessage("Fetch all permissions")
    public ResponseEntity<ResultPaginationDTO> fetchAllPermissions(@Filter Specification<Permission> specification,
            Pageable pageable) {
        return ResponseEntity.ok().body(this.permissionService.handleFetchAllPermissions(specification, pageable));
    }

    @DeleteMapping("/permissions/{id}")
    @ApiMessage("Delete permission by id")
    public ResponseEntity<Void> deletePermission(@PathVariable("id") long id) throws IdInvalidException {
        // Check if the permission exists
        Optional<Permission> currentPermissionOptional = this.permissionService.handleFetchPermissionById(id);
        if (currentPermissionOptional.isEmpty()) {
            throw new IdInvalidException("Permission with id " + id + " not found");
        }
        this.permissionService.handleDeletePermission(id);
        return ResponseEntity.ok(null);
    }

}
