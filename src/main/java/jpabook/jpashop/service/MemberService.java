package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {


//    @Autowired // field injection (cf. setter injection, constructor injection)
    private final MemberRepository memberRepository;

    /** join
     *
     * @param member
     * @return
     */
    @Transactional // readOnly = false
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);

        return member.getId();
    }

    /**
     * 중복 회원 검증
     *
     * @param member
     */
    private void validateDuplicateMember(Member member) {
        // OR count members -> if not 0
        List<Member> findMembers = memberRepository.findByName(member.getName()); // name -> unique key
        if(!findMembers.isEmpty()) {
            throw new IllegalStateException("Already Exists.");
        }
    }

    /**
     * 전체 회원 조회
     *
     * @return
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     * 개별 회원 조회
     *
     * @param memberId
     * @return
     */
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
