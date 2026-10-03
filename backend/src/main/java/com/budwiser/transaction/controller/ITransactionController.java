package com.budwiser.transaction.controller;

import com.budwiser.common.response.Response;
import com.budwiser.transaction.dto.TransactionDto;
import com.budwiser.transaction.dto.TransactionQuery;
import com.budwiser.transaction.dto.TransactionRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public interface ITransactionController {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  Response<TransactionDto> create(@Valid @RequestBody TransactionRequest request);

  /** ?from=2026-10-01&to=2026-10-31&type=EXPENSE&categoryId=5&page=0&size=20 */
  @GetMapping
  Response<List<TransactionDto>> list(@Valid @ModelAttribute TransactionQuery query);

  @GetMapping("/{id}")
  Response<TransactionDto> getById(@PathVariable Long id);

  @PutMapping("/{id}")
  Response<TransactionDto> update(@PathVariable Long id, @Valid @RequestBody TransactionRequest request);

  @DeleteMapping("/{id}")
  Response<Void> delete(@PathVariable Long id);
}
