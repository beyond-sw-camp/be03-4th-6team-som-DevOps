package com.encore.admin.service;


import com.encore.admin.domain.Member;
import com.encore.admin.dto.*;
import com.encore.admin.repository.MemberRepository;

import com.encore.common.support.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.awt.print.Pageable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@Slf4j
@Transactional
@Service
public class MemberService {

    private final MemberRepository repository;
    @Autowired
    public MemberService(MemberRepository repository) {
        this.repository = repository;
    }

    public void save(SignUpRequest signUpRequest) {
        Member member = Member.builder()
                .role(Role.USER)
                .email(signUpRequest.getEmail())
                .nickname(signUpRequest.getNickname())
                .password(signUpRequest.getPassword())
                .ranking(0L)
                .build();

        repository.save(member);
    }

    public void update(MemberUpdateRequest memberUpdateRequest) {
        Member member = repository.findById(memberUpdateRequest.getId()).orElseThrow(()-> new NoSuchElementException("찾는 회원이 없습니다."));
        member.updateMember(member, memberUpdateRequest);
        repository.save(member);

    }

    public List<SignInResponse> findAll() {
        List<Member> members = repository.findAll();

        return members.stream().map(SignInResponse::of).collect(Collectors.toList());
    }

    public SignInResponse findById(Long id) {
        Member member = repository.findById(id).orElseThrow(()-> new NoSuchElementException("찾는 회원이 없습니다."));
        return SignInResponse.of(member);
    }

    public void delete(Long id) {
        Member member = repository.findById(id).orElseThrow(()-> new NoSuchElementException("찾는 회원이 없습니다."));
        repository.delete(member);
    }

    public RankingListResponse loadRankingList(RankingListRequst requst) {
        List<Member> members = repository.findAllByEmailIn(requst.getEmailList());
        List<Ranking> rankingList = members.stream().map(Ranking::of).collect(Collectors.toList());
        return RankingListResponse.builder().rankingList(rankingList).build();
    }

    public RankingListResponse loadRankingListTop10() {

        System.out.println("1");
        List<Member> members = repository.findTop10ByOrderByRankingDesc();
        System.out.println(members.get(0).getRanking());
        List<Ranking> rankingList = members.stream().map(Ranking::of).collect(Collectors.toList());
        return RankingListResponse.builder().rankingList(rankingList).build();
    }
}
