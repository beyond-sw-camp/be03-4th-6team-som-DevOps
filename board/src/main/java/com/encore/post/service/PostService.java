package com.encore.post.service;


import com.encore.post.domain.Post;
import com.encore.post.dto.PostReqDto;
import com.encore.post.dto.PostResDto;
import com.encore.post.dto.PostSearchDto;
import com.encore.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class PostService{
    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post create(PostReqDto postReqDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        LocalDate today = LocalDate.now();
        List<Post> pst = postRepository.findByEmailAndCreatedAtBetween(email, today.atStartOfDay(), today.atTime(23, 59, 59));
        if (pst.size() >= 5) {
            throw new IllegalArgumentException("하루 최대 포스팅 횟수를 넘겼습니다.");
        }
      
        Post new_post = Post.builder()
                .title(postReqDto.getTitle())
                .contents(postReqDto.getContents())
                .email(email)
                .build();

        Post post = postRepository.save(new_post);
        return post;
    }

    public List<PostResDto> findAll(Pageable pageable) {
        Specification<Post> spec = new Specification<Post>() {
            @Override
            public Predicate toPredicate(Root<Post> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>(); //쿼리를 생성하기 위해서 predicates라는 리스트 생성
                //                delYn 기본 N으로 설정
                predicates.add(criteriaBuilder.equal(root.get("delYn"), "N"));

                //                리스트였던 predicates를 배열로 변환
                Predicate[] predicateArr = new Predicate[predicates.size()];
                for (int i = 0; i < predicates.size(); i++) {
                    predicateArr[i] = predicates.get(i);
                }

                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };

        Page<Post> posts = postRepository.findAll(spec, pageable); // select * from post
        List<Post> postList = posts.getContent();
        List<PostResDto> postResDtos = new ArrayList<>();
        postResDtos = postList.stream()
                .map(p -> PostResDto.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .contents(p.getContents())
                        .member_email(p.getEmail())
                        .build()).collect(Collectors.toList());

//        Page<PostResDto> postResDtos
//                = posts.map(p -> new PostResDto(p.getId(), p.getTitle(), p.getEmail()==null? "익명유저" : email));
        return postResDtos;
    }

    public Post update(Long id, PostReqDto postReqDto) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("not found post"));
        post.updatePost(postReqDto.getTitle(), postReqDto.getContents());
        return post;
    }

    public Post delete(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("not found post"));
        post.deletePost();
        return post;
    }

}
