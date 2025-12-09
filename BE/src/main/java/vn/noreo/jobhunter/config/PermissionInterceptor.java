package vn.noreo.jobhunter.config;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.noreo.jobhunter.domain.Permission;
import vn.noreo.jobhunter.domain.Role;
import vn.noreo.jobhunter.domain.User;
import vn.noreo.jobhunter.service.UserService;
import vn.noreo.jobhunter.util.SecurityUtil;
import vn.noreo.jobhunter.util.error.PermissionException;

public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    @Override
    @Transactional
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);

        // Check permission
        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        if (email != null && !email.isEmpty()) {
            User user = this.userService.handleFetchUserByUsername(email);
            if (user != null) {
                Role role = user.getRole();
                if (role != null) {
                    List<Permission> permissions = role.getPermissions();
                    boolean isAllowed = permissions.stream()
                            .anyMatch(eachPermission -> eachPermission.getApiPath().equals(path)
                                    && eachPermission.getMethod().equals(httpMethod));
                    if (isAllowed == false) {
                        throw new PermissionException("You are not authorized to access this resource");
                    }
                } else {
                    throw new PermissionException("Access denied: missing required role");
                }
            }
        }
        return true;
    }
}