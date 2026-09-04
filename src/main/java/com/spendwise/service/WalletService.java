    package com.spendwise.service;

    import com.spendwise.dto.request.wallet.ActivateRequest;
    import com.spendwise.dto.request.wallet.CreateWalletRequest;
    import com.spendwise.dto.request.wallet.ArchiveRequest;
    import com.spendwise.dto.request.wallet.GetWalletsRequest;
    import com.spendwise.dto.response.wallet.WalletResponse;
    import com.spendwise.entity.User;
    import com.spendwise.entity.Wallet;
    import com.spendwise.enums.WalletStatus;
    import com.spendwise.enums.WalletType;
    import com.spendwise.exception.WalletExceptions.DuplicateWalletException;
    import com.spendwise.exception.WalletExceptions.InvalidWalletException;
    import com.spendwise.exception.WalletExceptions.WalletDoesNotExist;
    import com.spendwise.mapper.WalletMapper;
    import com.spendwise.repository.UserRepository;
    import com.spendwise.repository.WalletRepository;
    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;

    import java.math.BigDecimal;
    import java.util.List;
    import java.util.UUID;

    @Service
    @RequiredArgsConstructor
    @Transactional
    public class WalletService {

        private final WalletRepository walletRepository;
        private final UserRepository userRepository;
        private final WalletMapper walletMapper;
        private final CurrentUserService currentUserService;


        public WalletResponse createWallet(CreateWalletRequest request){

            UUID userId = currentUserService.getCurrentUserId();

            String walletName = request.getWalletName();
            if(walletRepository.existsByWalletNameAndUserId(walletName, userId)){
                throw new DuplicateWalletException(
                        "Wallet with name '" + walletName + "' already exists"
                );
            }

            WalletType walletType;
            try {
                walletType = WalletType.valueOf(request.getType().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidWalletException(
                        "Invalid wallet type: " + request.getType()
                );
            }

            User user = userRepository.getReferenceById(userId);

            BigDecimal initialBalance = request.getInitialBalance() != null
                    ? request.getInitialBalance()
                    : BigDecimal.ZERO;

            Wallet wallet = walletMapper.toEntity(
                    request,
                    user,
                    walletName,
                    walletType,
                    initialBalance,
                    WalletStatus.ACTIVE
            );

            Wallet savedWallet = walletRepository.save(wallet);

            return walletMapper.toResponse(savedWallet);
        }

        public List<WalletResponse> getWallets(){

            UUID userId = currentUserService.getCurrentUserId();

            List<Wallet> wallets = walletRepository.findAllByUserId(userId);


            return walletMapper.toResponseList(wallets);
        }

        public WalletResponse getWallet(GetWalletsRequest request){

            UUID userId = currentUserService.getCurrentUserId();

            Wallet wallet = walletRepository.findByWalletNameAndUserId(request.getWalletName(), userId).orElseThrow(()->new WalletDoesNotExist("Wallet does not exist"));
            return walletMapper.toResponse(wallet);
        }

        public WalletResponse archiveWallet(ArchiveRequest request){

            UUID userId = currentUserService.getCurrentUserId();

            Wallet wallet = walletRepository.findByWalletNameAndUserId(request.getWalletName(), userId).orElseThrow(() -> new WalletDoesNotExist("Wallet not found"));

            wallet.setStatus(WalletStatus.ARCHIVED);
            walletRepository.save(wallet);

            return walletMapper.toResponse(wallet);
        }

        public WalletResponse activateWallet(ActivateRequest request){

            UUID userId = currentUserService.getCurrentUserId();

            Wallet wallet = walletRepository.findByWalletNameAndUserId(request.getWalletName(), userId).orElseThrow(() -> new WalletDoesNotExist("Wallet not found"));

            wallet.setStatus(WalletStatus.ACTIVE);
            return walletMapper.toResponse(wallet);
        }
    }
