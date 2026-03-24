package CEOS.concurrency.domain.member.security;

import CEOS.concurrency.common.enums.CharacterType;
import CEOS.concurrency.domain.member.entity.Member;
import CEOS.concurrency.common.enums.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final UUID uuid;
    private final String nickname;
    private final CharacterType characterType;
    private final String password;
    private final Role role;

    public CustomUserDetails(Member member) {
        this.id = member.getId();
        this.uuid = member.getUuid();
        this.nickname = member.getNickname();
        this.characterType = member.getCharacterType();
        this.password = member.getPassword();
        this.role = member.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getValue()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return nickname;
    }
}
