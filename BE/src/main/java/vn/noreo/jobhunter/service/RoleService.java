package vn.noreo.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.noreo.jobhunter.domain.Permission;
import vn.noreo.jobhunter.domain.Role;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.repository.PermissionRepository;
import vn.noreo.jobhunter.repository.RoleRepository;
import vn.noreo.jobhunter.util.error.IdInvalidException;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public boolean checkRoleExists(String name) {
        return roleRepository.existsByName(name);
    }

    public Optional<Role> handleFetchRoleById(long id) {
        return this.roleRepository.findById(id);
    }

    public Role handleCreateRole(Role newRole) throws IdInvalidException {
        // Check if the role already exists
        if (this.checkRoleExists(newRole.getName())) {
            throw new IdInvalidException("Role with name " + newRole.getName() + " already exists");
        }

        // Check permissions exists
        if (newRole.getPermissions() != null) {
            List<Long> reqPermissions = newRole.getPermissions().stream()
                    .map(permissions -> permissions.getId())
                    .collect(Collectors.toList());
            List<Permission> listPermissions = this.permissionRepository.findAllById(reqPermissions);
            newRole.setPermissions(listPermissions);
        }

        return this.roleRepository.save(newRole);
    }

    public Role handleUpdateRole(Role updatedRole) throws IdInvalidException {
        // Check if the role exists
        Optional<Role> roleOptional = this.roleRepository.findById(updatedRole.getId());
        if (roleOptional.isEmpty()) {
            throw new IdInvalidException("Role with id " + updatedRole.getId() + " does not exist");
        }
        Role currentRole = roleOptional.get();
        // currentRole.setName("NONAME");
        // this.roleRepository.save(currentRole);

        // Check if the role name already exists
        // if (this.checkRoleExists(updatedRole.getName())) {
        // throw new IdInvalidException("Role with name " + updatedRole.getName() + "
        // already exists");
        // }

        // Update fields
        currentRole.setName(updatedRole.getName());
        currentRole.setDescription(updatedRole.getDescription());
        currentRole.setActive(updatedRole.isActive());

        // Check permissions exists
        if (updatedRole.getPermissions() != null) {
            List<Long> reqPermissions = updatedRole.getPermissions().stream()
                    .map(permissions -> permissions.getId())
                    .collect(Collectors.toList());
            List<Permission> listPermissions = this.permissionRepository.findAllById(reqPermissions);
            currentRole.setPermissions(listPermissions);
        }

        return this.roleRepository.save(currentRole);
    }

    public ResultPaginationDTO handleFetchAllRoles(Specification<Role> specification, Pageable pageable) {
        Page<Role> rolePage = this.roleRepository.findAll(specification, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(rolePage.getTotalPages());
        meta.setTotal(rolePage.getTotalElements());

        resultPaginationDTO.setMeta(meta);

        resultPaginationDTO.setResult(rolePage.getContent());
        return resultPaginationDTO;
    }

    public void handleDeleteRole(long id) {
        this.roleRepository.deleteById(id);
    }
}
