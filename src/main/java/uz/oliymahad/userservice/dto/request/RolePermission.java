package uz.oliymahad.userservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RolePermission {
    private String roleName;
    private PermissionRequestDto permissionRequestDto;
}
