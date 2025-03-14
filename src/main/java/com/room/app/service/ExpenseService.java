package com.room.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.room.app.dto.Expense;
import com.room.app.dto.ExpenseRequest;
import com.room.app.dto.Member;
import com.room.app.dto.User;
import com.room.app.exception.AccessDeniedException;
import com.room.app.repository.ExpenseRepository;
import com.room.app.repository.MemberRepository;

import jakarta.transaction.Transactional;

@Service
public class ExpenseService {
	private final ExpenseRepository expenseRepository;
	private final MemberService memberService;
	private final MemberRepository memberRepository;

	public ExpenseService(ExpenseRepository expenseRepository, MemberService memberService,
			MemberRepository memberRepository) {
		this.expenseRepository = expenseRepository;
		this.memberService = memberService;
		this.memberRepository = memberRepository;
	}

	public List<Expense> getAllActiveExpenses() {
		return expenseRepository.findAllActive();
	}

	public void softDeleteExpense(Long id, User deletedBy) throws ResourceNotFoundException {
		Expense expense = expenseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
		expense.setIsDeleted("Y");
		expense.setDeletedBy(deletedBy);
		expense.setDeletedAt(LocalDateTime.now());
		expenseRepository.save(expense);
	}

	public List<Expense> getMonthlyExpenses() {
		LocalDate start = LocalDate.now().withDayOfMonth(1);
		LocalDate end = start.plusMonths(1).minusDays(1);
		return expenseRepository.findByDateBetween(start, end);
	}

	public List<Expense> getYearlyExpenses() {
		LocalDate start = LocalDate.now().withDayOfYear(1);
		LocalDate end = start.plusYears(1).minusDays(1);
		return expenseRepository.findByDateBetween(start, end);
	}

	public List<Expense> getExpensesByMember(Long memberId) {
		return expenseRepository.findByMemberId(memberId);
	}

	public List<Expense> getExpensesWithoutMember() {
		return expenseRepository.findByMemberIsNull();
	}

	public Optional<Expense> getExpenseByMessageId(Integer messageId) {
		return expenseRepository.findByMessageId(messageId);
	}

	public Expense saveExpense(Expense expense) {
		return expenseRepository.save(expense);
	}

	public Map<String, BigDecimal> getClearedSummaryByMember() {
		return expenseRepository.findAllActive().stream().filter(expense -> expense.getClearedAmount() != null)
				.collect(Collectors.groupingBy(expense -> expense.getMember().getName(),
						Collectors.reducing(BigDecimal.ZERO, Expense::getClearedAmount, BigDecimal::add)));
	}

	public Map<String, BigDecimal> getExpenseSummaryByMember() {
		return expenseRepository.findAllActive().stream().filter(expense -> expense.getMember() != null)
				.collect(Collectors.groupingBy(expense -> expense.getMember().getName(),
						Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
	}

	public void markExpenseCleared(Long expenseId, BigDecimal amount) throws ResourceNotFoundException {
		Expense expense = expenseRepository.findById(expenseId)
				.orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

		expense.setClearedAmount(amount);
		expense.setCleared(true); // Mark the expense as cleared
		expenseRepository.save(expense);
	}

	public Expense addExpense(ExpenseRequest expenseRequest) throws ResourceNotFoundException {
		Member member = memberService.getMemberById(expenseRequest.getMemberId());

		Expense expense = new Expense();
		expense.setMember(member);
		expense.setDescription(expenseRequest.getDescription());
		expense.setDate(expenseRequest.getDate());
		expense.setAmount(expenseRequest.getAmount());

		return expenseRepository.save(expense);
	}

	public Expense updateExpense(Long id, ExpenseRequest request, User user)
			throws ResourceNotFoundException, AccessDeniedException {
		if (!user.getRole().equalsIgnoreCase("ROLE_ADMIN") && !user.getRole().equalsIgnoreCase("ADMIN")) {
			throw new AccessDeniedException("Only admins can update expenses");
		}

		Expense existingExpense = expenseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

		// Add check for cleared expense
		if (existingExpense.isCleared()) { // Assuming you have a boolean isCleared() method
			throw new AccessDeniedException("Cannot modify cleared expenses");
		}

		// Rest of your existing code
		Member member = memberRepository.findById(request.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member not found"));

		existingExpense.setMember(member);
		existingExpense.setDescription(request.getDescription());
		existingExpense.setAmount(request.getAmount());
		existingExpense.setDate(request.getDate());

		return expenseRepository.save(existingExpense);
	}

	public void deleteExpense(Long id, User user) throws ResourceNotFoundException, AccessDeniedException {
		if (!user.getRole().equalsIgnoreCase("ROLE_ADMIN") && !user.getRole().equalsIgnoreCase("ADMIN")) {
			throw new AccessDeniedException("Only admins can delete expenses");
		}

		if (!expenseRepository.existsById(id)) {
			throw new ResourceNotFoundException("Expense not found");
		}
		expenseRepository.deleteById(id);
	}

	@Transactional
	public Expense clearExpense(Long expenseId, Long memberId, BigDecimal amount) throws ResourceNotFoundException {
	    Expense expense = expenseRepository.findById(expenseId)
	            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
	    Member clearingMember = memberRepository.findById(memberId)
	            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

	    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
	        throw new IllegalArgumentException("Amount must be greater than zero");
	    }

	    BigDecimal remaining = expense.getAmount().subtract(expense.getClearedAmount());
	    if (amount.compareTo(remaining) > 0) {
	        throw new IllegalArgumentException("Amount exceeds remaining balance of " + remaining);
	    }

	    BigDecimal newClearedAmount = expense.getClearedAmount().add(amount);
	    boolean fullyCleared = newClearedAmount.compareTo(expense.getAmount()) >= 0;

	    // Track last payment amount
	    expense.setLastClearedAmount(amount);  // Add this line
	    expense.setClearedAmount(newClearedAmount);
	    expense.setClearedBy(clearingMember);
	    expense.setClearedAt(LocalDateTime.now());
	    expense.setCleared(fullyCleared);

	    if (!fullyCleared) {
	        expense.setLastClearedBy(clearingMember);
	        expense.setLastClearedAt(LocalDateTime.now());
	    }

	    return expenseRepository.save(expense);
	}
}
