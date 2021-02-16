package com.jacky.imagecloud.security;

import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


public class MySQLUserDetailsService implements UserDetailsService {
    UserRepository userRepository;
    public MySQLUserDetailsService(UserRepository userRepository){
        this.userRepository=userRepository;
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
        }
        return builder.build();
    }
}
