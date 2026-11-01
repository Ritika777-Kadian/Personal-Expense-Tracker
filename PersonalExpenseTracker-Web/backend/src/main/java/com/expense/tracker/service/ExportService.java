package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryExpenseDTO;
import com.expense.tracker.model.Transaction;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ReportService reportService;

    public byte[] exportTransactionsCsv(User user, LocalDate start, LocalDate end) {
        List<Transaction> list = (start != null && end != null) ?
                transactionRepository.findByUserIdAndDateBetweenOrderByDateDescIdDesc(user.getId(), start, end) :
                transactionRepository.findByUserIdOrderByDateDescIdDesc(user.getId());

        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM for Microsoft Excel compatibility
        sb.append('\uFEFF');
        sb.append("ID,Type,Amount,Category,Description,Date\n");

        for (Transaction t : list) {
            sb.append(t.getId()).append(",");
            sb.append(t.getType()).append(",");
            sb.append(t.getAmount().toPlainString()).append(",");
            sb.append(escapeCsvField(t.getCategory())).append(",");
            sb.append(escapeCsvField(t.getDescription())).append(",");
            sb.append(t.getDate()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportCategoryReportCsv(User user, LocalDate start, LocalDate end) {
        List<CategoryExpenseDTO> list = reportService.getCategoryBreakdown(user, start, end);

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Category,Expense Amount,Percentage,Transaction Count\n");

        for (CategoryExpenseDTO dto : list) {
            sb.append(escapeCsvField(dto.getCategory())).append(",");
            sb.append(dto.getAmount().toPlainString()).append(",");
            sb.append(dto.getPercentage()).append("%,");
            sb.append(dto.getCount()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsvField(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
