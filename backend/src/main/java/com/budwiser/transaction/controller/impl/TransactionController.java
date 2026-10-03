package com.budwiser.transaction.controller.impl;

import com.budwiser.common.response.PageMeta;
import com.budwiser.common.response.Response;
import com.budwiser.transaction.controller.ITransactionController;
import com.budwiser.transaction.dto.TransactionDto;
import com.budwiser.transaction.dto.TransactionQuery;
import com.budwiser.transaction.dto.TransactionRequest;
import com.budwiser.transaction.service.ITransactionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionController implements ITransactionController {
  private final ITransactionService transactionService;

  @Override
  public Response<TransactionDto> create(TransactionRequest request) {
    return Response.<TransactionDto>builder().data(transactionService.create(request)).build();
  }

  @Override
  public Response<List<TransactionDto>> list(TransactionQuery query) {
    Page<TransactionDto> page = transactionService.list(query);
    return Response.<List<TransactionDto>>builder().data(page.getContent()).page(PageMeta.from(page)).build();
  }

  @Override
  public Response<TransactionDto> getById(Long id) {
    return Response.<TransactionDto>builder().data(transactionService.getById(id)).build();
  }

  @Override
  public Response<TransactionDto> update(Long id, TransactionRequest request) {
    return Response.<TransactionDto>builder().data(transactionService.update(id, request)).build();
  }

  @Override
  public Response<Void> delete(Long id) {
    transactionService.delete(id);
    return Response.<Void>builder().build();
  }
}
