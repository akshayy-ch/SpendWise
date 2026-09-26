package com.spendwise.exception;

import com.spendwise.dto.error.ErrorResponse;
import com.spendwise.exception.AuthExceptions.EmailAlreadyExistsException;
import com.spendwise.exception.AuthExceptions.PhoneNoAlreadyExistsException;
import com.spendwise.exception.AuthExceptions.UsernameAlreadyExistsException;
import com.spendwise.exception.BudgetExceptions.*;
import com.spendwise.exception.CategoryExceptions.*;
import com.spendwise.exception.EmailExceptions.EmailNotVerifiedException;
import com.spendwise.exception.EmailExceptions.EmailVerificationTokenAlreadyUsedException;
import com.spendwise.exception.EmailExceptions.EmailVerificationTokenExpiredException;
import com.spendwise.exception.EmailExceptions.EmailVerificationTokenInvalidException;
import com.spendwise.exception.ExpenseException.*;
import com.spendwise.exception.Income.InvalidIncomeSortFieldException;
import com.spendwise.exception.NotificationExceptions.InvalidNotificationSortFieldException;
import com.spendwise.exception.NotificationExceptions.NotificationDoesNotExistException;
import com.spendwise.exception.PaginationException.InvalidPaginationException;
import com.spendwise.exception.UserExceptions.InvalidRoleChangeException;
import com.spendwise.exception.WalletExceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.spendwise.exception.GroupExceptions.*;
import com.spendwise.exception.GroupMemberExceptions.*;
import com.spendwise.exception.ExpenseShareExceptions.*;
import com.spendwise.exception.SettlementExceptions.*;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler{

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler({UsernameAlreadyExistsException.class, EmailAlreadyExistsException.class,
            PhoneNoAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException e){
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        Map<String, String> map = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            map.put(error.getField(), error.getDefaultMessage());
        }
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation Failed")
                .timestamp(LocalDateTime.now())
                .errors(map)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
    @ExceptionHandler(WalletDoesNotExist.class)
    public ResponseEntity<ErrorResponse> handleWalletDoesNotExist(WalletDoesNotExist e){
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler(ArchivedWalletException.class)
    public ResponseEntity<ErrorResponse> handleArchivedWalletException(ArchivedWalletException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DuplicateWalletException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateWalletException(DuplicateWalletException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmountException(InvalidAmountException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidWalletException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWalletException(InvalidWalletException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(CategoryDoesNotExist.class)
    public ResponseEntity<ErrorResponse> handleCategoryDoesNotExist(CategoryDoesNotExist e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<ErrorResponse> handleCategoryInUse(CategoryInUseException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCategory(DuplicateCategoryException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCategory(InvalidCategoryException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(SystemCategoryException.class)
    public ResponseEntity<ErrorResponse> handleSystemCategory(SystemCategoryException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException e){
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(GroupDoesNotExist.class)
    public ResponseEntity<ErrorResponse> handleGroupDoesNotExist(GroupDoesNotExist e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ArchivedGroupException.class)
    public ResponseEntity<ErrorResponse> handleArchivedGroupException(ArchivedGroupException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedGroupActionException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedGroupAction(UnauthorizedGroupActionException e) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(GroupMemberDoesNotExist.class)
    public ResponseEntity<ErrorResponse> handleGroupMemberDoesNotExist(GroupMemberDoesNotExist e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NotGroupMemberException.class)
    public ResponseEntity<ErrorResponse> handleNotGroupMember(NotGroupMemberException e) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(InactiveGroupMemberException.class)
    public ResponseEntity<ErrorResponse> handleInactiveGroupMember(InactiveGroupMemberException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(OutStandingBalanceException.class)
    public ResponseEntity<ErrorResponse> handleOutstandingBalance(OutStandingBalanceException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ExpenseShareDoesNotExist.class)
    public ResponseEntity<ErrorResponse> handleExpenseShareDoesNotExist(ExpenseShareDoesNotExist e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(AlreadySettledException.class)
    public ResponseEntity<ErrorResponse> handleAlreadySettled(AlreadySettledException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidSplitException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSplit(InvalidSplitException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(SettlementDoesNotExist.class)
    public ResponseEntity<ErrorResponse> handleSettlementDoesNotExist(SettlementDoesNotExist e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InvalidSettlementException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSettlement(InvalidSettlementException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(UserDoesNotExist.class)
    public ResponseEntity<ErrorResponse> handleUserDoesNotExist(UserDoesNotExist e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ExpenseDoesNotExist.class)
    public ResponseEntity<ErrorResponse> handleExpenseDoesNotExist(ExpenseDoesNotExist e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler(VoidedExpenseException.class)
    public ResponseEntity<ErrorResponse> handleVoidedExpenseException(VoidedExpenseException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ReceiverNotFound.class)
    public ResponseEntity<ErrorResponse> handleReceiverNotFound(ReceiverNotFound e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedExpenseActionException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedExpenseActionException(UnauthorizedExpenseActionException e) {

        return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(DuplicateGroupMemberException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateGroupMemberException(DuplicateGroupMemberException e){
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BudgetAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBudgetAlreadyExistsException(BudgetAlreadyExistsException e){
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BudgetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBudgetNotFoundException(BudgetNotFoundException e){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidBudgetLimitException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBudgetLimitException(InvalidBudgetLimitException e){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidStartandEndDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStartandEndDateException(InvalidStartandEndDateException e){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(DuplicateBudgetCategoryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateBudgetCategoryException(DuplicateBudgetCategoryException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }
    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaginationException(InvalidPaginationException e){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    @ExceptionHandler(InvalidRoleChangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRoleChangeException(InvalidRoleChangeException e){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    @ExceptionHandler(EmailVerificationTokenAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleEmailVerificationTokenAlreadyUsed(EmailVerificationTokenAlreadyUsedException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(EmailVerificationTokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handleEmailVerificationTokenInvalid(EmailVerificationTokenInvalidException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(EmailVerificationTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleEmailVerificationTokenExpired(EmailVerificationTokenExpiredException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }
    @ExceptionHandler(NotificationDoesNotExistException.class)
    public ResponseEntity<ErrorResponse> handleNotificationDoesNotExistException(NotificationDoesNotExistException e){
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler(InvalidNotificationSortFieldException.class)
    public ResponseEntity<ErrorResponse> handleInvalidNotificationSortFieldException(InvalidNotificationSortFieldException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    @ExceptionHandler(InvalidExpenseSortFieldException.class)
    public ResponseEntity<ErrorResponse> handleInvalidExpenseSortFieldException(InvalidExpenseSortFieldException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    @ExceptionHandler(InvalidIncomeSortFieldException.class)
    public ResponseEntity<ErrorResponse> handleInvalidIncomeSortFieldException(InvalidIncomeSortFieldException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    @ExceptionHandler(InvalidSettlementSortFieldException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSettlementSortFieldException(InvalidSettlementSortFieldException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
