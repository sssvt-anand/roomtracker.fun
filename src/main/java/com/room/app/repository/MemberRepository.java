package com.room.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.room.app.dto.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByName(String name);

	boolean existsByMobileNumber(String mobileNumber);

	boolean existsByChatId(Long chatId);

	Optional<Member> findByChatId(Long chatId);

	Optional<Member> findByMobileNumber(String mobileNumber);
	long countByAdminTrue();
    

	boolean existsByName(String name);

	Optional<Member> findByUserId(Long userId);

	

	

	

	

	

}
