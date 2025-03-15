package com.room.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PaymentHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "expense_id")
	private Expense expense;

	private BigDecimal amount;

	@ManyToOne
	@JoinColumn(name = "member_id")
	private Member clearedBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Expense getExpense() {
		return expense;
	}

	public void setExpense(Expense expense) {
		this.expense = expense;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Member getClearedBy() {
		return clearedBy;
	}

	public void setClearedBy(Member clearedBy) {
		this.clearedBy = clearedBy;
	}

	public LocalDateTime getClearedAt() {
		return clearedAt;
	}

	public void setClearedAt(LocalDateTime clearedAt) {
		this.clearedAt = clearedAt;
	}

	private LocalDateTime clearedAt;

}
