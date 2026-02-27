package course.QAssistant.config;

import course.QAssistant.util.IdWorker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CommonConfig {
    // 密码加密器 BCryptPasswordEncoder 方法采用 SHA-256 对密码进行加密
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public IdWorker idWorker(){
        //基于运维人员对机房和机器的编号规划自行约定
        return new IdWorker(1l,2l);
    }

}
