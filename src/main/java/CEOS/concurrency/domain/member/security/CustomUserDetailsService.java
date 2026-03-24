package CEOS.concurrency.domain.member.security;

import CEOS.concurrency.common.exception.BusinessException;
import CEOS.concurrency.domain.member.entity.Member;
import CEOS.concurrency.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static CEOS.concurrency.common.code.BusinessErrorCode.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
        Member member = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));
        return new CustomUserDetails(member);
    }

    public UserDetails loadByMemberUUID(UUID uuid) throws UsernameNotFoundException {
        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));
        return new CustomUserDetails(member);
    }
}
