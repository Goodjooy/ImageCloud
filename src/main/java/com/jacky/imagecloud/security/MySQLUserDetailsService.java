package com.jacky.imagecloud.security;

import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.slf4j.Logger;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.naming.AuthenticationNotSupportedException;


public class MySQLUserDetailsService implements UserDetailsService {
    UserRepository userRepository;
    private Logger logger;

    public MySQLUserDetailsService(UserRepository userRepository, Logger logger){
        this.userRepository=userRepository;
        this.logger = logger;
    }


    /**
     * Locates the user based on the username. In the actual implementation, the search
     * may possibly be case sensitive, or case insensitive depending on how the
     * implementation instance is configured. In this case, the <code>UserDetails</code>
     * object that comes back may have a username that is of a different case than what
     * was actually requested..
     *
     * @param emailAddress the username identifying the user whose data is required.
     * @return a fully populated user record (never <code>null</code>)
     * @throws UsernameNotFoundException if the user could not be found or the user has no
     *                                   GrantedAuthority
     */
    @Override
    public UserDetails loadUserByUsername(String emailAddress) throws UsernameNotFoundException {
        var builder = org.springframework.security.core.userdetails.User.withUsername(emailAddress);
        User user = new User();
        user.setEmailAddress(emailAddress);
        var result = userRepository.findAll(Example.of(user));
        if (result.size()>=1) {
            var u = result.get(0);
            builder.password(u.getPasswordHash());
            builder.roles("USER");

            logger.info(String.format("find user<%s> in database",emailAddress));
            return builder.build();
        }
        logger.info(String.format("user<%s> not in database",emailAddress));
        throw new UsernameNotFoundException(emailAddress);
    }
}
