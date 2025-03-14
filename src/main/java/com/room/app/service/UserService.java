package com.room.app.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.room.app.dto.User;
import com.room.app.dto.UserResponse;
import com.room.app.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public void register(User user) throws Exception {
		if (userRepository.existsByUsername(user.getUsername())) {
			throw new Exception("Username already exists");
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepository.save(user);
	}

	public List<UserResponse> getAllUsers() {
		List<UserResponse> users = userRepository.findAll().stream()
				.map(user -> new UserResponse(user.getId(), user.getUsername(), user.getRole().replace("ROLE_", "")))
				.collect(Collectors.toList());

		return users;
	}

	public UserResponse updateUserRole(Long userId, String newRole) throws ResourceNotFoundException {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		String normalizedRole = newRole.startsWith("ROLE_") ? newRole : "ROLE_" + newRole;
		user.setRole(normalizedRole);

		User updatedUser = userRepository.save(user);

		return new UserResponse(updatedUser.getId(), updatedUser.getUsername(), normalizedRole.replace("ROLE_", ""));
	}

}
