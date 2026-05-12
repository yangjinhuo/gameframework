package com.scriptplatform.config;

import com.scriptplatform.entity.SysUser;
import com.scriptplatform.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Seeds built-in accounts on first startup.
 * <p>
 * Default accounts:
 * <ul>
 *   <li>admin  / Admin@123   (super_admin)</li>
 *   <li>devops / Devops@123  (admin)</li>
 *   <li>viewer / Viewer@123  (readonly)</li>
 * </ul>
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        List<DefaultUser> defaults = Arrays.asList(
                new DefaultUser("admin", "Admin@123", "super_admin", "默认超级管理员"),
                new DefaultUser("devops", "Devops@123", "admin", "默认运维管理员"),
                new DefaultUser("viewer", "Viewer@123", "readonly", "默认只读用户")
        );

        for (DefaultUser d : defaults) {
            if (sysUserMapper.selectByUsername(d.username) != null) continue;
            SysUser u = new SysUser();
            u.setUsername(d.username);
            u.setPassword(passwordEncoder.encode(d.password));
            u.setRole(d.role);
            u.setStatus(1);
            u.setRemark(d.remark);
            sysUserMapper.insert(u);
            log.info("seeded default user: {} ({})", d.username, d.role);
        }
    }

    private static class DefaultUser {
        final String username;
        final String password;
        final String role;
        final String remark;

        DefaultUser(String username, String password, String role, String remark) {
            this.username = username;
            this.password = password;
            this.role = role;
            this.remark = remark;
        }
    }
}
