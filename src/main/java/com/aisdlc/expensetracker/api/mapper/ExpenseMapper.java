package com.aisdlc.expensetracker.api.mapper;

import com.aisdlc.expensetracker.api.dto.ExpenseCreateRequest;
import com.aisdlc.expensetracker.api.dto.ExpenseResponse;
import com.aisdlc.expensetracker.api.dto.ExpenseUpdateRequest;
import com.aisdlc.expensetracker.domain.Expense;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ExpenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Expense toEntity(ExpenseCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ExpenseUpdateRequest request, @MappingTarget Expense expense);

    ExpenseResponse toResponse(Expense expense);
}
