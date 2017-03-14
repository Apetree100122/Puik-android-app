package piuk.blockchain.android.ui.account;

import android.app.Application;
import android.content.Context;

import info.blockchain.wallet.payload.PayloadManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import piuk.blockchain.android.BlockchainTestApplication;
import piuk.blockchain.android.BuildConfig;
import piuk.blockchain.android.data.datamanagers.AccountEditDataManager;
import piuk.blockchain.android.injection.ApiModule;
import piuk.blockchain.android.injection.ApplicationModule;
import piuk.blockchain.android.injection.DataManagerModule;
import piuk.blockchain.android.injection.Injector;
import piuk.blockchain.android.injection.InjectorTestUtils;
import piuk.blockchain.android.ui.swipetoreceive.SwipeToReceiveHelper;
import piuk.blockchain.android.ui.transactions.PayloadDataManager;
import piuk.blockchain.android.util.ExchangeRateFactory;
import piuk.blockchain.android.util.PrefsUtil;
import piuk.blockchain.android.util.StringUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@SuppressWarnings("PrivateMemberAccessBetweenOuterAndInnerClass")
@Config(sdk = 23, constants = BuildConfig.class, application = BlockchainTestApplication.class)
@RunWith(RobolectricTestRunner.class)
public class AccountEditViewModelTest {

//    private AccountEditViewModel subject;
//    @Mock AccountEditViewModel.DataListener activity;
//    @Mock PayloadDataManager payloadDataManager;
//    @Mock PrefsUtil prefsUtil;
//    @Mock StringUtils stringUtils;
//    @Mock AccountEditDataManager accountEditDataManager;
//    @Mock ExchangeRateFactory exchangeRateFactory;
//    @Mock AccountEditModel accountEditModel;
//    @Mock SwipeToReceiveHelper swipeToReceiveHelper;
//
//    @Before
//    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this);
//
//        InjectorTestUtils.initApplicationComponent(
//                Injector.getInstance(),
//                new MockApplicationModule(RuntimeEnvironment.application),
//                new ApiModule(),
//                new MockDataManagerModule());
//
//        subject = new AccountEditViewModel(accountEditModel, activity);
//    }
//
//
//    @Test
//    public void setAccountModel() throws Exception {
//        // Arrange
//        AccountEditModel newModel = new AccountEditModel(mock(Context.class));
//        // Act
//        subject.setAccountModel(newModel);
//        // Assert
//        assertEquals(newModel, subject.accountModel);
//    }

    //
//    @SuppressWarnings("WrongConstant")
//    @Test
//    public void onViewReadyV3() throws Exception {
//        // Arrange
//        Intent intent = new Intent();
//        intent.putExtra("account_index", 0);
//        when(activity.getIntent()).thenReturn(intent);
//        Payload mockPayload = mock(Payload.class, RETURNS_DEEP_STUBS);
//        Account importedAccount = new ConsolidatedAccount();
//        Account account = new Account();
//        when(mockPayload.getHdWallet().getAccounts()).thenReturn(Arrays.asList(account, importedAccount));
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        when(stringUtils.getString(anyInt())).thenReturn("string resource");
//        // Act
//        subject.onViewReady();
//        // Assert
//        verify(activity).getIntent();
//        verify(accountEditModel).setLabel(anyString());
//        verify(accountEditModel).setLabelHeader("string resource");
//        verify(accountEditModel).setScanPrivateKeyVisibility(anyInt());
//        verify(accountEditModel).setXpubText("string resource");
//        verify(accountEditModel).setTransferFundsVisibility(anyInt());
//    }
//
//    @SuppressWarnings("WrongConstant")
//    @Test
//    public void onViewReadyV3Archived() throws Exception {
//        // Arrange
//        Intent intent = new Intent();
//        intent.putExtra("account_index", 0);
//        when(activity.getIntent()).thenReturn(intent);
//        Payload mockPayload = mock(Payload.class, RETURNS_DEEP_STUBS);
//        Account importedAccount = new ConsolidatedAccount();
//        Account account = new Account();
//        account.setArchived(true);
//        when(mockPayload.getHdWallet().getAccounts()).thenReturn(Arrays.asList(account, importedAccount));
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        when(stringUtils.getString(anyInt())).thenReturn("string resource");
//        // Act
//        subject.onViewReady();
//        // Assert
//        verify(activity).getIntent();
//        verify(accountEditModel).setLabel(anyString());
//        verify(accountEditModel).setLabelHeader("string resource");
//        verify(accountEditModel).setScanPrivateKeyVisibility(anyInt());
//        verify(accountEditModel).setXpubText("string resource");
//        verify(accountEditModel).setTransferFundsVisibility(anyInt());
//    }
//
//    @SuppressWarnings("WrongConstant")
//    @Test
//    public void onViewReadyV2() throws Exception {
//        // Arrange
//        Intent intent = new Intent();
//        intent.putExtra("address_index", 0);
//        when(activity.getIntent()).thenReturn(intent);
//        Payload mockPayload = mock(Payload.class, RETURNS_DEEP_STUBS);
//        LegacyAddress legacyAddress = new LegacyAddress();
//        when(mockPayload.getLegacyAddressList()).thenReturn(Collections.singletonList(legacyAddress));
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        when(mockPayload.getHdWallet().getAccounts().get(anyInt())).thenReturn(mock(Account.class));
//        when(stringUtils.getString(anyInt())).thenReturn("string resource");
//        // Act
//        subject.onViewReady();
//        // Assert
//        verify(activity).getIntent();
//        verify(accountEditModel).setLabel(isNull());
//        verify(accountEditModel).setLabelHeader("string resource");
//        verify(accountEditModel).setScanPrivateKeyVisibility(anyInt());
//        verify(accountEditModel).setXpubText("string resource");
//        verify(accountEditModel).setTransferFundsVisibility(anyInt());
//    }
//
//    @SuppressWarnings("WrongConstant")
//    @Test
//    public void onViewReadyV2WatchOnlyUpgraded() throws Exception {
//        // Arrange
//        Intent intent = new Intent();
//        intent.putExtra("address_index", 0);
//        when(activity.getIntent()).thenReturn(intent);
//        Payload mockPayload = mock(Payload.class, RETURNS_DEEP_STUBS);
//        LegacyAddress legacyAddress = new LegacyAddress();
//        legacyAddress.setWatchOnly(true);
//        when(mockPayload.getLegacyAddressList()).thenReturn(Collections.singletonList(legacyAddress));
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        when(mockPayload.isUpgraded()).thenReturn(true);
//        when(mockPayload.getHdWallet().getAccounts().get(anyInt())).thenReturn(mock(Account.class));
//        when(stringUtils.getString(anyInt())).thenReturn("string resource");
//        // Act
//        subject.onViewReady();
//        // Assert
//        verify(activity).getIntent();
//        verify(accountEditModel).setLabel(isNull());
//        verify(accountEditModel).setLabelHeader("string resource");
//        verify(accountEditModel).setScanPrivateKeyVisibility(anyInt());
//        verify(accountEditModel).setXpubText("string resource");
//        verify(accountEditModel).setTransferFundsVisibility(anyInt());
//    }
//
//    @Test
//    public void onClickTransferFundsSuccess() throws Exception {
//        // Arrange
//        PendingTransaction pendingTransaction = new PendingTransaction();
//        pendingTransaction.bigIntAmount = new BigInteger("1");
//        pendingTransaction.bigIntFee = new BigInteger("1");
//        SpendableUnspentOutputs spendableUnspentOutputs = new SpendableUnspentOutputs();
//        MyTransactionOutPoint mockTransactionOutPoint = mock(MyTransactionOutPoint.class);
//        spendableUnspentOutputs.setSpendableOutputs(Collections.singletonList(mockTransactionOutPoint));
//        pendingTransaction.unspentOutputBundle = spendableUnspentOutputs;
//        pendingTransaction.sendingObject = mock(ItemAccount.class);
//        when(accountEditDataManager.getPendingTransactionForLegacyAddress(any(LegacyAddress.class), any(Payment.class)))
//                .thenReturn(Observable.just(pendingTransaction));
//        when(prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY)).thenReturn("USD");
//        subject.legacyAddress = new LegacyAddress();
//        // Act
//        subject.onClickTransferFunds();
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).showPaymentDetails(any(PaymentConfirmationDetails.class), eq(pendingTransaction));
//    }
//
//    @Test
//    public void onClickTransferFundsSuccessTransactionEmpty() throws Exception {
//        // Arrange
//        subject.legacyAddress = new LegacyAddress();
//        PendingTransaction pendingTransaction = new PendingTransaction();
//        pendingTransaction.bigIntAmount = new BigInteger("0");
//        when(accountEditDataManager.getPendingTransactionForLegacyAddress(any(LegacyAddress.class), any(Payment.class)))
//                .thenReturn(Observable.just(pendingTransaction));
//        // Act
//        subject.onClickTransferFunds();
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void onClickTransferFundsError() throws Exception {
//        // Arrange
//        subject.legacyAddress = new LegacyAddress();
//        when(accountEditDataManager.getPendingTransactionForLegacyAddress(any(LegacyAddress.class), any(Payment.class)))
//                .thenReturn(Observable.error(new Throwable()));
//        // Act
//        subject.onClickTransferFunds();
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void transferFundsClickable() throws Exception {
//        // Arrange
//        when(accountEditModel.getTransferFundsClickable()).thenReturn(false);
//        // Act
//        boolean value = subject.transferFundsClickable();
//        // Assert
//        assertFalse(value);
//    }
//
//    @Test
//    public void submitPaymentSuccess() throws Exception {
//        // Arrange
//        PendingTransaction pendingTransaction = new PendingTransaction();
//        pendingTransaction.bigIntAmount = new BigInteger("1");
//        pendingTransaction.bigIntFee = new BigInteger("1");
//        LegacyAddress legacyAddress = new LegacyAddress();
//        pendingTransaction.sendingObject = new ItemAccount("", "", "", null, legacyAddress);
//        pendingTransaction.unspentOutputBundle = new SpendableUnspentOutputs();
//        Payload mockPayload = mock(Payload.class, RETURNS_DEEP_STUBS);
//        when(mockPayload.isDoubleEncrypted()).thenReturn(false);
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        when(accountEditDataManager.submitPayment(
//                any(SpendableUnspentOutputs.class),
//                anyList(),
//                isNull(),
//                isNull(),
//                any(BigInteger.class),
//                any(BigInteger.class))).thenReturn(Observable.just("hash"));
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(true));
//        // Act
//        subject.submitPayment(pendingTransaction);
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).showTransactionSuccess();
//        //noinspection WrongConstant
//        verify(accountEditModel).setTransferFundsVisibility(anyInt());
//        verify(activity).setActivityResult(anyInt());
//    }
//
//    @Test
//    public void submitPaymentFailed() throws Exception {
//        // Arrange
//        PendingTransaction pendingTransaction = new PendingTransaction();
//        pendingTransaction.bigIntAmount = new BigInteger("1");
//        pendingTransaction.bigIntFee = new BigInteger("1");
//        LegacyAddress legacyAddress = new LegacyAddress();
//        pendingTransaction.sendingObject = new ItemAccount("", "", "", null, legacyAddress);
//        pendingTransaction.unspentOutputBundle = new SpendableUnspentOutputs();
//        Payload mockPayload = mock(Payload.class, RETURNS_DEEP_STUBS);
//        when(mockPayload.isDoubleEncrypted()).thenReturn(false);
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        when(accountEditDataManager.submitPayment(
//                any(SpendableUnspentOutputs.class),
//                anyList(),
//                isNull(),
//                isNull(),
//                any(BigInteger.class),
//                any(BigInteger.class))).thenReturn(Observable.error(new Throwable()));
//        // Act
//        subject.submitPayment(pendingTransaction);
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void submitPaymentECKeyException() throws Exception {
//        // Arrange
//        PendingTransaction pendingTransaction = new PendingTransaction();
//        pendingTransaction.bigIntAmount = new BigInteger("1");
//        pendingTransaction.bigIntFee = new BigInteger("1");
//        LegacyAddress legacyAddress = new LegacyAddress();
//        pendingTransaction.sendingObject = new ItemAccount("", "", "", null, legacyAddress);
//        Payload mockPayload = mock(Payload.class, RETURNS_DEEP_STUBS);
//        when(mockPayload.isDoubleEncrypted()).thenReturn(true);
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        // Act
//        subject.submitPayment(pendingTransaction);
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).dismissProgressDialog();
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//        verifyZeroInteractions(accountEditDataManager);
//    }
//
//    @Test
//    public void updateAccountLabelInvalid() throws Exception {
//        // Arrange
//
//        // Act
//        subject.updateAccountLabel("    ");
//        // Assert
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void updateAccountLabelSuccess() throws Exception {
//        // Arrange
//        subject.account = new Account();
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(true));
//        // Act
//        subject.updateAccountLabel("label");
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).dismissProgressDialog();
//        verify(accountEditModel).setLabel(anyString());
//        verify(activity).setActivityResult(anyInt());
//    }
//
//    @Test
//    public void updateAccountLabelFailed() throws Exception {
//        // Arrange
//        subject.legacyAddress = new LegacyAddress();
//        subject.legacyAddress.setLabel("old label");
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(false));
//        // Act
//        subject.updateAccountLabel("new label");
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).dismissProgressDialog();
//        verify(accountEditModel).setLabel("old label");
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void updateAccountLabelError() throws Exception {
//        // Arrange
//        subject.legacyAddress = new LegacyAddress();
//        subject.legacyAddress.setLabel("old label");
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.error(new Throwable()));
//        // Act
//        subject.updateAccountLabel("new label");
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).dismissProgressDialog();
//        verify(accountEditModel).setLabel("old label");
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void onClickChangeLabel() throws Exception {
//        // Arrange
//        when(accountEditModel.getLabel()).thenReturn("label");
//        // Act
//        subject.onClickChangeLabel(null);
//        // Assert
//        verify(activity).promptAccountLabel("label");
//    }
//
//    @Test
//    public void onClickDefaultSuccess() throws Exception {
//        // Arrange
//        subject.account = new Account();
//        Payload mockPayload = mock(Payload.class, RETURNS_DEEP_STUBS);
//        when(mockPayload.getHdWallet().getDefaultIndex()).thenReturn(0);
//        when(mockPayload.getHdWallet().getAccounts()).thenReturn(Collections.singletonList(new Account()));
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(true));
//        // Act
//        subject.onClickDefault(null);
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).dismissProgressDialog();
//        verify(activity).setActivityResult(anyInt());
//        verify(activity).updateAppShortcuts();
//        verify(swipeToReceiveHelper).updateAndStoreAddresses();
//    }
//
//    @Test
//    public void onClickDefaultFailure() throws Exception {
//        // Arrange
//        subject.account = new Account();
//        Payload mockPayload = mock(Payload.class, RETURNS_DEEP_STUBS);
//        when(mockPayload.getHdWallet().getDefaultIndex()).thenReturn(0);
//        when(mockPayload.getHdWallet().getAccounts()).thenReturn(Collections.singletonList(new Account()));
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(false));
//        // Act
//        subject.onClickDefault(null);
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).dismissProgressDialog();
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void onClickDefaultError() throws Exception {
//        // Arrange
//        subject.account = new Account();
//        Payload mockPayload = mock(Payload.class, RETURNS_DEEP_STUBS);
//        when(mockPayload.getHdWallet().getDefaultIndex()).thenReturn(0);
//        when(mockPayload.getHdWallet().getAccounts()).thenReturn(Collections.singletonList(new Account()));
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.error(new Throwable()));
//        // Act
//        subject.onClickDefault(null);
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).dismissProgressDialog();
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void onClickScanXpriv() throws Exception {
//        // Arrange
//        subject.legacyAddress = new LegacyAddress();
//        Payload mockPayload = mock(Payload.class);
//        when(mockPayload.isDoubleEncrypted()).thenReturn(false);
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        // Act
//        subject.onClickScanXpriv(null);
//        // Assert
//        verify(activity).startScanActivity();
//    }
//
//    @Test
//    public void onClickScanXprivDoubleEncrypted() throws Exception {
//        // Arrange
//        subject.legacyAddress = new LegacyAddress();
//        Payload mockPayload = mock(Payload.class);
//        when(mockPayload.isDoubleEncrypted()).thenReturn(true);
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        when(stringUtils.getString(R.string.watch_only_spend_instructionss)).thenReturn("%1$s");
//        // Act
//        subject.onClickScanXpriv(null);
//        // Assert
//        verify(activity).promptPrivateKey(anyString());
//    }
//
//    @Test
//    public void onClickShowXpubAccount() throws Exception {
//        // Arrange
//        subject.account = new Account();
//        // Act
//        subject.onClickShowXpub(null);
//        // Assert
//        verify(activity).showXpubSharingWarning();
//    }
//
//    @Test
//    public void onClickShowXpubLegacyAddress() throws Exception {
//        // Arrange
//        subject.legacyAddress = new LegacyAddress();
//        // Act
//        subject.onClickShowXpub(null);
//        // Assert
//        verify(activity).showAddressDetails(isNull(), isNull(), isNull(), isNull(), isNull());
//    }
//
//    @Test
//    public void onClickArchive() throws Exception {
//        // Arrange
//        subject.account = new Account();
//        when(stringUtils.getString(anyInt())).thenReturn("resource string");
//        // Act
//        subject.onClickArchive(null);
//        // Assert
//        verify(activity).promptArchive("resource string", "resource string");
//    }
//
//    @Test
//    public void showAddressDetails() throws Exception {
//        // Arrange
//        subject.legacyAddress = new LegacyAddress();
//        // Act
//        subject.showAddressDetails();
//        // Assert
//        verify(activity).showAddressDetails(isNull(), isNull(), isNull(), isNull(), isNull());
//    }
//
//    @Test
//    public void handleIncomingScanIntentInvalidData() throws Exception {
//        // Arrange
//        Intent intent = new Intent();
//        intent.putExtra(CaptureActivity.SCAN_RESULT, (String[]) null);
//        // Act
//        subject.handleIncomingScanIntent(intent);
//        // Assert
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void handleIncomingScanIntentUnrecognisedKeyFormat() throws Exception {
//        // Arrange
//        Intent intent = new Intent();
//        intent.putExtra(CaptureActivity.SCAN_RESULT, "6PRJmkckxBct8jUwn6UcJbickdrnXBiPP9JkNW83g4VyFNsfEuxas39pS");
//        // Act
//        subject.handleIncomingScanIntent(intent);
//        // Assert
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void handleIncomingScanIntentBip38() throws Exception {
//        // Arrange
//        Intent intent = new Intent();
//        intent.putExtra(CaptureActivity.SCAN_RESULT, "6PRJmkckxBct8jUwn6UcJbickdrnXBiPP9JkNW83g4VyFNsfEuxas39pSS");
//        // Act
//        subject.handleIncomingScanIntent(intent);
//        // Assert
//        verify(activity).promptBIP38Password(anyString());
//    }
//
//    @Test
//    public void handleIncomingScanIntentNonBip38NoKey() throws Exception {
//        // Arrange
//        Intent intent = new Intent();
//        intent.putExtra(CaptureActivity.SCAN_RESULT, "L1FQxC7wmmRNNe2YFPNXscPq3kaheiA4T7SnTr7vYSBW7Jw1A7PD");
//        // Act
//        subject.handleIncomingScanIntent(intent);
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).dismissProgressDialog();
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void setSecondPassword() throws Exception {
//        // Arrange
//
//        // Act
//        subject.setSecondPassword("password");
//        // Assert
//        assertEquals("password", subject.secondPassword);
//    }
//
//    @Test
//    public void archiveAccountSuccess() throws Exception {
//        // Arrange
//        subject.account = new Account();
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(true));
//        when(accountEditDataManager.updateBalancesAndTransactions()).thenReturn(Completable.complete());
//        // Act
//        subject.archiveAccount();
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).dismissProgressDialog();
//        verify(accountEditDataManager).updateBalancesAndTransactions();
//        verify(activity).setActivityResult(anyInt());
//    }
//
//    @Test
//    public void archiveAccountFailed() throws Exception {
//        // Arrange
//        subject.account = new Account();
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(false));
//        // Act
//        subject.archiveAccount();
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).dismissProgressDialog();
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void archiveAccountError() throws Exception {
//        // Arrange
//        subject.account = new Account();
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.error(new Throwable()));
//        // Act
//        subject.archiveAccount();
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        verify(activity).dismissProgressDialog();
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void importBIP38AddressError() throws Exception {
//        // Arrange
//
//        // Act
//        subject.importBIP38Address("", "");
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//        verify(activity).dismissProgressDialog();
//    }
//
//    @Test
//    public void importBIP38AddressValidAddressEmptyKey() throws Exception {
//        // Arrange
//
//        // Act
//        subject.importBIP38Address("6PRJmkckxBct8jUwn6UcJbickdrnXBiPP9JkNW83g4VyFNsfEuxas39pSS", "");
//        // Assert
//        verify(activity).showProgressDialog(anyInt());
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//        verify(activity).dismissProgressDialog();
//    }
//
//    @SuppressWarnings("WrongConstant")
//    @Test
//    public void importAddressPrivateKeySuccessMatchesIntendedAddressNoDoubleEncryption() throws Exception {
//        // Arrange
//        Payload mockPayload = mock(Payload.class);
//        when(mockPayload.isDoubleEncrypted()).thenReturn(false);
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        ECKey mockEcKey = mock(ECKey.class);
//        when(mockEcKey.getPrivKeyBytes()).thenReturn("privkey".getBytes());
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(true));
//        // Act
//        subject.importAddressPrivateKey(mockEcKey, new LegacyAddress(), true);
//        // Assert
//        verify(activity).setActivityResult(anyInt());
//        verify(accountEditModel).setScanPrivateKeyVisibility(anyInt());
//        verify(accountEditModel).setArchiveVisibility(anyInt());
//        verify(activity).privateKeyImportSuccess();
//    }
//
//    @SuppressWarnings("WrongConstant")
//    @Test
//    public void importAddressPrivateKeySuccessNoAddressMatchDoubleEncryption() throws Exception {
//        // Arrange
//        subject.setSecondPassword("password");
//        Payload mockPayload = mock(Payload.class);
//        when(mockPayload.isDoubleEncrypted()).thenReturn(true);
//        Options mockOptions = mock(Options.class);
//        when(mockOptions.getIterations()).thenReturn(1);
//        when(mockPayload.getOptions()).thenReturn(mockOptions);
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        ECKey mockEcKey = mock(ECKey.class);
//        when(mockEcKey.getPrivKeyBytes()).thenReturn("privkey".getBytes());
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(true));
//        // Act
//        subject.importAddressPrivateKey(mockEcKey, new LegacyAddress(), false);
//        // Assert
//        verify(activity).setActivityResult(anyInt());
//        verify(accountEditModel).setScanPrivateKeyVisibility(anyInt());
//        verify(accountEditModel).setArchiveVisibility(anyInt());
//        verify(activity).privateKeyImportMismatch();
//    }
//
//    @Test
//    public void importAddressPrivateKeyFailed() throws Exception {
//        // Arrange
//        subject.setSecondPassword("password");
//        Payload mockPayload = mock(Payload.class);
//        when(mockPayload.isDoubleEncrypted()).thenReturn(true);
//        Options mockOptions = mock(Options.class);
//        when(mockOptions.getIterations()).thenReturn(1);
//        when(mockPayload.getOptions()).thenReturn(mockOptions);
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        ECKey mockEcKey = mock(ECKey.class);
//        when(mockEcKey.getPrivKeyBytes()).thenReturn("privkey".getBytes());
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(false));
//        // Act
//        subject.importAddressPrivateKey(mockEcKey, new LegacyAddress(), false);
//        // Assert
//        //noinspection WrongConstant
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//    }
//
//    @Test
//    public void importUnmatchedPrivateKeyFoundInPayloadSuccess() throws Exception {
//        // Arrange
//        Payload mockPayload = mock(Payload.class);
//        when(mockPayload.isDoubleEncrypted()).thenReturn(false);
//        List<String> legacyStrings = Arrays.asList("addr0", "addr1", "addr2");
//        when(mockPayload.getLegacyAddressStringList()).thenReturn(legacyStrings);
//        List<LegacyAddress> legacyAddresses = Collections.singletonList(new LegacyAddress("", 0L, "addr0", "", 0L, "", ""));
//        when(mockPayload.getLegacyAddressList()).thenReturn(legacyAddresses);
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        ECKey mockEcKey = mock(ECKey.class);
//        when(mockEcKey.getPrivKeyBytes()).thenReturn("privkey".getBytes());
//        org.bitcoinj.core.Address mockAddress = mock(org.bitcoinj.core.Address.class);
//        when(mockAddress.toString()).thenReturn("addr0");
//        when(mockEcKey.toAddress(any(NetworkParameters.class))).thenReturn(mockAddress);
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(true));
//        // Act
//        subject.importUnmatchedPrivateKey(mockEcKey);
//        // Assert
//        verify(activity).setActivityResult(anyInt());
//        //noinspection WrongConstant
//        verify(accountEditModel).setScanPrivateKeyVisibility(anyInt());
//        verify(activity).privateKeyImportMismatch();
//    }
//
//    @Test
//    public void importUnmatchedPrivateNotFoundInPayloadSuccess() throws Exception {
//        // Arrange
//        Payload mockPayload = mock(Payload.class);
//        when(mockPayload.isDoubleEncrypted()).thenReturn(false);
//        when(mockPayload.getLegacyAddressList()).thenReturn(new ArrayList<>());
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        ECKey mockEcKey = mock(ECKey.class);
//        when(mockEcKey.getPrivKeyBytes()).thenReturn("privkey".getBytes());
//        org.bitcoinj.core.Address mockAddress = mock(org.bitcoinj.core.Address.class);
//        when(mockAddress.toString()).thenReturn("addr0");
//        when(mockEcKey.toAddress(any(NetworkParameters.class))).thenReturn(mockAddress);
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(true));
//        // Act
//        subject.importUnmatchedPrivateKey(mockEcKey);
//        // Assert
//        verify(activity).setActivityResult(anyInt());
//        verify(activity).sendBroadcast(anyString(), anyString());
//        //noinspection WrongConstant
//        verify(activity).privateKeyImportMismatch();
//    }
//
//    @SuppressWarnings("WrongConstant")
//    @Test
//    public void importUnmatchedPrivateNotFoundInPayloadFailure() throws Exception {
//        // Arrange
//        Payload mockPayload = mock(Payload.class);
//        when(mockPayload.isDoubleEncrypted()).thenReturn(false);
//        when(mockPayload.getLegacyAddressList()).thenReturn(new ArrayList<>());
//        when(payloadDataManager.getPayload()).thenReturn(mockPayload);
//        ECKey mockEcKey = mock(ECKey.class);
//        when(mockEcKey.getPrivKeyBytes()).thenReturn("privkey".getBytes());
//        org.bitcoinj.core.Address mockAddress = mock(org.bitcoinj.core.Address.class);
//        when(mockAddress.toString()).thenReturn("addr0");
//        when(mockEcKey.toAddress(any(NetworkParameters.class))).thenReturn(mockAddress);
//        when(accountEditDataManager.syncPayloadWithServer()).thenReturn(Observable.just(false));
//        // Act
//        subject.importUnmatchedPrivateKey(mockEcKey);
//        // Assert
//        verify(activity).showToast(anyInt(), eq(ToastCustom.TYPE_ERROR));
//        verify(activity).privateKeyImportMismatch();
//    }
//
//    private class MockApplicationModule extends ApplicationModule {
//        public MockApplicationModule(Application application) {
//            super(application);
//        }
//
//        @Override
//        protected StringUtils provideStringUtils() {
//            return stringUtils;
//        }
//
//        @Override
//        protected PrefsUtil providePrefsUtil() {
//            return prefsUtil;
//        }
//
//        @Override
//        protected ExchangeRateFactory provideExchangeRateFactory() {
//            return exchangeRateFactory;
//        }
//    }
//
//    private class MockDataManagerModule extends DataManagerModule {
//
//        @Override
//        protected PayloadDataManager provideTransactionHelper(PayloadManager payloadManager) {
//            return payloadDataManager;
//        }
//
//        @Override
//        protected SwipeToReceiveHelper provideSwipeToReceiveHelper(PayloadManager payloadManager, PrefsUtil prefsUtil) {
//            return swipeToReceiveHelper;
//        }
//    }
}