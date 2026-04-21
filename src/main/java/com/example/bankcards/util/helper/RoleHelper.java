package com.example.bankcards.util.helper;

import com.example.bankcards.entity.Role;
import com.example.bankcards.util.enums.RolesEnum;

import java.util.ArrayList;
import java.util.List;

public class RoleHelper {

    public static List<String> getRoles(List<Role> roles) {
        List<String> result = new ArrayList<>();
        result.add(RolesEnum.ROLE_USER.name());
        result.addAll(roles.stream().map(Role::getName).toList());

        return result;
    }
}
