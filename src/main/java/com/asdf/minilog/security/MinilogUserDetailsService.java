package com.asdf.minilog.security;

import com.asdf.minilog.entity.User;
import com.asdf.minilog.exception.UserNotFoundException;
import com.asdf.minilog.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MinilogUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Autowired
  public MinilogUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new UserNotFoundException("User not found with username : " + username));

    List<GrantedAuthority> authorities =
        user.getRoles().stream().map(MinilogGrantedAuthority::new).collect(Collectors.toList());

    return new MinilogUserDetails(user.getId(), username, user.getPassword(), authorities);
  }
}
