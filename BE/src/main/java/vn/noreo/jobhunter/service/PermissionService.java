package vn.noreo.jobhunter.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.noreo.jobhunter.domain.Permission;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.repository.PermissionRepository;
import vn.noreo.jobhunter.util.error.IdInvalidException;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public boolean checkPermissionExists(Permission permission) {
        return this.permissionRepository.existsByApiPathAndMethodAndModule(
                permission.getApiPath(),
                permission.getMethod(),
                permission.getModule());
    }

    public boolean isSameName(Permission permission) {
        Optional<Permission> existingPermission = this.handleFetchPermissionById(permission.getId());
        if (existingPermission.isPresent()) {
            if (existingPermission.get().getName().equals(permission.getName())) {
                return true;
            }
        }
        return false;
    }

    public Permission handleCreatePermission(Permission newPermission) {
        return this.permissionRepository.save(newPermission);
    }

    public Optional<Permission> handleFetchPermissionById(long id) {
        return this.permissionRepository.findById(id);
    }

    public Permission handleUpdatePermission(Permission updatedPermission) throws IdInvalidException {
        Optional<Permission> permissionOptional = this.handleFetchPermissionById(updatedPermission.getId());
        if (!permissionOptional.isPresent()) {
            throw new IdInvalidException("Permission with id " + updatedPermission.getId() + " not found");
        }
        Permission currentPermission = permissionOptional.get();

        // Update fields
        currentPermission.setName(updatedPermission.getName());
        currentPermission.setApiPath(updatedPermission.getApiPath());
        currentPermission.setMethod(updatedPermission.getMethod());
        currentPermission.setModule(updatedPermission.getModule());

        return this.permissionRepository.save(currentPermission);
    }

    public ResultPaginationDTO handleFetchAllPermissions(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> permissionPage = this.permissionRepository.findAll(spec, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(permissionPage.getTotalPages());
        meta.setTotal(permissionPage.getTotalElements());

        resultPaginationDTO.setMeta(meta);

        resultPaginationDTO.setResult(permissionPage.getContent());
        return resultPaginationDTO;
    }

    public void handleDeletePermission(long id) {
        // Delete in the permission_role table
        Optional<Permission> permissionOptional = this.permissionRepository.findById(id);
        Permission permission = permissionOptional.get();
        permission.getRoles().forEach(role -> {
            role.getPermissions().remove(permission);
        });

        // Delete the skill
        this.permissionRepository.delete(permission);
    }

}
