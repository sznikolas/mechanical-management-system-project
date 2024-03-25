package com.nikolas.mechanicalmanagementsystem.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    // Creating an instance of HeaderWriterLogoutHandler to clear cookie data upon user logout.
//    HeaderWriterLogoutHandler clearSiteData = new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.COOKIES));

    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomAccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    /**
     * By registering this bean, we can listen for and respond to HttpSession events using Spring's event handling mechanisms.
     **/
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

//    @Bean
//    public DaoAuthenticationProvider daoAuthenticationProvider(){
//        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//
//        daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
//        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
//
//        // Check user status before authentication
//        daoAuthenticationProvider.setPreAuthenticationChecks(userDetails -> {
//            if (!userDetails.isEnabled()) {
//                throw new EmailNotVerifiedException("SecurityConfiguration::daoAuthenticationProvider - User account is not verified");
//            } else if (!userDetails.isAccountNonLocked()){
//                throw  new AccountLockedException("SecurityConfiguration::daoAuthenticationProvider - User account is locked");
//            }
//        });
//        return daoAuthenticationProvider;
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
//                .csrf((csrf -> csrf.ignoringRequestMatchers("/h2-console/**")))
//                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
//                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/",
                                        "/login",
                                        "/error",
                                        "/verificationToken/**",
                                        "/registration", "/registration/**", "/forgotPassword/**", "/company/**",
                                        "/searchAndJoinCompany/**", "/actuator").permitAll()
                                .requestMatchers("/users/**").hasRole("ADMIN")
                                .requestMatchers("/deleted_machines/**").hasAnyRole("ADMIN")
                                .requestMatchers("/machines/**").hasAnyRole("ADMIN", "USER")
                                .anyRequest().authenticated())

                .formLogin(formLogin ->
                        formLogin.loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/searchAndJoinCompany", true)
                                .failureHandler(customAuthenticationFailureHandler).permitAll())

                .logout(logout ->
                        logout.invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .deleteCookies("JSESSIONID")
                                .addLogoutHandler(securityContextLogoutHandler())
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout"))

                .exceptionHandling(e -> e.accessDeniedHandler(customAccessDeniedHandler()))
                .build();
    }
}