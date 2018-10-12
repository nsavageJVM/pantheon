package tech.pegasys.pantheon.ethereum.mainnet;

import static com.google.common.base.Preconditions.checkNotNull;

import tech.pegasys.pantheon.ethereum.core.BlockHashFunction;
import tech.pegasys.pantheon.ethereum.core.BlockImporter;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockProcessor.TransactionReceiptFactory;
import tech.pegasys.pantheon.ethereum.vm.EVM;
import tech.pegasys.pantheon.ethereum.vm.GasCalculator;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProtocolSpecBuilder<T> {
  private Supplier<GasCalculator> gasCalculatorBuilder;
  private Wei blockReward;
  private BlockHashFunction blockHashFunction;
  private TransactionReceiptFactory transactionReceiptFactory;
  private DifficultyCalculator<T> difficultyCalculator;
  private Function<GasCalculator, EVM> evmBuilder;
  private Function<GasCalculator, TransactionValidator> transactionValidatorBuilder;
  private Function<DifficultyCalculator<T>, BlockHeaderValidator<T>> blockHeaderValidatorBuilder;
  private Function<ProtocolSchedule<T>, BlockBodyValidator<T>> blockBodyValidatorBuilder;
  private BiFunction<GasCalculator, EVM, AbstractMessageProcessor> contractCreationProcessorBuilder;
  private Function<GasCalculator, PrecompileContractRegistry> precompileContractRegistryBuilder;
  private BiFunction<EVM, PrecompileContractRegistry, AbstractMessageProcessor>
      messageCallProcessorBuilder;
  private TransactionProcessorBuilder transactionProcessorBuilder;
  private BlockProcessorBuilder blockProcessorBuilder;
  private BlockImporterBuilder<T> blockImporterBuilder;
  private TransactionReceiptType transactionReceiptType;
  private String name;
  private MiningBeneficiaryCalculator miningBeneficiaryCalculator;

  public ProtocolSpecBuilder<T> gasCalculator(final Supplier<GasCalculator> gasCalculatorBuilder) {
    this.gasCalculatorBuilder = gasCalculatorBuilder;
    return this;
  }

  public ProtocolSpecBuilder<T> blockReward(final Wei blockReward) {
    this.blockReward = blockReward;
    return this;
  }

  public ProtocolSpecBuilder<T> blockHashFunction(final BlockHashFunction blockHashFunction) {
    this.blockHashFunction = blockHashFunction;
    return this;
  }

  public ProtocolSpecBuilder<T> transactionReceiptFactory(
      final TransactionReceiptFactory transactionReceiptFactory) {
    this.transactionReceiptFactory = transactionReceiptFactory;
    return this;
  }

  public ProtocolSpecBuilder<T> difficultyCalculator(
      final DifficultyCalculator<T> difficultyCalculator) {
    this.difficultyCalculator = difficultyCalculator;
    return this;
  }

  public ProtocolSpecBuilder<T> evmBuilder(final Function<GasCalculator, EVM> evmBuilder) {
    this.evmBuilder = evmBuilder;
    return this;
  }

  public ProtocolSpecBuilder<T> transactionValidatorBuilder(
      final Function<GasCalculator, TransactionValidator> transactionValidatorBuilder) {
    this.transactionValidatorBuilder = transactionValidatorBuilder;
    return this;
  }

  public ProtocolSpecBuilder<T> blockHeaderValidatorBuilder(
      final Function<DifficultyCalculator<T>, BlockHeaderValidator<T>>
          blockHeaderValidatorBuilder) {
    this.blockHeaderValidatorBuilder = blockHeaderValidatorBuilder;
    return this;
  }

  public ProtocolSpecBuilder<T> blockBodyValidatorBuilder(
      final Function<ProtocolSchedule<T>, BlockBodyValidator<T>> blockBodyValidatorBuilder) {
    this.blockBodyValidatorBuilder = blockBodyValidatorBuilder;
    return this;
  }

  public ProtocolSpecBuilder<T> contractCreationProcessorBuilder(
      final BiFunction<GasCalculator, EVM, AbstractMessageProcessor>
          contractCreationProcessorBuilder) {
    this.contractCreationProcessorBuilder = contractCreationProcessorBuilder;
    return this;
  }

  public ProtocolSpecBuilder<T> precompileContractRegistryBuilder(
      final Function<GasCalculator, PrecompileContractRegistry> precompileContractRegistryBuilder) {
    this.precompileContractRegistryBuilder = precompileContractRegistryBuilder;
    return this;
  }

  public ProtocolSpecBuilder<T> messageCallProcessorBuilder(
      final BiFunction<EVM, PrecompileContractRegistry, AbstractMessageProcessor>
          messageCallProcessorBuilder) {
    this.messageCallProcessorBuilder = messageCallProcessorBuilder;
    return this;
  }

  public ProtocolSpecBuilder<T> transactionProcessorBuilder(
      final TransactionProcessorBuilder transactionProcessorBuilder) {
    this.transactionProcessorBuilder = transactionProcessorBuilder;
    return this;
  }

  public ProtocolSpecBuilder<T> blockProcessorBuilder(
      final BlockProcessorBuilder blockProcessorBuilder) {
    this.blockProcessorBuilder = blockProcessorBuilder;
    return this;
  }

  public ProtocolSpecBuilder<T> blockImporterBuilder(
      final BlockImporterBuilder<T> blockImporterBuilder) {
    this.blockImporterBuilder = blockImporterBuilder;
    return this;
  }

  public ProtocolSpecBuilder<T> transactionReceiptType(
      final TransactionReceiptType transactionReceiptType) {
    this.transactionReceiptType = transactionReceiptType;
    return this;
  }

  public ProtocolSpecBuilder<T> miningBeneficiaryCalculator(
      final MiningBeneficiaryCalculator miningBeneficiaryCalculator) {
    this.miningBeneficiaryCalculator = miningBeneficiaryCalculator;
    return this;
  }

  public ProtocolSpecBuilder<T> name(final String name) {
    this.name = name;
    return this;
  }

  public <R> ProtocolSpecBuilder<R> changeConsensusContextType(
      final Function<DifficultyCalculator<R>, BlockHeaderValidator<R>> blockHeaderValidatorBuilder,
      final Function<ProtocolSchedule<R>, BlockBodyValidator<R>> blockBodyValidatorBuilder,
      final BlockImporterBuilder<R> blockImporterBuilder,
      final DifficultyCalculator<R> difficultyCalculator) {
    return new ProtocolSpecBuilder<R>()
        .gasCalculator(gasCalculatorBuilder)
        .evmBuilder(evmBuilder)
        .transactionValidatorBuilder(transactionValidatorBuilder)
        .contractCreationProcessorBuilder(contractCreationProcessorBuilder)
        .precompileContractRegistryBuilder(precompileContractRegistryBuilder)
        .messageCallProcessorBuilder(messageCallProcessorBuilder)
        .transactionProcessorBuilder(transactionProcessorBuilder)
        .blockHeaderValidatorBuilder(blockHeaderValidatorBuilder)
        .blockBodyValidatorBuilder(blockBodyValidatorBuilder)
        .blockProcessorBuilder(blockProcessorBuilder)
        .blockImporterBuilder(blockImporterBuilder)
        .blockHashFunction(blockHashFunction)
        .blockReward(blockReward)
        .difficultyCalculator(difficultyCalculator)
        .transactionReceiptFactory(transactionReceiptFactory)
        .transactionReceiptType(transactionReceiptType)
        .miningBeneficiaryCalculator(miningBeneficiaryCalculator)
        .name(name);
  }

  public ProtocolSpec<T> build(final ProtocolSchedule<T> protocolSchedule) {
    checkNotNull(gasCalculatorBuilder, "Missing gasCalculator");
    checkNotNull(evmBuilder, "Missing operation registry");
    checkNotNull(transactionValidatorBuilder, "Missing transaction validator");
    checkNotNull(contractCreationProcessorBuilder, "Missing contract creation processor");
    checkNotNull(precompileContractRegistryBuilder, "Missing precompile contract registry");
    checkNotNull(messageCallProcessorBuilder, "Missing message call processor");
    checkNotNull(transactionProcessorBuilder, "Missing transaction processor");
    checkNotNull(blockHeaderValidatorBuilder, "Missing block header validator");
    checkNotNull(blockBodyValidatorBuilder, "Missing block body validator");
    checkNotNull(blockProcessorBuilder, "Missing block processor");
    checkNotNull(blockImporterBuilder, "Missing block importer");
    checkNotNull(blockHashFunction, "Missing block hash function");
    checkNotNull(blockReward, "Missing block reward");
    checkNotNull(difficultyCalculator, "Missing difficulty calculator");
    checkNotNull(transactionReceiptFactory, "Missing transaction receipt factory");
    checkNotNull(transactionReceiptType, "Missing transaction receipt type");
    checkNotNull(name, "Missing name");
    checkNotNull(miningBeneficiaryCalculator, "Missing Mining Beneficiary Calculator");
    checkNotNull(protocolSchedule, "Missing protocol schedule");

    final GasCalculator gasCalculator = gasCalculatorBuilder.get();
    final EVM evm = evmBuilder.apply(gasCalculator);
    final TransactionValidator transactionValidator =
        transactionValidatorBuilder.apply(gasCalculator);
    final AbstractMessageProcessor contractCreationProcessor =
        contractCreationProcessorBuilder.apply(gasCalculator, evm);
    final PrecompileContractRegistry precompileContractRegistry =
        precompileContractRegistryBuilder.apply(gasCalculator);
    final AbstractMessageProcessor messageCallProcessor =
        messageCallProcessorBuilder.apply(evm, precompileContractRegistry);
    final TransactionProcessor transactionProcessor =
        transactionProcessorBuilder.apply(
            gasCalculator, transactionValidator, contractCreationProcessor, messageCallProcessor);
    final BlockHeaderValidator<T> blockHeaderValidator =
        blockHeaderValidatorBuilder.apply(difficultyCalculator);
    final BlockBodyValidator<T> blockBodyValidator =
        blockBodyValidatorBuilder.apply(protocolSchedule);
    final BlockProcessor blockProcessor =
        blockProcessorBuilder.apply(
            transactionProcessor,
            transactionReceiptFactory,
            blockReward,
            miningBeneficiaryCalculator);
    final BlockImporter<T> blockImporter =
        blockImporterBuilder.apply(blockHeaderValidator, blockBodyValidator, blockProcessor);
    return new ProtocolSpec<>(
        name,
        evm,
        transactionValidator,
        transactionProcessor,
        blockHeaderValidator,
        blockBodyValidator,
        blockProcessor,
        blockImporter,
        blockHashFunction,
        transactionReceiptFactory,
        difficultyCalculator,
        blockReward,
        transactionReceiptType,
        miningBeneficiaryCalculator);
  }

  public interface TransactionProcessorBuilder {
    TransactionProcessor apply(
        GasCalculator gasCalculator,
        TransactionValidator transactionValidator,
        AbstractMessageProcessor contractCreationProcessor,
        AbstractMessageProcessor messageCallProcessor);
  }

  public interface BlockProcessorBuilder {
    BlockProcessor apply(
        TransactionProcessor transactionProcessor,
        TransactionReceiptFactory transactionReceiptFactory,
        Wei blockReward,
        MiningBeneficiaryCalculator miningBeneficiaryCalculator);
  }

  public interface BlockImporterBuilder<T> {
    BlockImporter<T> apply(
        BlockHeaderValidator<T> blockHeaderValidator,
        BlockBodyValidator<T> blockBodyValidator,
        BlockProcessor blockProcessor);
  }
}