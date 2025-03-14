package com.room.app.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.room.app.dto.Member;
import com.room.app.repository.MemberRepository;

import jakarta.transaction.Transactional;

@Service
public class MemberService {
	private final MemberRepository memberRepository;
	private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\+\\d{10,14}$");

	public MemberService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public Optional<Member> getMemberByMobileNumber(String mobileNumber) {
		return memberRepository.findByMobileNumber(mobileNumber);
	}

	public boolean existsByChatId(Long chatId) {
		return memberRepository.existsByChatId(chatId);
	}

	public boolean existsByMobileNumber(String mobile) {
		return memberRepository.findByMobileNumber(mobile).isPresent();
	}

	@Transactional
	public Member registerMember(String name, String mobileNumber, Long chatId) {
		validateMobileNumber(mobileNumber);

		if (memberRepository.existsByChatId(chatId)) {
			throw new IllegalArgumentException("❌ Chat ID already registered");
		}

		if (memberRepository.existsByMobileNumber(mobileNumber)) {
			throw new IllegalArgumentException("❌ Mobile number already registered");
		}

		Member member = new Member();
		member.setName(name);
		member.setMobileNumber(mobileNumber);
		member.setChatId(chatId);

		return save(member);
	}

	@Transactional
	public Member linkChatIdToMember(String mobileNumber, Long chatId) throws ResourceNotFoundException {
		Member member = memberRepository.findByMobileNumber(mobileNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found"));

		if (member.getChatId() != null) {
			throw new IllegalStateException("❌ Member already linked to a chat ID");
		}

		member.setChatId(chatId);
		return save(member);
	}

	private void validateMobileNumber(String mobileNumber) {
		if (!MOBILE_PATTERN.matcher(mobileNumber).matches()) {
			throw new IllegalArgumentException("❌ Invalid mobile format. Use +CountryCodeNumber");
		}
	}

	public Optional<Member> getMemberByMobile(String mobileNumber) {
		return memberRepository.findByMobileNumber(mobileNumber);
	}

	public Optional<Member> getMemberByChatId(Long chatId) {
		return memberRepository.findByChatId(chatId);
	}

	public List<Member> getAllMembers() {
		return memberRepository.findAll();
	}

	public Member getMemberById(Long id) throws ResourceNotFoundException {
		return memberRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member not found"));
	}

	public Optional<Member> getMemberByName(String name) {
		return memberRepository.findByName(name); // Returns Optional
	}

	public boolean hasExistingAdmins() {
		return memberRepository.countByAdminTrue() > 0;
	}

	@Transactional
	public Member save(Member member) {
		try {
			return memberRepository.save(member);
		} catch (DataIntegrityViolationException e) {
			throw new RuntimeException("❌ Database error: " + e.getMessage());
		}

	}

	public Optional<Member> getMemberByUserId(Long userId) {
		return memberRepository.findByUserId(userId);

	}

	public boolean existsByUserId(Long userId) {

		return memberRepository.existsById(userId);
	}

}
