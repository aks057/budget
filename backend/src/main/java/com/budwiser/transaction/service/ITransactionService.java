package com.budwiser.transaction.service;

import com.budwiser.transaction.dto.TransactionDto;
import com.budwiser.transaction.dto.TransactionQuery;
import com.budwiser.transaction.dto.TransactionRequest;
import org.springframework.data.domain.Page;

public interface ITransactionService {

  TransactionDto create(TransactionRequest request);

  TransactionDto getById(Long id);

  Page<TransactionDto> list(TransactionQuery query);

  TransactionDto update(Long id, TransactionRequest request);

  void delete(Long id);
}
