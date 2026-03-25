package CEOS.concurrency.domain.payment.security;

import CEOS.concurrency.common.enums.Role;
import CEOS.concurrency.domain.payment.entity.Store;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class StoreUserDetails implements UserDetails {

    private final Long id;
    private final String githubId;

    public StoreUserDetails(Store store) {
        this.id = store.getId();
        this.githubId = store.getGithubId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(Role.STORE.getValue()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return githubId;
    }
}
