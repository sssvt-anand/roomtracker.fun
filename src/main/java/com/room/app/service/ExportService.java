package com.room.app.service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.room.app.dto.Expense;
import com.room.app.dto.ExportHistoryDTO;
import com.room.app.repository.ExpenseRepository;

@Service
public class ExportService {
	private final ExpenseService expenseService;
	private final ExpenseRepository expenseRepository;
	private final ConcurrentLinkedQueue<ExportHistoryDTO> exportHistory = new ConcurrentLinkedQueue<>();
	private static final int MAX_HISTORY_ENTRIES = 50;

	public ExportService(ExpenseService expenseService, ExpenseRepository expenseRepository) {
		this.expenseService = expenseService;
		this.expenseRepository = expenseRepository;
	}

	public ResponseEntity<ByteArrayResource> exportMonthlyExpenses(LocalDate start, LocalDate end) {
		if (start == null)
			start = LocalDate.now().minusMonths(1);
		if (end == null)
			end = LocalDate.now();
		validateDateRange(start, end);

		List<Expense> expenses = getExpensesByDateRange(start, end);
		String filename = String.format("monthly_%s_to_%s.csv", start, end);
		return exportCSV(expenses, filename, "MONTHLY");
	}

	public ResponseEntity<ByteArrayResource> exportYearlyExpenses(LocalDate start, LocalDate end) {
		if (start == null)
			start = LocalDate.now().withDayOfYear(1);
		if (end == null)
			end = LocalDate.now();
		validateDateRange(start, end);

		List<Expense> expenses = getExpensesByDateRange(start, end);
		String filename = String.format("yearly_%s_to_%s.csv", start.getYear(), end.getYear());
		return exportCSV(expenses, filename, "YEARLY");
	}

	private void validateDateRange(LocalDate start, LocalDate end) {
		if (start.isAfter(end)) {
			throw new IllegalArgumentException("Start date must be before end date");
		}
	}

	public List<Expense> getExpensesByDateRange(LocalDate start, LocalDate end) {
	    return expenseRepository.findByDateBetween(start, end);
	}

	public ResponseEntity<ByteArrayResource> exportExpensesByMember(Long memberId) {
		List<Expense> expenses = expenseService.getExpensesByMember(memberId);
		String filename = String.format("member_%d_expenses_%s.csv", memberId,
				LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		return exportCSV(expenses, filename, "MEMBER");
	}

	public ResponseEntity<ByteArrayResource> exportExpensesWithoutMember() {
		List<Expense> expenses = expenseService.getExpensesWithoutMember();
		String filename = String.format("general_expenses_%s.csv",
				LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		return exportCSV(expenses, filename, "GENERAL");
	}

	public List<ExportHistoryDTO> getExportHistory() {
		return new ArrayList<>(exportHistory);
	}

	private ResponseEntity<ByteArrayResource> exportCSV(List<Expense> expenses, String filename, String exportType) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(outputStream);

			// Updated CSV header with cleared information
			writer.println("ID,Description,Amount,Date,Member,Cleared Amount,Cleared By,Cleared At");

			// Write data rows with cleared information
			expenses.forEach(expense -> {
				String memberName = (expense.getMember() != null) ? expense.getMember().getName() : "N/A";

				String clearedBy = (expense.getClearedBy() != null) ? expense.getClearedBy().getName() : "N/A";

				String clearedAt = (expense.getClearedAt() != null)
						? expense.getClearedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
						: "N/A";

				writer.printf("%d,%s,%.2f,%s,%s,%.2f,%s,%s%n", expense.getId(), escapeCsv(expense.getDescription()),
						expense.getAmount().doubleValue(), expense.getDate().toString(), escapeCsv(memberName),
						expense.getClearedAmount() != null ? expense.getClearedAmount().doubleValue() : 0.0,
						escapeCsv(clearedBy), clearedAt);
			});

			writer.flush();
			byte[] csvData = outputStream.toByteArray();
			writer.close();

			// Log export history
			logExport(exportType, filename, csvData.length);

			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
					.contentType(MediaType.parseMediaType("text/csv")).contentLength(csvData.length)
					.body(new ByteArrayResource(csvData));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	private void logExport(String exportType, String filename, int fileSizeBytes) {
		ExportHistoryDTO historyEntry = new ExportHistoryDTO(exportType, LocalDateTime.now(), filename,
				formatFileSize(fileSizeBytes));

		exportHistory.add(historyEntry);

		// Maintain history size limit
		while (exportHistory.size() > MAX_HISTORY_ENTRIES) {
			exportHistory.poll();
		}
	}

	private String formatFileSize(int bytes) {
		if (bytes < 1024)
			return bytes + " B";
		return String.format("%.1f KB", bytes / 1024.0);
	}

	private String escapeCsv(String input) {
		if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
			return "\"" + input.replace("\"", "\"\"") + "\"";
		}
		return input;
	}

}